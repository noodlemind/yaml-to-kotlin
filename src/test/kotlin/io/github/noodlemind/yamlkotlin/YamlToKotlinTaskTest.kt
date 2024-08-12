package io.github.noodlemind.yamlkotlin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class YamlToKotlinTaskTest {

    private lateinit var task: YamlToKotlinTask
    private lateinit var project: Project

    @TempDir
    lateinit var testProjectDir: File

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder().build()
        task = project.tasks.create("generateKotlin", YamlToKotlinTask::class.java)

        val inputDir = testProjectDir.resolve("input")
        val outputDir = testProjectDir.resolve("output")

        task.inputDir.set(inputDir)
        task.outputDir.set(outputDir)
        task.packageName = "com.example.generated"
        task.schemaFileExtension = "yml"

        println("Current working directory: ${System.getProperty("user.dir")}")
        println("Input directory: ${inputDir.absolutePath}")
        println("Output directory: ${outputDir.absolutePath}")

        // Copy the sample schema to the input directory
        val sampleSchemaResource = javaClass.classLoader.getResource("schemas/sample-schema.yml")
        if (sampleSchemaResource == null) {
            fail("schemas/sample-schema.yml not found in resources")
        } else {
            println("Found sample-schema.yml at: ${sampleSchemaResource.path}")
            val sampleSchemaContent = sampleSchemaResource.readText()
            inputDir.mkdirs()
            val targetFile = inputDir.resolve("sample-schema.yml")
            targetFile.writeText(sampleSchemaContent)
            println("Copied sample-schema.yml to: ${targetFile.absolutePath}")
        }
    }

    @Test
    fun `test YAML parsing and Kotlin generation`() {
        // Act
        task.generateKotlin()

        // Assert
        val outputDir = task.outputDir.get().asFile
        assertTrue(outputDir.exists(), "Output directory should exist")

        // Check for generated files
        assertTrue(outputDir.resolve("Employee.kt").exists(), "Employee.kt should be generated")
        assertTrue(outputDir.resolve("Address.kt").exists(), "Address.kt should be generated")
        assertTrue(outputDir.resolve("Department.kt").exists(), "Department.kt should be generated")

        // Check Employee.kt content
        val employeeContent = outputDir.resolve("Employee.kt").readText()
        assertTrue(employeeContent.contains("data class Employee"), "Employee class should be generated")
        assertTrue(employeeContent.contains("val firstName: String"), "firstName property should be present")
        assertTrue(employeeContent.contains("val lastName: String"), "lastName property should be present")
        assertTrue(employeeContent.contains("val email: String?"), "email property should be present")
        assertTrue(
            employeeContent.contains("val departmentName: Department?"), "departmentName property should be present"
        )
        assertTrue(employeeContent.contains("val jobTitle: String?"), "jobTitle property should be present")
        assertTrue(employeeContent.contains("val reportsTo: String?"), "reportsTo property should be present")
        assertTrue(employeeContent.contains("val phoneNumber: String?"), "phoneNumber property should be present")
        assertTrue(
            employeeContent.contains("val addressDetails: AddressDetails?"), "addressDetails property should be present"
        )

        // Check Address.kt content
        val addressContent = outputDir.resolve("Address.kt").readText()
        assertTrue(addressContent.contains("data class Address"), "Address class should be generated")
        assertTrue(addressContent.contains("val street: String?"), "street property should be present")
        assertTrue(addressContent.contains("val zipCode: String?"), "zipCode property should be present")
        assertTrue(addressContent.contains("val country: String?"), "country property should be present")

        // Check Department.kt content
        val departmentContent = outputDir.resolve("Department.kt").readText()
        assertTrue(departmentContent.contains("enum class Department"), "Department enum should be generated")
        assertTrue(departmentContent.contains("SALES"), "SALES enum value should be present")
        assertTrue(departmentContent.contains("MARKETING"), "MARKETING enum value should be present")
        assertTrue(departmentContent.contains("HR"), "HR enum value should be present")
        assertTrue(departmentContent.contains("IT"), "IT enum value should be present")
        assertTrue(departmentContent.contains("FINANCE"), "FINANCE enum value should be present")

        // Check for validations
        assertTrue(
            employeeContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()),
            "isLetter validation should be present for firstName"
        )
        assertTrue(
            employeeContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()),
            "isLetter validation should be present for lastName"
        )
        assertTrue(
            employeeContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()),
            "isLetter validation should be present for jobTitle"
        )
        assertTrue(
            employeeContent.contains("@Validate\\(\"regex\\(.*\\)\"\\)".toRegex()),
            "regex validation should be present for phoneNumber"
        )
        assertTrue(
            addressContent.contains("@Validate\\(\"minLength\\(2\\)\"\\)".toRegex()),
            "minLength validation should be present for street"
        )
        assertTrue(
            addressContent.contains("@Validate\\(\"isNumeric\\(\\)\"\\)".toRegex()),
            "isNumeric validation should be present for zipCode"
        )
        assertTrue(
            addressContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()),
            "isLetter validation should be present for country"
        )

    }

    @Test
    fun `test empty input directory`() {
        // Arrange
        task.inputDir.get().asFile.deleteRecursively()
        task.inputDir.get().asFile.mkdirs()

        // Act
        task.generateKotlin()

        // Assert
        val outputDir = task.outputDir.get().asFile
        assertTrue(outputDir.exists(), "Output directory should exist even with empty input")
        assertTrue(outputDir.listFiles()?.isEmpty() != false, "No files should be generated for empty input")
    }

    @Test
    fun `test custom package name`() {
        // Arrange
        task.packageName = "com.custom.mypackage"

        // Act
        task.generateKotlin()

        // Assert
        val outputDir = task.outputDir.get().asFile
        val employeeContent = File(outputDir, "Employee.kt").readText()
        assertTrue(employeeContent.contains("package com.custom.mypackage"), "Custom package name should be used")
    }

    @Test
    fun `test custom file extension`() {
        // Arrange
        task.schemaFileExtension = "yaml"
        val inputDir = task.inputDir.get().asFile
        inputDir.resolve("sample-schema.yml").renameTo(inputDir.resolve("sample-schema.yaml"))

        // Act
        task.generateKotlin()

        // Assert
        val outputDir = task.outputDir.get().asFile
        assertTrue(outputDir.resolve("Employee.kt").exists(), "Should process files with custom extension")
    }

    @Test
    fun `test regex pattern validation`() {
        // Act
        task.generateKotlin()

        // Assert
        val outputDir = task.outputDir.get().asFile
        assertTrue(outputDir.exists(), "Output directory should exist")

        // Check Employee.kt content
        val employeeContent = outputDir.resolve("Employee.kt").readText()
        println("Generated Employee.kt content:\n$employeeContent")

        val expectedRegexPattern = """@Validate\("regex\(\"(\^|\\^)\[0-9\]\{10\}(\$|\\$)\"\)"\)"""
        val containsPattern = employeeContent.contains(expectedRegexPattern.toRegex())

        println("Expected pattern: $expectedRegexPattern")
        println("Contains pattern: $containsPattern")

        assertTrue(containsPattern, "regex validation should be present for phoneNumber")

        // Additional check for the exact regex pattern
        val regexPattern = """regex\("(\^|\\^)\[0-9\]\{10\}(\$|\\$)"\)""".toRegex()
        val matchResult = regexPattern.find(employeeContent)

        assertNotNull(matchResult, "Regex pattern should be found in the generated code")

        val extractedRegex = matchResult?.groupValues?.get(0)
        println("Extracted regex: $extractedRegex")

        assertEquals(
            """regex("^[0-9]{10}$")""",
            extractedRegex,
            "The extracted regex should match the expected pattern"
        )
    }

    @Test
    fun `test Employee class generation`() {
        // Act
        task.generateKotlin()

        // Assert
        val outputDir = task.outputDir.get().asFile
        assertTrue(outputDir.exists(), "Output directory should exist")

        // Check Employee.kt content
        val employeeContent = outputDir.resolve("Employee.kt").readText()
        println("Generated Employee.kt content:\n$employeeContent")

        // Check for data class declaration
        assertTrue(employeeContent.contains("data class Employee("), "Should be a data class")

        // Check for properties
        val propertyPattern = """val\s+\w+:\s+\w+(\?)?""".toRegex()
        val properties = propertyPattern.findAll(employeeContent).map { it.value }.toList()
        assertTrue(properties.size >= 8, "Should have at least 8 properties")

        // Check for specific properties and their types
        assertTrue(
            properties.any { it.matches("""val\s+firstName:\s+String""".toRegex()) }, "Should have firstName property"
        )
        assertTrue(
            properties.any { it.matches("""val\s+lastName:\s+String""".toRegex()) }, "Should have lastName property"
        )
        assertTrue(properties.any { it.matches("""val\s+email:\s+String\?""".toRegex()) }, "Should have email property")
        assertTrue(
            properties.any { it.matches("""val\s+departmentName:\s+Department\?""".toRegex()) },
            "Should have departmentName property"
        )
        assertTrue(
            properties.any { it.matches("""val\s+jobTitle:\s+String\?""".toRegex()) }, "Should have jobTitle property"
        )
        assertTrue(
            properties.any { it.matches("""val\s+reportsTo:\s+String\?""".toRegex()) }, "Should have reportsTo property"
        )
        assertTrue(
            properties.any { it.matches("""val\s+phoneNumber:\s+String\?""".toRegex()) },
            "Should have phoneNumber property"
        )
        assertTrue(
            properties.any { it.matches("""val\s+addressDetails:\s+AddressDetails\?""".toRegex()) },
            "Should have addressDetails property"
        )

        // Check for Validate annotations
        assertTrue(employeeContent.contains("""@Validate("isLetter()")"""), "Should have isLetter validation")

        // Update this line to use a regex pattern that matches the generated code
        val phoneRegexPattern = """@Validate\("regex\(\"(\^|\\^)\[0-9\]\{10\}(\$|\\$)\"\)"\)""".toRegex()
        assertTrue(
            phoneRegexPattern.containsMatchIn(employeeContent), "Should have phone number regex validation"
        )

        // Check for imports
        assertTrue(employeeContent.contains("import com.example.generated.Validate"), "Should import Validate")
        assertTrue(employeeContent.contains("import kotlinx.serialization.Serializable"), "Should import Serializable")

        // Check for single Validate import
        val validateImportCount = employeeContent.lines().count { it.trim() == "import com.example.generated.Validate" }
        assertEquals(1, validateImportCount, "Should have only one Validate import")

        // Check for the absence of incorrect import
        assertFalse(employeeContent.contains("import Validate"), "Should not have an unqualified Validate import")
    }
}