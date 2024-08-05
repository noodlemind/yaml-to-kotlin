package io.github.noodlemind.yamlkotlin

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

abstract class YamlToKotlinTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    var packageName: String = "com.example.generated"

    @get:Input
    var schemaFileExtension: String = "yaml"

    @get:Input
    var excludeFiles: List<String> = emptyList()

    @get:Input
    var generateValidations: Boolean = true

    @get:Input
    @get:Optional
    var customValidationPackage: String? = null

    @get:Input
    var overwriteExistingFiles: Boolean = true

    @get:Input
    var customTypeMapping: Map<String, String> = emptyMap()

    @get:Input
    var generateDataClasses: Boolean = true

    @get:Input
    var generateSealedClasses: Boolean = false

    @get:Input
    var addKotlinxSerializationAnnotations: Boolean = false

    @get:Input
    @get:Optional
    var configFile: File? = null

    @get:Input
    var verbose: Boolean = false

    @TaskAction
    fun generateKotlin() {
        logger.lifecycle("Generating Kotlin code from YAML schemas")
        logger.lifecycle("Input directory: ${inputDir.get().asFile.absolutePath}")
        logger.lifecycle("Output directory: ${outputDir.get().asFile.absolutePath}")
        logger.lifecycle("Package name: $packageName")

        val inputFiles =
            inputDir.asFile.get().walkTopDown().filter { it.isYamlFile() }.filterNot { it.isExcluded() }.toList()

        logger.lifecycle("Found ${inputFiles.size} YAML files to process")

        val generatedFiles = inputFiles.map { file ->
            logger.lifecycle("Processing file: ${file.absolutePath}")
            processYamlFile(file)
        }

        logger.lifecycle("YAML to Kotlin generation completed")
        logger.lifecycle("Generated files:")
        generatedFiles.forEach { logger.lifecycle("- ${it.absolutePath}") }
    }

    private fun File.isYamlFile() = isFile && extension == schemaFileExtension

    private fun File.isExcluded() = excludeFiles.any { name.matches(it.toRegex()) }

    private fun processYamlFile(file: File): File {
        val content = file.readYamlContent()
        val fileName = file.nameWithoutExtension.toClassName()

        val kotlinFile =
            FileSpec.builder(packageName, fileName).apply { generateClasses(content, fileName, this) }.build()

        val outputFile = outputDir.asFile.get()
        outputFile.mkdirs()

        val generatedFile = outputFile.resolve("$fileName.kt")
        kotlinFile.writeTo(outputFile)

        logger.lifecycle("Generated Kotlin file: ${generatedFile.absolutePath}")

        return generatedFile
    }

    private fun File.readYamlContent(): Map<String, Any> = when (val yaml = Yaml().load<Any>(readText())) {
        is Map<*, *> -> yaml.mapKeys { it.key.toString() }.mapValues { it.value ?: Any() }
        else -> emptyMap()
    }

    private fun generateClasses(content: Map<String, Any>, className: String, fileBuilder: FileSpec.Builder) {
        val classBuilder = TypeSpec.classBuilder(className).apply {
            if (generateDataClasses) addModifiers(KModifier.DATA)
            if (addKotlinxSerializationAnnotations) {
                addAnnotation(ClassName("kotlinx.serialization", "Serializable"))
            }
        }

        val constructor = FunSpec.constructorBuilder()

        content.forEach { (key, value) ->
            val property = createProperty(key, value, fileBuilder)
            classBuilder.addProperty(property)
            constructor.addParameter(key, property.type)
        }

        classBuilder.primaryConstructor(constructor.build())
        fileBuilder.addType(classBuilder.build())
    }

    private fun createProperty(key: String, value: Any, fileBuilder: FileSpec.Builder): PropertySpec = when (value) {
        is Map<*, *> -> createNestedClassProperty(key,
            value.mapKeys { it.key.toString() }.mapValues { it.value ?: Any() },
            fileBuilder
        )

        is List<*> -> createListProperty(key)
        else -> createSimpleProperty(key, value)
    }

    private fun createNestedClassProperty(
        key: String, value: Map<String, Any>, fileBuilder: FileSpec.Builder
    ): PropertySpec {
        val nestedClassName = key.toClassName()
        generateClasses(value, nestedClassName, fileBuilder)
        return PropertySpec.builder(key.toCamelCase(), ClassName(packageName, nestedClassName))
            .initializer(nestedClassName).build()
    }

    private fun createListProperty(key: String): PropertySpec =
        PropertySpec.builder(key.toCamelCase(), LIST.parameterizedBy(ANY)).initializer("emptyList()").build()

    private fun createSimpleProperty(key: String, value: Any): PropertySpec {
        val type = value.kotlinType()
        return PropertySpec.builder(key.toCamelCase(), type).initializer(type.defaultValue()).build()
    }

    private fun Any.kotlinType(): ClassName = when (this) {
        is String -> STRING
        is Int -> INT
        is Boolean -> BOOLEAN
        is Double -> DOUBLE
        else -> ANY
    }

    private fun ClassName.defaultValue(): String = when (this) {
        STRING -> "\"\""
        INT -> "0"
        BOOLEAN -> "false"
        DOUBLE -> "0.0"
        else -> "null"
    }

    private fun String.toClassName(): String = split(Regex("[_\\-.]")).joinToString("") { it.capitalizeFirst() }

    private fun String.toCamelCase(): String =
        split(Regex("[_\\-.]")).mapIndexed { index, part -> if (index == 0) part.lowercase() else part.capitalizeFirst() }
            .joinToString("")

    private fun String.capitalizeFirst(): String =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    companion object {
        private val STRING = ClassName("kotlin", "String")
        private val INT = ClassName("kotlin", "Int")
        private val BOOLEAN = ClassName("kotlin", "Boolean")
        private val DOUBLE = ClassName("kotlin", "Double")
        private val ANY = ClassName("kotlin", "Any")
        private val LIST = ClassName("kotlin.collections", "List")
    }
}
