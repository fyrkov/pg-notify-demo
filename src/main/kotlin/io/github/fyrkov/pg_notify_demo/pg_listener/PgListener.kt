package io.github.fyrkov.pg_notify_demo.pg_listener

import io.github.fyrkov.pg_notify_demo.publisher.Publisher
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class PgListener(
    private val dataSource: DataSource,
    private val publisher: Publisher,
    @Value("\${outbox.notify.channel:outbox}") private val channel: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    @PostConstruct
    fun start() {
        job = scope.launch {
            listenLoop()
        }
    }

    @PreDestroy
    fun stop() {
        job?.cancel()
        scope.cancel()
    }

    private fun onNotify(payload: String?) {
        log.info("Received PG notification: {}", payload)
        publisher.publish()
    }

    private suspend fun listenLoop() {
        dataSource.connection.use { conn ->
            conn.autoCommit = true
            conn.createStatement().use { st ->
                // registers the backend session to receive notifications
                st.execute("""listen "$channel"""")
            }

            val pg = conn.unwrap(org.postgresql.PGConnection::class.java)

            while (currentCoroutineContext().isActive) {
                // blocks up to timeoutMs waiting for NOTIFY
                val notifications = pg.getNotifications(0)
                notifications?.forEach { notification ->
                    onNotify(notification.parameter)
                }
            }
        }
    }
}
