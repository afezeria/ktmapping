package com.github.afezeria.ktmapping

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.math.BigDecimal
import java.math.BigInteger
import java.net.InetAddress
import java.sql.*
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*
import kotlin.reflect.KClass

/**
 *
 * @date 2021/8/12
 */
val resultSetCanProvideClasses: Set<KClass<*>> = setOf(
    BigDecimal::class,
    String::class,
    Boolean::class,
    Short::class,
    Integer::class,
    Long::class,
    BigInteger::class,
    Float::class,
    Double::class,
    Date::class,
    Time::class,
    Timestamp::class,
    Calendar::class,
    Blob::class,
    Clob::class,
    java.util.Date::class,
    Array::class,
    SQLXML::class,
    UUID::class,
    InetAddress::class,
    LocalDate::class,
    LocalTime::class,
    LocalDateTime::class,
    OffsetDateTime::class,
)
lateinit var resultSetCanProvideTypeSet: Set<KSClassDeclaration>