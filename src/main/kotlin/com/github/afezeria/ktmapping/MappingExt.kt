package com.github.afezeria.ktmapping

import java.sql.ResultSet
import java.sql.SQLException

object MappingExt {

    inline fun <reified T : Any> _getNullable(rs: ResultSet, vararg keys: String): T? {
        var any: Any?
        for (key in keys) {
            any = rs.getObject(key)
            if (any != null) {
                return try {
                    if (T::class.isInstance(any)) {
                        any as T
                    } else {
                        rs.getObject(key, T::class.java)
                    }
                } catch (e: SQLException) {
                    throw MappingException("[key:${key}] ${e.message}", e)
                }
            }
        }
        return null
    }

    inline fun <reified T : Any> _get(rs: ResultSet, vararg keys: String): T {
        return _getNullable(rs, *keys)
            ?: throw MappingException("Required ${keys.joinToString(" or ")} was null", null)
    }

    inline fun <reified T : Any> _getNullable(map: Map<*, *>, vararg keys: String): T? {
        for (key in keys) {
            try {
                if (map[key] != null) {
                    return map[key] as T
                }
            } catch (e: ClassCastException) {
                throw MappingException("[key:$key] ${e.message}", e)
            }
        }
        return null
    }

    inline fun <reified T : Any> _get(map: Map<*, *>, vararg keys: String): T {
        return _getNullable(map, *keys)
            ?: throw MappingException("Required ${keys.joinToString(" or ")} was null", null)
    }
}