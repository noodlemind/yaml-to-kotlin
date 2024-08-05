package io.github.noodlemind.yamlkotlin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class YamlToKotlinTaskTest {

    private lateinit var project: org.gradle.api.Project
    private lateinit var task: YamlToKotlinTask

    @TempDir
    lateinit var testProjectDir: File

    @BeforeEach
    fun setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir).build()

        // Apply the plugin
        project.pluginManager.apply("io.github.noodlemind.yaml-to-kotlin")

        // Get the task
        task = project.tasks.getByName("generateKotlinFromYaml") as YamlToKotlinTask

        // Configure the task
        task.inputDir.set(File(testProjectDir, "schemas"))
        task.outputDir.set(File(testProjectDir, "generated"))
        task.packageName = "com.example.test"

        // Create the input directory
        File(testProjectDir, "schemas").mkdirs()

        // Copy the test schema to the test project directory
        val sourceSchema = File(javaClass.classLoader.getResource("schemas/sample-schema.yaml")?.file ?: "")
        if (sourceSchema.exists()) {
            val targetSchema = File(testProjectDir, "schemas/sample-schema.yaml")
            sourceSchema.copyTo(targetSchema)
        } else {
            throw IllegalStateException("Test schema file not found")
        }
    }

    @Test
    fun testComplexSchemaGeneration() {
        println("Test project directory: ${testProjectDir.absolutePath}")
        println("Input directory: ${task.inputDir.get().asFile.absolutePath}")
        println("Output directory: ${task.outputDir.get().asFile.absolutePath}")
        println("Package name: ${task.packageName}")

        // List files in the input directory
        println("Files in input directory:")
        task.inputDir.get().asFile.walkTopDown().filter { it.isFile }.forEach { println(it.absolutePath) }

        task.generateKotlin()

        // List files in the output directory
        println("Files in output directory:")
        task.outputDir.get().asFile.walkTopDown().filter { it.isFile }.forEach { println(it.absolutePath) }

        val generatedFile = File(testProjectDir, "generated/com/example/test/SampleSchema.kt")
        println("Expected generated file: ${generatedFile.absolutePath}")
        println("Generated file exists: ${generatedFile.exists()}")

        assertTrue(generatedFile.exists(), "Generated Kotlin file should exist")

        if (generatedFile.exists()) {
            val content = generatedFile.readText()
            println("Generated file content:\n$content")

            assertTrue(content.contains("package com.example.test"), "Package declaration should be present")
            assertTrue(content.contains("data class SampleSchema"), "SampleSchema class should be present")
        }
    }

}
