package com.github.afezeria.ktmapping.example.spring

import com.github.afezeria.ktmapping.MappingException
import com.github.afezeria.ktmapping.example.A
import com.github.afezeria.ktmapping.example.AMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 *
 * @date 2021/9/8
 */
@SpringBootTest
class MapTest {

    @Autowired
    lateinit var ampper: AMapper

    @Nested
    inner class SimpleConvert {
        @Test
        fun success() {
            val map = mapOf(
                "valString" to "abc",
                "valInt" to 2,
                "varString" to "aaa",
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val a = ampper.c1(map)
            assert(a.valString == map["valString"])
            assert(a.valInt == map["valInt"])
            assert(a.varString == map["varString"])
            assert(a.valNullString == map["valNullString"])
            assert(a.e == map["e"])
            assert(a.lateinitLocalDateTime == map["lateinitLocalDateTime"])
        }

        @Test
        fun test2() {
            val map = mapOf(
                "valString" to "abc",
                "valInt" to "abc",
                "varString" to "aaa",
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val ex = assertFailsWith<MappingException> {
                ampper.c1(map)
            }
            assertContains(ex.message!!,
                "[key:valInt] class java.lang.String cannot be cast to class java.lang.Integer")
        }

        @Test
        fun test3() {
            val map = mapOf(
                "valString" to "abc",
                "valInt" to 0,
//            "varString" to null,
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val ex = assertFailsWith<MappingException> {
                ampper.c1(map)
            }
            assertEquals(ex.message!!, "Required varString was null")
        }
    }

    @Nested
    inner class ConvertWithMappingPolicy {
        @Test
        fun firstNotNull() {
            val map = mapOf(
                "s1" to "s1",
                "s2" to "s2",
                "valInt" to 2,
                "varString" to "aaa",
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val a = ampper.c2(map)
            assert(a.valString == map["s1"])
        }

        @Test
        fun lastDeclare() {
            val map = mapOf(
                "s1" to "s1",
                "s2" to "s2",
                "valInt" to 2,
                "varString" to "aaa",
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val a = ampper.c3(map)
            assert(a.valString == map["s2"])
        }

        @Test
        fun onlyExplicit() {
            val map = mapOf(
                "a" to "a",
                "b" to 1,
                "c" to "c",
                "d" to null,
                "valString" to "abc",
                "valInt" to 2,
                "varString" to "aaa",
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val a = ampper.c4(map)
            assert(a.valString == map["a"])
            assert(a.valInt == map["b"])
            assert(a.varString == map["c"])
            assert(a.valNullString == map["d"])
        }
    }

    @Nested
    inner class SimpleUpdate {
        @Test
        fun success() {
            val map = mapOf(
                "valString" to "abc",
                "valInt" to 2,
                "varString" to "aaa",
                "valNullString" to "null",
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val a = A(valString = "", valInt = 0, varString = "", valNullString = null)
            ampper.u1(map, a)
            assert(a.valString != map["valString"])
            assert(a.valInt != map["valInt"])
            assert(a.varString == map["varString"])
            assert(a.valNullString != map["valNullString"])
            assert(a.e == map["e"])
            assert(a.lateinitLocalDateTime == map["lateinitLocalDateTime"])
        }

        @Test
        fun missProperty() {
            val map = mapOf(
//                "varString" to "aaa",
                "valNullString" to "null",
                "e" to "ee",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val a = A(valString = "", valInt = 0, varString = "", valNullString = null)
            val ex = assertFailsWith<MappingException> {
                ampper.u1(map, a)
            }
            assert(ex.message!! == "Required varString was null")
        }

        @Test
        fun sourceIsNotNull() {
            val map = mapOf(
                "varString" to null,
                "e" to "abc",
                "lateinitLocalDateTime" to LocalDateTime.now(),
            )
            val a = A(valString = "", valInt = 0, varString = "", valNullString = null)
            ampper.u2(map, a)
            assert(a.varString != map["varString"])
            assert(a.e == map["e"])
            assert(a.lateinitLocalDateTime == map["lateinitLocalDateTime"])
        }

        @Test
        fun targetIsNull() {
            val map = mapOf(
                "varString" to "abc",
                "e" to "abc",
                "lateinitLocalDateTime" to LocalDateTime.of(2020, 9, 1, 1, 1),
            )
            val a = A(valString = "", valInt = 0, varString = "", valNullString = null).apply {
                e = "e"
                lateinitLocalDateTime = LocalDateTime.now()
            }
            ampper.u3(map, a)
            assert(a.varString == map["varString"])
            assert(a.e != map["e"])
            assert(a.lateinitLocalDateTime != map["lateinitLocalDateTime"])
        }
    }
}