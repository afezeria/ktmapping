package com.github.afezeria.ktmapping.example

import com.github.afezeria.ktmapping.Mapper
import java.time.LocalDateTime

/**
 *
 * @date 2021/9/20
 */
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
    var createDate: LocalDateTime,
    var createBy: String? = null,
)

@Mapper
interface EntityMapper {
    fun c1(c: C1): B

//    @MapperConfig(mappingPolicy = MappingPolicy.FIRST_NOT_NULL)
//    fun c2(c: C2): B
}