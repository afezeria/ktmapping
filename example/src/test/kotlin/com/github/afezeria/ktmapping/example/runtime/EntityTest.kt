package com.github.afezeria.ktmapping.example.runtime

import com.github.afezeria.ktmapping.example.C1
import com.github.afezeria.ktmapping.example.EntityMapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 *
 * @date 2021/9/19
 */
@SpringBootTest
class EntityTest {
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
//        @Test
//        fun test2(){
//            val c = C2(account = "admin", passwd = "123456", createDate = LocalDateTime.now(), createBy = null,)
//            val b = mapper.c2(c)
//            assert(b.account == c.account)
//            assert(b.passwd == c.passwd)
//        }
    }
}