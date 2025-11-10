package dev.whysoezzy.meet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
class MeetBackendApplication

fun main(args: Array<String>) {
	runApplication<MeetBackendApplication>(*args)
}
