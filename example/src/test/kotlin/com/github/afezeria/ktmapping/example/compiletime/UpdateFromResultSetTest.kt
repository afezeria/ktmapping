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
class UpdateFromResultSetTest {
    fun template(fn: String): SourceFile = SourceFile.kotlin(
        "test.kt",
        """
package com.github.afezeria.ktmapping

import JavaA
import java.sql.ResultSet
import java.time.LocalDateTime

@Mapper
interface InterfaceTest {
$fn
    fun def() {}
}

class A(val account: String, var name: String, var password: String) {
    var id: String? = null
    var age: Int? = null
    lateinit var createById: String
    lateinit var createDate: LocalDateTime
}

"""
    )

    @Test
    fun simple() {
        val kotlinSource = template(
            """
    fun abc(m: ResultSet, a: A)
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
import java.sql.ResultSet
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun abc(m: ResultSet, a: A): Unit {
        a.id = _getNullable(m, "id")
        a.age = _getNullable(m, "age")
        a.createById = _get(m, "createById")
        a.createDate = _get(m, "createDate")
        a.name = _get(m, "name")
        a.password = _get(m, "password")
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
    fun u1(rs: ResultSet, a: A)
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
import java.sql.ResultSet
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun u1(rs: ResultSet, a: A): Unit {
        a.id = _getNullable(rs, "id")
        a.age = _getNullable(rs, "age")
        a.createById = _get(rs, "createById")
        a.createDate = _get(rs, "createDate")
        a.name = _get(rs, "name")
        a.password = _get(rs, "password")
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
    fun u2(rs: ResultSet, a: A)
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
import java.sql.ResultSet
import java.time.LocalDateTime
import kotlin.Int
import kotlin.String
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun u2(rs: ResultSet, a: A): Unit {
        val idTmp: String? = _getNullable(rs, "id")
        if (idTmp != null) {
            a.id = idTmp
        }
        val ageTmp: Int? = _getNullable(rs, "age")
        if (ageTmp != null) {
            a.age = ageTmp
        }
        val createByIdTmp: String? = _getNullable(rs, "createById")
        if (createByIdTmp != null) {
            a.createById = createByIdTmp
        }
        val createDateTmp: LocalDateTime? = _getNullable(rs, "createDate")
        if (createDateTmp != null) {
            a.createDate = createDateTmp
        }
        val nameTmp: String? = _getNullable(rs, "name")
        if (nameTmp != null) {
            a.name = nameTmp
        }
        val passwordTmp: String? = _getNullable(rs, "password")
        if (passwordTmp != null) {
            a.password = passwordTmp
        }
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
    @Mapping(source = "updateDate", target = "createDate")
    fun u3(rs: ResultSet, a: A)

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
import java.lang.reflect.Field
import java.sql.ResultSet
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    private val com_github_afezeria_ktmapping_A_createById: Field =
            A::class.java.getField("createById")

    private val com_github_afezeria_ktmapping_A_createDate: Field =
            A::class.java.getField("createDate")

    public override fun u3(rs: ResultSet, a: A): Unit {
        val idTmp = a.id
        if (idTmp == null) {
            a.id = _getNullable(rs, "id")
        }
        val ageTmp = a.age
        if (ageTmp == null) {
            a.age = _getNullable(rs, "age")
        }
        val createByIdTmp = com_github_afezeria_ktmapping_A_createById.get(a)
        if (createByIdTmp == null) {
            a.createById = _get(rs, "createById")
        }
        val createDateTmp = com_github_afezeria_ktmapping_A_createDate.get(a)
        if (createDateTmp == null) {
            a.createDate = _get(rs, "updateDate")
        }
        a.name = _get(rs, "name")
        a.password = _get(rs, "password")
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
    @Mapping(source = "updateDate", target = "createDate")
    fun u4(rs: ResultSet, a: A)

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
import java.lang.reflect.Field
import java.sql.ResultSet
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    private val com_github_afezeria_ktmapping_A_createById: Field =
            A::class.java.getField("createById")

    private val com_github_afezeria_ktmapping_A_createDate: Field =
            A::class.java.getField("createDate")

    public override fun u4(rs: ResultSet, a: A): Unit {
        val idTmp = a.id
        if (idTmp == null) {
            a.id = _getNullable(rs, "id")
        }
        val ageTmp = a.age
        if (ageTmp == null) {
            a.age = _getNullable(rs, "age")
        }
        val createByIdTmp = com_github_afezeria_ktmapping_A_createById.get(a)
        if (createByIdTmp == null) {
            a.createById = _get(rs, "createById")
        }
        val createDateTmp = com_github_afezeria_ktmapping_A_createDate.get(a)
        if (createDateTmp == null) {
            a.createDate = _get(rs, "createDate", "updateDate")
        }
        a.name = _get(rs, "name")
        a.password = _get(rs, "password")
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
    @Mapping(source = "updateDate", target = "createDate")
    fun u5(rs: ResultSet, a: A)

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
import java.lang.reflect.Field
import java.sql.ResultSet
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    private val com_github_afezeria_ktmapping_A_createDate: Field =
            A::class.java.getField("createDate")

    public override fun u5(rs: ResultSet, a: A): Unit {
        val createDateTmp = com_github_afezeria_ktmapping_A_createDate.get(a)
        if (createDateTmp == null) {
            a.createDate = _get(rs, "updateDate")
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
    fun ju1(rs: ResultSet, a: JavaA)

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
import com.github.afezeria.ktmapping.MappingExt._get
import com.github.afezeria.ktmapping.MappingExt._getNullable
import java.sql.ResultSet
import kotlin.Unit
import org.springframework.stereotype.Component

@Component
public class InterfaceTestImpl : InterfaceTest {
    public override fun ju1(rs: ResultSet, a: JavaA): Unit {
        val accountTmp = a.account
        if (accountTmp == null) {
            a.account = _getNullable(rs, "account")
        }
        val nameTmp = a.name
        if (nameTmp == null) {
            a.name = _getNullable(rs, "name")
        }
        val passwordTmp = a.password
        if (passwordTmp == null) {
            a.password = _getNullable(rs, "password")
        }
        val idTmp = a.id
        if (idTmp == null) {
            a.id = _getNullable(rs, "id")
        }
        val ageTmp = a.age
        if (ageTmp == null) {
            a.age = _getNullable(rs, "age")
        }
        val addressTmp = a.address
        if (addressTmp == null) {
            a.address = _getNullable(rs, "address")
        }
        val createByIdTmp = a.createById
        if (createByIdTmp == null) {
            a.createById = _getNullable(rs, "createById")
        }
        val createDateTmp = a.createDate
        if (createDateTmp == null) {
            a.createDate = _getNullable(rs, "createDate")
        }
        val modifyByIdTmp = a.modifyById
        if (modifyByIdTmp == null) {
            a.modifyById = _getNullable(rs, "modifyById")
        }
        val modifyDateTmp = a.modifyDate
        if (modifyDateTmp == null) {
            a.modifyDate = _getNullable(rs, "modifyDate")
        }
    }
}

                """.trimIndent()

        val generatedCode = compilation.getGeneratedCode()
        diff(generatedCode, str)
    }
}