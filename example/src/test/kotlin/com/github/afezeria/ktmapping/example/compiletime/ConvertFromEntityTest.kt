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
class ConvertFromEntityTest {
    fun template(fn: String): SourceFile = SourceFile.kotlin(
        "test.kt",
        """
package com.github.afezeria.ktmapping

import java.time.LocalDateTime

@Mapper
interface InterfaceTest {
$fn
    fun def() {}
}

class A(val account: String, var name: String, var password: String) {
    var id: String? = null
    var age: Int? = null
    val address: String? = null
    lateinit var createById: String
    lateinit var createDate: LocalDateTime
}

data class B(
    val account: String,
    val name: String,
    val password: String,
    val age: Int? = null,
    val address: String? = null,
)

            """
    )

    @Test
    fun sample() {
        val kotlinSource = template("fun abc(b: B): A")

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()
        requireNotNull("") { "" }
        requireNotNull("")

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: B): A {
        val result = A(b.account, b.name, b.password)
        result.age = b.age
        result.name = b.name
        result.password = b.password
        return result
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun excludeSourceMapping() {
        val kotlinSource = template("""
            @ExcludeMapping(sourceMappings = ["age"])
            fun abc(b: B): A
        """)

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: B): A {
        val result = A(b.account, b.name, b.password)
        result.name = b.name
        result.password = b.password
        return result
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun excludeTargetMapping() {
        val kotlinSource = template("""
            @ExcludeMapping(targetMappings = ["age"])
            fun abc(b: B): A
        """)

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()
        requireNotNull("") { "" }
        requireNotNull("")

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: B): A {
        val result = A(b.account, b.name, b.password)
        result.name = b.name
        result.password = b.password
        return result
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }
}