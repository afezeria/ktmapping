package com.github.afezeria.ktmapping

/**
 * 运行时从Map/ResultSet中获取的值无法转换成指定类型时抛出该异常
 * @date 2021/9/8
 */
class MappingException(msg: String, throwable: Throwable? = null) : RuntimeException(msg, throwable)