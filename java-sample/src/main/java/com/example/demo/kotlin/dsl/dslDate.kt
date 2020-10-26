package com.example.demo.kotlin.dsl

import org.springframework.util.Assert
import java.time.LocalDateTime
import java.time.Period

// 확장함수의 프로퍼티는 백킹필드를 가질 수 없으므로 getter로 만들어줘야 한다.
val Int.days: Period get() = Period.ofDays(this)

val Period.ago: LocalDateTime get() = LocalDateTime.now() - this
val Period.fromNow: LocalDateTime get() = LocalDateTime.now() + this
val LocalDateTime.toDate get() = this.toLocalDate()

fun main() {
    Assert.isTrue(1.days.ago.toDate == LocalDateTime.now().minusDays(1).toLocalDate())
    Assert.isTrue(1.days.fromNow.toDate == LocalDateTime.now().plusDays(1).toLocalDate())
}

