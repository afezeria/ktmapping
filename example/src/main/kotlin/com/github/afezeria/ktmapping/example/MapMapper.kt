package com.github.afezeria.ktmapping.example

import com.github.afezeria.ktmapping.*
import java.time.LocalDateTime

/**
 *
 * @date 2021/9/8
 */
class A(
    val valString: String,
    val valInt: Int,
    var varString: String,
    val valNullString: String? = null,
) {
    var e: String? = null
    lateinit var lateinitLocalDateTime: LocalDateTime
}

@Mapper
interface MapMapper {
    fun c1(m: Map<String, Any>): A

    @MapperConfig(mappingPolicy = MappingPolicy.FIRST_NOT_NULL)
    @Mapping(source = "s1", target = "valString")
    @Mapping(source = "s2", target = "valString")
    fun c2(m: Map<String, Any>): A

    @MapperConfig(mappingPolicy = MappingPolicy.LAST_DECLARE)
    @Mapping(source = "s1", target = "valString")
    @Mapping(source = "s2", target = "valString")
    fun c3(m: Map<String, Any>): A

    @MapperConfig(mappingPolicy = MappingPolicy.ONLY_EXPLICIT)
    @Mapping(source = "a", target = "valString")
    @Mapping(source = "b", target = "valInt")
    @Mapping(source = "c", target = "varString")
    @Mapping(source = "d", target = "valNullString")
    fun c4(m: Map<String, Any?>): A

    @ExcludeMapping(targetMappings = ["e", "lateinitLocalDateTime"])
    fun c5(m: Map<String, Any?>): A

    fun u1(m: Map<String, Any?>, a: A)

    @MapperConfig(UpdatePolicy.SOURCE_IS_NOT_NULL)
    fun u2(m: Map<String, Any?>, a: A)

    @MapperConfig(UpdatePolicy.TARGET_IS_NULL)
    fun u3(m: Map<String, Any?>, a: A)
}