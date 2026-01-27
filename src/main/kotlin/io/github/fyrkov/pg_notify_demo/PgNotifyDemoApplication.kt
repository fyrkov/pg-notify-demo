package io.github.fyrkov.pg_notify_demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PgNotifyDemoApplication

fun main(args: Array<String>) {
	runApplication<PgNotifyDemoApplication>(*args)
}
