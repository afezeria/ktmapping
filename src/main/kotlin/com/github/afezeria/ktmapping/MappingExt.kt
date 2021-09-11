package com.github.afezeria.ktmapping

import java.sql.ResultSet
import java.sql.SQLException

object MappingExt {

    inline fun <reified T : Any> _get(rs: ResultSet, name: String): T? {
        val any = rs.getObject(name) ?: return null
        return try {
            if (T::class.isInstance(any)) {
                any as T
            } else {
                rs.getObject(name, T::class.java)
            }
        } catch (e: SQLException) {
            throw MappingException("[key:${name}] ${e.message}", e)
        }
    }

    inline fun <reified T : Any> _get(m: Map<*, *>, key: String): T? {
        try {
            val tmp = m[key] ?: return null
            return tmp as T
        } catch (e: ClassCastException) {
            throw MappingException("[key:$key] ${e.message}", e)
        }
    }
}