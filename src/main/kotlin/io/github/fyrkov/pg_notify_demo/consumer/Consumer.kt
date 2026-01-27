package io.github.fyrkov.pg_notify_demo.consumer

import io.github.fyrkov.pg_notify_demo.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

@Component
class Consumer(
    private val outboxRepository: OutboxRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRateString = "PT0.2S")
    fun store() {
        val aggregateType = "account"
        val aggregateId = UUID.randomUUID().toString()
        val payload = """{"balance":${Random.nextInt(1000)}}"""

        // when
        val id = outboxRepository.insert(aggregateType, aggregateId, payload)

        log.info("Stored event id={}", id)
    }
}