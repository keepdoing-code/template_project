package ru.stubby.project

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HeadApplication

fun main(args: Array<String>) {
	runApplication<HeadApplication>(*args)
}
