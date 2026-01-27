package io.github.fyrkov.pg_notify_demo.publisher

import io.github.fyrkov.pg_notify_demo.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PublisherIntegrationTest @Autowired constructor(
    private val publisher: Publisher,
) : AbstractIntegrationTest() {

    @Test
    fun publish() {
        publisher.publish(listOf(123))
    }
}