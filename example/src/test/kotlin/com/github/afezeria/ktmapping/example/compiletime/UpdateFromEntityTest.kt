package com.github.afezeria.ktmapping.example.compiletime

import com.github.afezeria.ktmapping.example.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

/**
 *
 * @date 2021/8/6
 */
class UpdateFromEntityTest {
    fun template(fn: String): SourceFile = SourceFile.kotlin(
        "test.kt",
        """
package com.github.afezeria.ktmapping

import JavaA
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
    lateinit var modifyById: String
    lateinit var modifyDate: LocalDateTime
}

data class B(
    val account: String,
    val name: String,
    val password: String,
    val age: Int? = null,
    val address: String? = null,
    var updateDate: LocalDateTime,
) {
    lateinit var modifyById: String
    lateinit var modifyDate: LocalDateTime
}


            """
    )

    @Test
    fun simple() {
        val kotlinSource = template(
            "    fun abc(b: B,a: A)"
        )

        val compilation = createKotlinCompilation(kotlinSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: B, a: A): Unit {
        a.age = b.age
        a.modifyById = b.modifyById
        a.modifyDate = b.modifyDate
        a.name = b.name
        a.password = b.password
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }


    @Test
    fun config1() {
        val kotlinSource = template(
            """
    @MapperConfig(updatePolicy = UpdatePolicy.UPDATE_ALL)
    fun abc(b: B, a: A)
            """
        )

        val javaSource = getJavaSource("JavaA.java")

        val compilation = createKotlinCompilation(kotlinSource, javaSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(b: B, a: A): Unit {
        a.age = b.age
        a.modifyById = b.modifyById
        a.modifyDate = b.modifyDate
        a.name = b.name
        a.password = b.password
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun config2() {
        val kotlinSource = template(
            """
    @MapperConfig(updatePolicy = UpdatePolicy.SOURCE_IS_NOT_NULL)
    fun abc(b: B, a: A)
            """
        )

        val javaSource = getJavaSource("JavaA.java")

        val compilation = createKotlinCompilation(kotlinSource, javaSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import java.lang.reflect.Field
import java.time.LocalDateTime
import kotlin.String
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    private val com_github_afezeria_ktmapping_B_modifyById: Field =
            B::class.java.getField("modifyById")

    private val com_github_afezeria_ktmapping_B_modifyDate: Field =
            B::class.java.getField("modifyDate")

    public override fun abc(b: B, a: A): Unit {
        val ageTmp = b.age
        if (ageTmp != null) {
            a.age = ageTmp
        }
        val modifyByIdTmp = com_github_afezeria_ktmapping_B_modifyById.get(b) as String?
        if (modifyByIdTmp != null) {
            a.modifyById = modifyByIdTmp
        }
        val modifyDateTmp = com_github_afezeria_ktmapping_B_modifyDate.get(b) as LocalDateTime?
        if (modifyDateTmp != null) {
            a.modifyDate = modifyDateTmp
        }
        a.name = b.name
        a.password = b.password
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun config3() {
        val kotlinSource = template(
            """
    @MapperConfig(updatePolicy = UpdatePolicy.TARGET_IS_NULL)
    @Mapping(source = "updateDate", target = "modifyDate")
    fun abc(b: B, a: A)
            """
        )

        val javaSource = getJavaSource("JavaA.java")

        val compilation = createKotlinCompilation(kotlinSource, javaSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import java.lang.reflect.Field
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    private val com_github_afezeria_ktmapping_A_modifyById: Field =
            A::class.java.getField("modifyById")

    private val com_github_afezeria_ktmapping_A_modifyDate: Field =
            A::class.java.getField("modifyDate")

    public override fun abc(b: B, a: A): Unit {
        val ageTmp = a.age
        if (ageTmp == null) {
            a.age = b.age
        }
        val modifyByIdTmp = com_github_afezeria_ktmapping_A_modifyById.get(a)
        if (modifyByIdTmp == null) {
            a.modifyById = b.modifyById
        }
        val modifyDateTmp = com_github_afezeria_ktmapping_A_modifyDate.get(a)
        if (modifyDateTmp == null) {
            a.modifyDate = b.updateDate
        }
        a.name = b.name
        a.password = b.password
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun config4() {
        val kotlinSource = template(
            """
    @MapperConfig(updatePolicy = UpdatePolicy.TARGET_IS_NULL, mappingPolicy = MappingPolicy.FIRST_NOT_NULL)
    @Mapping(source = "updateDate", target = "modifyDate")
    fun abc(b: B, a: A)
            """
        )

        val javaSource = getJavaSource("JavaA.java")

        val compilation = createKotlinCompilation(kotlinSource, javaSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import java.lang.reflect.Field
import java.time.LocalDateTime
import kotlin.String
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    private val com_github_afezeria_ktmapping_B_modifyById: Field =
            B::class.java.getField("modifyById")

    private val com_github_afezeria_ktmapping_A_modifyById: Field =
            A::class.java.getField("modifyById")

    private val com_github_afezeria_ktmapping_B_modifyDate: Field =
            B::class.java.getField("modifyDate")

    private val com_github_afezeria_ktmapping_A_modifyDate: Field =
            A::class.java.getField("modifyDate")

    public override fun abc(b: B, a: A): Unit {
        val ageTmp = a.age
        if (ageTmp == null) {
            a.age = b.age
        }
        val modifyByIdTmp = com_github_afezeria_ktmapping_A_modifyById.get(a)
        if (modifyByIdTmp == null) {
            a.modifyById = com_github_afezeria_ktmapping_B_modifyById.get(b) as String
        }
        val modifyDateTmp = com_github_afezeria_ktmapping_A_modifyDate.get(a)
        if (modifyDateTmp == null) {
            a.modifyDate = (com_github_afezeria_ktmapping_B_modifyDate.get(b) ?: b.updateDate) as
                    LocalDateTime
        }
        a.name = b.name
        a.password = b.password
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun config5() {
        val kotlinSource = template(
            """
    @MapperConfig(updatePolicy = UpdatePolicy.TARGET_IS_NULL, mappingPolicy = MappingPolicy.ONLY_EXPLICIT)
    @Mapping(source = "updateDate", target = "modifyDate")
    fun abc(b: B, a: A)
            """
        )

        val javaSource = getJavaSource("JavaA.java")

        val compilation = createKotlinCompilation(kotlinSource, javaSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import java.lang.reflect.Field
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    private val com_github_afezeria_ktmapping_A_modifyDate: Field =
            A::class.java.getField("modifyDate")

    public override fun abc(b: B, a: A): Unit {
        val modifyDateTmp = com_github_afezeria_ktmapping_A_modifyDate.get(a)
        if (modifyDateTmp == null) {
            a.modifyDate = b.updateDate
        }
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }

    @Test
    fun config6() {
        val kotlinSource = template(
            """
    @MapperConfig(updatePolicy = UpdatePolicy.TARGET_IS_NULL)
    fun ju1(b: B, a: JavaA)
            """
        )

        val javaSource = getJavaSource("JavaA.java")

        val compilation = createKotlinCompilation(kotlinSource, javaSource)
        val result = compilation.compile()

        assert(result.exitCode == KotlinCompilation.ExitCode.OK)

        compilation.printGeneratedFile()

        @Language("kotlin")
        val str = """
package com.github.afezeria.ktmapping

import JavaA
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun ju1(b: B, a: JavaA): Unit {
        val accountTmp = a.account
        if (accountTmp == null) {
            a.account = b.account
        }
        val nameTmp = a.name
        if (nameTmp == null) {
            a.name = b.name
        }
        val passwordTmp = a.password
        if (passwordTmp == null) {
            a.password = b.password
        }
        val ageTmp = a.age
        if (ageTmp == null) {
            a.age = b.age
        }
        val addressTmp = a.address
        if (addressTmp == null) {
            a.address = b.address
        }
        val modifyByIdTmp = a.modifyById
        if (modifyByIdTmp == null) {
            a.modifyById = b.modifyById
        }
        val modifyDateTmp = a.modifyDate
        if (modifyDateTmp == null) {
            a.modifyDate = b.modifyDate
        }
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }
}