package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingPolicy.*
import com.github.afezeria.ktmapping.NamingStyle.*
import com.github.afezeria.ktmapping.UpdatePolicy.*


/**
 * 标记映射接口
 */
@Target(AnnotationTarget.CLASS)
annotation class Mapper

/**
 * 映射规则配置
 *
 * [updatePolicy] 更新策略
 *
 * [mappingPolicy] 规则生效策略
 *
 * [sourceNameStyle] 从map映射到实体类时，实体类中的属性对应的map中的属性的命名规则
 *
 * [targetNameStyle] 从实体类映射到map时，实体类中的属性对应的map中的属性的命名规则
 *
 * [targetLateinitFieldPolicy] lateinit字段处理规则
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class MapperConfig(
    val updatePolicy: UpdatePolicy = UPDATE_ALL,
    val mappingPolicy: MappingPolicy = LAST_DECLARE,
    val sourceNameStyle: Array<NamingStyle> = [CAMEL_CASE],
)


/**
 * 显示指定映射关系
 *
 * [source] String 来源属性名
 * [target] String 目标属性名
 * @constructor
 */
@Repeatable
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Mapping(val source: String, val target: String)

/**
 * 映射规则生效策略
 *
 * 当从对象映射到对象时，source中的属性为最先声明的规则
 *
 * 当从map映射到对象时，target中的属性为最先声明的规则
 *
 * [LAST_DECLARE] 使用最后声明的规则
 *
 * [FIRST_NOT_NULL] 应用所有的规则，按照声明顺序从source取值，取到第一个不为null的值后结束，
 * 该选项假设所有的映射中至少有一个不为null，当[MapperConfig.updatePolicy]为[UpdatePolicy.SOURCE_IS_NOT_NULL]时，
 * 该选项不起作用
 *
 * [ONLY_EXPLICIT] 只应用使用mapping注解显示声明的规则，此时一个target中的属性只能对应到一个source中的属性
 */
enum class MappingPolicy {
    LAST_DECLARE,
    FIRST_NOT_NULL,
    ONLY_EXPLICIT,
}

/**
 * 函数为更新时的属性覆盖策略
 *
 * [UPDATE_ALL] 更新全部可更新字段
 *
 * [TARGET_IS_NULL] 当target中对应属性为空时更新
 *
 * [SOURCE_IS_NOT_NULL] 当source中对应属性不为空时更新
 */
enum class UpdatePolicy {
    UPDATE_ALL,
    TARGET_IS_NULL,
    SOURCE_IS_NOT_NULL,
}


/**
 * 自动映射且来源为Map或ResultSet类型时 source 中属性名称可选的命名风格
 *
 * 同一属性多个别名同时存在时取优先级最高的
 *
 * 优先级按照使用时声明顺序递减
 *
 * [CAMEL_CASE] userId -> userId
 *
 * [CONSTANT_CASE] USER_ID -> userId
 *
 * [DASH_CASE] user-id -> userId
 *
 * [HTTP_HEADER_CASE] User-Id -> userId
 *
 * [PASCAL_CASE] UserId -> userId
 *
 * [SNAKE_CASE] user_id -> userId
 *
 * [TRAIN_CASE] USER-ID -> userId
 */
enum class NamingStyle {
    CAMEL_CASE,
    CONSTANT_CASE,
    DASH_CASE,
    HTTP_HEADER_CASE,
    PASCAL_CASE,
    SNAKE_CASE,
    TRAIN_CASE,
}