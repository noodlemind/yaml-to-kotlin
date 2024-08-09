package io.github.noodlemind.yamlkotlin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

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
        assertTrue(employeeContent.contains("val departmentName: Department?"), "departmentName property should be present")
        assertTrue(employeeContent.contains("val jobTitle: String?"), "jobTitle property should be present")
        assertTrue(employeeContent.contains("val reportsTo: String?"), "reportsTo property should be present")
        assertTrue(employeeContent.contains("val phoneNumber: String?"), "phoneNumber property should be present")
        assertTrue(employeeContent.contains("val addressDetails: AddressDetails?"), "addressDetails property should be present")

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
        assertTrue(employeeContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()), "isLetter validation should be present for firstName")
        assertTrue(employeeContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()), "isLetter validation should be present for lastName")
        assertTrue(employeeContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()), "isLetter validation should be present for jobTitle")
        assertTrue(employeeContent.contains("@Validate\\(\"regex\\(.*\\)\"\\)".toRegex()), "regex validation should be present for phoneNumber")
        assertTrue(addressContent.contains("@Validate\\(\"minLength\\(2\\)\"\\)".toRegex()), "minLength validation should be present for street")
        assertTrue(addressContent.contains("@Validate\\(\"isNumeric\\(\\)\"\\)".toRegex()), "isNumeric validation should be present for zipCode")
        assertTrue(addressContent.contains("@Validate\\(\"isLetter\\(\\)\"\\)".toRegex()), "isLetter validation should be present for country")

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
        assertTrue(outputDir.listFiles()?.isEmpty() ?: true, "No files should be generated for empty input")
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
}