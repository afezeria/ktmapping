package com.github.afezeria.ktmapping.example.compiletime

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
class ConvertFromMapTest {
    fun template(fn: String): SourceFile = SourceFile.kotlin(
        "test.kt",
        """
package com.github.afezeria.ktmapping

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
        val kotlinSource = template("    fun abc(b: Map<String,Any>): A")

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingExt._get
import com.github.afezeria.ktmapping.MappingExt._getNullable
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: Map<String, Any>): A {
        val result = A(_get(b, "account"), _get(b, "name"), _get(b, "password"))
        result.id = _getNullable(b, "id")
        result.age = _getNullable(b, "age")
        result.createById = _get(b, "createById")
        result.createDate = _get(b, "createDate")
        result.name = _get(b, "name")
        result.password = _get(b, "password")
        return result
    }
}

                """.trimIndent()

        diff(compilation.getGeneratedCode(), str)
    }

    @Test
    fun config1() {
        val kotlinSource = template(
            """
    @MapperConfig(
        sourceNameStyle = [NamingStyle.SNAKE_CASE, NamingStyle.CAMEL_CASE],
        mappingPolicy = MappingPolicy.FIRST_NOT_NULL
    )
    @Mapping(source = "NICK_NAME", target = "name")
    fun abc(b: Map<String,Any>): A
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
import com.github.afezeria.ktmapping.MappingExt._getNullable
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: Map<String, Any>): A {
        val result = A(_get(b, "account"), _get(b, "name", "NICK_NAME"), _get(b, "password"))
        result.id = _getNullable(b, "id")
        result.age = _getNullable(b, "age")
        result.createById = _get(b, "create_by_id", "createById")
        result.createDate = _get(b, "create_date", "createDate")
        result.name = _get(b, "name", "NICK_NAME")
        result.password = _get(b, "password")
        return result
    }
}

                """.trimIndent()
        diff(compilation.getGeneratedCode(), str)
    }

    @Test
    fun config2() {
        val kotlinSource = template(
            """
    @MapperConfig(
        mappingPolicy = MappingPolicy.ONLY_EXPLICIT
    )
    @Mapping(source = "Account",target = "account")
    @Mapping(source = "Name",target = "name")
    @Mapping(source = "pw",target = "password")
    @Mapping(source = "Passwd",target = "password")
    fun bcd(b: Map<String,Any>): A
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
import com.github.afezeria.ktmapping.MappingExt._getNullable
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun bcd(b: Map<String, Any>): A {
        val result = A(_get(b, "Account"), _get(b, "Name"), _get(b, "pw", "Passwd"))
        result.name = _get(b, "Name")
        result.password = _get(b, "pw", "Passwd")
        return result
    }
}

                """.trimIndent()
        diff(compilation.getGeneratedCode(), str)
    }

    @Test
    fun excludeSourceMapping() {
        val kotlinSource = template("""
            @ExcludeMapping(targetMappings = ["age", "id"])
            fun abc(b: Map<String,Any>): A
        """)

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingExt._get
import com.github.afezeria.ktmapping.MappingExt._getNullable
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: Map<String, Any>): A {
        val result = A(_get(b, "account"), _get(b, "name"), _get(b, "password"))
        result.createById = _get(b, "createById")
        result.createDate = _get(b, "createDate")
        result.name = _get(b, "name")
        result.password = _get(b, "password")
        return result
    }
}

                """.trimIndent()

        diff(compilation.getGeneratedCode(), str)
    }

    @Test
    fun excludeTargetMapping() {
        val kotlinSource = template("""
            @ExcludeMapping(targetMappings = ["age", "id"])
            fun abc(b: Map<String,Any>): A
        """)

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingExt._get
import com.github.afezeria.ktmapping.MappingExt._getNullable
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: Map<String, Any>): A {
        val result = A(_get(b, "account"), _get(b, "name"), _get(b, "password"))
        result.createById = _get(b, "createById")
        result.createDate = _get(b, "createDate")
        result.name = _get(b, "name")
        result.password = _get(b, "password")
        return result
    }
}

                """.trimIndent()

        diff(compilation.getGeneratedCode(), str)
    }

    @Test
    fun excludeTarget() {
        val kotlinSource = template("""
            @MapperConfig(
                sourceNameStyle = [NamingStyle.SNAKE_CASE, NamingStyle.CAMEL_CASE],
                mappingPolicy = MappingPolicy.FIRST_NOT_NULL
            )
            @ExcludeMapping(targetMappings = ["age"], sourceMappings = ["id", "create_by_id"])
            fun abc(b: Map<String,Any>): A
        """)

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import com.github.afezeria.ktmapping.MappingExt._get
import com.github.afezeria.ktmapping.MappingExt._getNullable
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: Map<String, Any>): A {
        val result = A(_get(b, "account"), _get(b, "name"), _get(b, "password"))
        result.createById = _get(b, "createById")
        result.createDate = _get(b, "create_date", "createDate")
        result.name = _get(b, "name")
        result.password = _get(b, "password")
        return result
    }
}

                """.trimIndent()

        diff(compilation.getGeneratedCode(), str)
    }
}