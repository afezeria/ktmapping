package com.github.afezeria.ktmapping.example.success

import com.github.afezeria.ktmapping.example.createKotlinCompilation
import com.github.afezeria.ktmapping.example.diff
import com.github.afezeria.ktmapping.example.getGeneratedCode
import com.github.afezeria.ktmapping.example.printGeneratedFile
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

/**
 *
 * @date 2021/8/6
 */
class ConvertFromResultSetTest {
    fun template(fn: String): SourceFile = SourceFile.kotlin(
        "test.kt",
        """
package com.github.afezeria.ktmapping

import java.sql.ResultSet
import java.time.LocalDateTime

@Mapper
interface InterfaceTest {
$fn
}

class A(val account: String, var name: String, var password: String) {
    var id: String? = null
    var age: Int? = null
    val address: String? = null
    lateinit var createById: String
    lateinit var createDate: LocalDateTime
}

            """
    )

    @Test
    fun sample() {

        val kotlinSource = template("    fun abc(b: ResultSet): A")

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()


        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingExt._get
import java.sql.ResultSet
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: ResultSet): A {
        val result = A(requireNotNull(_get(b, "account")), requireNotNull(_get(b, "name")),
                requireNotNull(_get(b, "password")))
        result.id = _get(b, "id")
        result.age = _get(b, "age")
        result.createById = requireNotNull(_get(b, "createById"))
        result.createDate = requireNotNull(_get(b, "createDate"))
        result.name = requireNotNull(_get(b, "name"))
        result.password = requireNotNull(_get(b, "password"))
        return result
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun conifg1() {

        val kotlinSource = template("""
    @MapperConfig(
        sourceNameStyle = [NamingStyle.SNAKE_CASE, NamingStyle.CAMEL_CASE],
        mappingPolicy = MappingPolicy.FIRST_NOT_NULL
    )
    @Mapping(source = "NICK_NAME", target = "name")
    fun abc(b: ResultSet): A
        """
        )

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()


        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingExt._get
import java.sql.ResultSet
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: ResultSet): A {
        val result = A(requireNotNull(_get(b, "account")), requireNotNull(_get(b, "name") ?: _get(b,
                "NICK_NAME")), requireNotNull(_get(b, "password")))
        result.id = _get(b, "id")
        result.age = _get(b, "age")
        result.createById = requireNotNull(_get(b, "create_by_id") ?: _get(b, "createById"))
        result.createDate = requireNotNull(_get(b, "create_date") ?: _get(b, "createDate"))
        result.name = requireNotNull(_get(b, "name") ?: _get(b, "NICK_NAME"))
        result.password = requireNotNull(_get(b, "password"))
        return result
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun conifg2() {

        val kotlinSource = template("""
    @MapperConfig(
        mappingPolicy = MappingPolicy.ONLY_EXPLICIT
    )
    @Mapping(source = "Account",target = "account")
    @Mapping(source = "Name",target = "name")
    @Mapping(source = "pw",target = "password")
    @Mapping(source = "Passwd",target = "password")
    fun bcd(b: ResultSet): A
        """
        )

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()


        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingExt._get
import java.sql.ResultSet
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun bcd(b: ResultSet): A {
        val result = A(requireNotNull(_get(b, "Account")), requireNotNull(_get(b, "Name")),
                requireNotNull(_get(b, "pw") ?: _get(b, "Passwd")))
        result.name = requireNotNull(_get(b, "Name"))
        result.password = requireNotNull(_get(b, "pw") ?: _get(b, "Passwd"))
        return result
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }
}

