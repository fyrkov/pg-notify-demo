package io.github.fyrkov.pg_notify_demo.repository

import io.github.fyrkov.pg_notify_demo.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional

@Transactional
@Rollback
class OutboxRepositoryIntegrationTest @Autowired constructor(
    private val outboxRepository: OutboxRepository,
) : AbstractIntegrationTest() {


    @Test
    fun `should insert a record in the outbox`() {
        // given
        val aggregateType = "account"
        val aggregateId = "123"
        val payload = """{"balance":100}"""

        // when
        val id = outboxRepository.insert(aggregateType, aggregateId, payload)

        // then
        assertNotNull(id)
    }

    @Test
    fun `should select unpublished records`() {
        // given
        val aggregateType = "account"
        val aggregateId = "123"
        val payload = """{"balance":100}"""

        // when
        val id = outboxRepository.insert(aggregateType, aggregateId, payload)

        // when
        val records = outboxRepository.selectUnpublished(10)

        // then
        assertEquals(1, records.size)
    }
}