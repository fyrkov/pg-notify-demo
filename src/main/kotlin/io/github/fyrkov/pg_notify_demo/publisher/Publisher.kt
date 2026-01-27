package io.github.fyrkov.pg_notify_demo.publisher

import io.github.fyrkov.pg_notify_demo.domain.OutboxRecord
import io.github.fyrkov.pg_notify_demo.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class Publisher(
    private val outboxRepository: OutboxRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRateString = "\${outbox.publish.interval}")
    @Transactional
    fun publish() {
        val records: List<OutboxRecord> = outboxRepository.selectUnpublished(100)
        // Batch publish records
        log.info("Published {} records", records.size)
    }
}