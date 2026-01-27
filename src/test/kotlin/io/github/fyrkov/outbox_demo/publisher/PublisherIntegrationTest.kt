package io.github.fyrkov.outbox_demo.publisher

import io.github.fyrkov.outbox_demo.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PublisherIntegrationTest @Autowired constructor(
    private val publisher: Publisher,
) : AbstractIntegrationTest() {

    @Test
    fun publish() {
        publisher.publish()
    }
}