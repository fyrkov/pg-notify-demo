package io.github.fyrkov.pg_notify_demo.repository

import io.github.fyrkov.pg_notify_demo.domain.OutboxRecord
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class OutboxRepository(
    private val dsl: DSLContext,
) {

    fun insert(aggregateType: String, aggregateId: String, payload: String): Long {
        return dsl.insertInto(table("outbox"))
            .set(field("aggregate_type", String::class.java), aggregateType)
            .set(field("aggregate_id", String::class.java), aggregateId)
            .set(field("payload", JSONB::class.java), JSONB.valueOf(payload))
            .returning(field("id"))
            .fetchSingle()
            .get(field("id"), Long::class.java)
    }

    fun selectUnpublished(limit: Int): List<OutboxRecord> {
        return dsl.fetch(
            """
                with picked as (
                  select id
                  from outbox
                  where published_at is null
                  order by id
                  limit ?
                  for update skip locked
                )
                update outbox
                set published_at = now()
                where id in (select id from picked)
                returning *;
                """.trimIndent(),
            limit
        ).map { deser(it) }
    }

    private fun deser(record: Record): OutboxRecord = OutboxRecord(
        id = record.get(field("id", Long::class.java)),
        aggregateType = record.get(field("aggregate_type", String::class.java)),
        aggregateId = record.get(field("aggregate_id", String::class.java)),
        payload = record.get(field("payload", JSONB::class.java))?.data() ?: "{}",
        createdAt = record.get(field("created_at"), Instant::class.java),
        publishedAt = record.get(field("published_at"), Instant::class.java)
    )
}