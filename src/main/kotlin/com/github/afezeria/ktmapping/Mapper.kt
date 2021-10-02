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
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class MapperConfig(
    /**
     * 更新策略
     */
    val updatePolicy: UpdatePolicy = UPDATE_ALL,
    /**
     * 规则生效策略
     */
    val mappingPolicy: MappingPolicy = LAST_DECLARE,
    /**
     * 从map映射到实体类时，实体类中的属性对应的map中的属性的命名规则
     */
    val sourceNameStyle: Array<NamingStyle> = [CAMEL_CASE],
)


/**
 * 从映射关系中排除指定属性
 * [sourceMappings]/[targetMappings] 中的字符串对应的属性名在映射中不存在时将会编译失败
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ExcludeMapping(
    val sourceMappings: Array<String> = [],
    val targetMappings: Array<String> = [],
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
 * 映射生效策略
 *
 * 当从对象映射到对象时，source中的属性为最先声明的映射
 *
 * 当从map映射到对象时，target中的属性为最先声明的映射
 */
enum class MappingPolicy {
    /**
     * 使用最后声明的映射
     */
    LAST_DECLARE,

    /**
     * 应用所有的映射，按照声明顺序从source取值，取到第一个不为null的值后结束
     *
     * 当转换为 entity->entity 的形式时，该选项允许编译期 T?->T 的映射，
     * 非空检查被移动到运行时
     *
     * 该选项假设运行时所有的映射中至少有一个不为null
     *
     * 当[MapperConfig.updatePolicy]为[UpdatePolicy.SOURCE_IS_NOT_NULL]时该选项不起作用
     */
    FIRST_NOT_NULL,
    /**
     * 只应用使用mapping注解显示声明的映射
     * 此时一个target中的属性只能对应到一个source中的属性
     */
    ONLY_EXPLICIT,
}

/**
 * 函数为更新时的属性覆盖策略
 */
enum class UpdatePolicy {
    /**
     * 更新全部可更新字段
     */
    UPDATE_ALL,
    /**
     * 当target中对应属性为空时更新
     */
    TARGET_IS_NULL,
    /**
     * 当source中对应属性不为空时更新
     */
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