package com.github.afezeria.ktmapping.example.runtime

import com.github.afezeria.ktmapping.Mapper
import com.github.afezeria.ktmapping.MapperConfig
import com.github.afezeria.ktmapping.MappingPolicy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

/**
 *
 * @date 2021/9/19
 */
@SpringBootTest
class EntityMapperTest {
    @Autowired
    lateinit var mapper: EntityMapper

    @Nested
    inner class SimpleConvert {
        @Test
        fun test1() {
            val c = C1(account = "admin", passwd = "123456", email = "aa@aa.com", age = null)
            val b = mapper.c1(c)
            assert(b.account == c.account)
            assert(b.passwd == c.passwd)
            assert(b.email == c.email)
            assert(b.age == null)
        }
    }

    @Nested
    inner class ConvertWithConfig {
        @Test
        fun firstNotNull() {
            var c = C2(account = "admin", passwd = "123456", createBy = "abc")
            val b = mapper.firstNotNull(c)
            assert(b.account == c.account)
            assert(b.passwd == c.passwd)
            assert(b.createBy == c.createBy)
            c = C2(account = "admin", passwd = "123456")
            assertThrows<NullPointerException> {
                mapper.firstNotNull(c)
            }

        }
    }
}
class B(
    val account: String,
    val passwd: String,
) {
    lateinit var id: String
    var email: String? = null
    var age: Int? = null

    lateinit var createDate: LocalDateTime
    lateinit var createBy: String
}

class C1(
    val account: String,
    val passwd: String,
    var email: String? = null,
    var age: Int? = null,
)

class C2(
    val account: String,
    val passwd: String,
    var createBy: String? = null,
)

@Mapper
interface EntityMapper {
    fun c1(c: C1): B

    @MapperConfig(mappingPolicy = MappingPolicy.FIRST_NOT_NULL)
    fun firstNotNull(c: C2): B
}
