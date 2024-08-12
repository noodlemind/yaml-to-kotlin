package io.github.noodlemind.yamlkotlin

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

/**
 * This class represents a task for generating Kotlin code from YAML schema files.
 *
 * @property inputDir The directory containing the YAML schema files to process.
 * @property outputDir The directory where the generated Kotlin files will be saved.
 * @property packageName The package name for the generated Kotlin files.
 * @property schemaFileExtension The file extension for the YAML schema files.
 * @property excludeFiles A list of file names to be excluded from processing.
 * @property generateValidations Flag indicating whether to generate validation classes for the data classes.
 * @property customValidationPackage The package name for custom validation classes.
 * @property overwriteExistingFiles Flag indicating whether to overwrite existing Kotlin files in the output directory.
 * @property customTypeMapping A map of custom type mappings for property types.
 * @property generateDataClasses Flag indicating whether to generate data classes.
 * @property generateSealedClasses Flag indicating whether to generate sealed classes.
 * @property addKotlinxSerializationAnnotations Flag indicating whether to add Kotlinx Serialization annotations to generated classes.
 * @property configFile The configuration file for the task.
 * @property verbose Flag indicating whether to enable verbose logging.
 * @property generateMainClass Flag indicating whether to generate a main class for the generated Kotlin code.
 * @property references A map of reference names to their corresponding type names.
 * @property anchorMap A map of anchor names to their corresponding type names.
 */
abstract class YamlToKotlinTask : DefaultTask() {
    /**
     * The directory property representing the input directory for the YAML to Kotlin conversion process.
     *
     * This property is abstract and must be overridden in a subclass. It specifies the directory where the YAML schema files are located.
     *
     * @property [inputDir] the directory property representing the input directory for the YAML to Kotlin conversion process
     * @see InputDirectory
     */
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    /**
     * Represents the directory where the generated output files will be written.
     *
     * This property is a [DirectoryProperty] that allows specifying a directory as the output destination for the generated Kotlin code from YAML schemas.
     * The directory must exist before generating the code, and it will be created if it doesn't exist.
     *
     * Example usage:
     * ```kotlin
     * val outputDir: DirectoryProperty = project.objects.directoryProperty()
     * task.outputDir.set(outputDir)
     * ```
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /**
     * The `packageName` variable specifies the package name for the generated Kotlin code.
     * It's a string variable that represents the package name in dot-separated format (e.g., "com.example.generated").
     *
     * Usage Example:
     * ```
     * @get:Input
     * var packageName: String = "com.example.generated"
     * ```
     *
     * Guidelines:
     * - Use this variable to define the package structure for the generated Kotlin classes.
     * - The package name should follow the standard naming conventions for packages in Kotlin.
     * - Avoid using reserved keywords or special characters in the package name.
     */
    @get:Input
    var packageName: String = "com.example.generated"

    /**
     * The file extension for schema files.
     *
     * @property schemaFileExtension The file extension for schema files. The default value is "yaml".
     */
    @get:Input
    var schemaFileExtension: String = "yaml"

    /**
     * The list of files to exclude from processing.
     *
     * This variable contains a list of file names that should be excluded from the processing of YAML files. These files will not be converted into Kotlin code.
     *
     * @property excludeFiles The list of file names to exclude.
     * @get The method to get the list of excluded files.
     * @set The method to set the list of excluded files.
     * @Input This annotation indicates that the value of this property can be configured from outside the task, such as through command-line arguments or build configuration files
     * .
     */
    @get:Input
    var excludeFiles: List<String> = emptyList()

    /**
     * A boolean variable indicating whether to generate validations.
     *
     * When set to true, validations will be generated during the code generation process.
     * When set to false, validations will not be generated.
     *
     * The default value is true.
     *
     * @get:Input - Indicates that this variable is an input property that can be configured by the user.
     */
    @get:Input
    var generateValidations: Boolean = true

    /**
     * The package name for custom validation classes. This variable specifies the package where the generated
     * custom validation classes will be placed. If not specified, the default package will be used.
     *
     * @property customValidationPackage The package name for custom validation classes.
     */
    @get:Input
    var customValidationPackage: String? = null

    /**
     * Indicates whether existing files should be overwritten during the generation process.
     *
     * If set to `true`, any existing files in the output directory will be overwritten with the
     * newly generated Kotlin classes. If set to `false`, the generator will skip generating
     * classes for any files that already exist in the output directory.
     *
     * By default, this property is set to `true`.
     *
     * @property overwriteExistingFiles `true` to overwrite existing files, `false` otherwise
     */
    @get:Input
    var overwriteExistingFiles: Boolean = true

    /**
     * The customTypeMapping variable is a property that represents a mapping between custom types in the YAML schema
     * and their corresponding Kotlin types. It is used in the YamlToKotlinTask to generate Kotlin classes from YAML schema definitions.
     *
     * The mapping is defined as a map where the keys represent the custom types in the YAML schema and the values represent
     * the corresponding Kotlin types. The custom types must be strings, while the Kotlin types can be any valid Kotlin type.
     *
     * Example:
     * ```
     * customTypeMapping = mapOf(
     *     "long" to "Long",
     *     "decimal" to "BigDecimal",
     *     "date" to "LocalDate"
     * )
     * ```
     *
     * By default, the customTypeMapping is an empty map.
     *
     * @property customTypeMapping the mapping between custom types in the YAML schema and their corresponding Kotlin types
     *
     * @since 1.0.0
     */
    @get:Input
    var customTypeMapping: Map<String, String> = emptyMap()

    /**
     * A boolean variable that specifies whether to generate data classes.
     *
     * Setting this variable to `true` will generate data classes based on the YAML schema definitions.
     * By default, this variable is set to `true`.
     *
     * @get:Input
     * @set:Optionality(OPTIONAL)
     */
    @get:Input
    var generateDataClasses: Boolean = true

    /**
     * Flag that determines whether sealed classes should be generated.
     *
     * Sealed classes are a feature in Kotlin that allow you to define a closed set of subclasses.
     * When this flag is set to `true`, the code generation process will include the generation of sealed classes based on the provided YAML schema definitions.
     * Sealed classes are useful when you need to work with a limited set of related classes with known types.
     *
     * @property generateSealedClasses Boolean value indicating whether sealed classes should be generated
     */
    @get:Input
    var generateSealedClasses: Boolean = false

    /**
     * Indicates whether Kotlinx Serialization annotations should be added to the generated Kotlin classes.
     *
     * By default, this variable is set to `false`, which means that no Kotlinx Serialization annotations will be added.
     * Setting it to `true` will add the necessary annotations from the Kotlinx Serialization library to the generated classes.
     *
     * @property addKotlinxSerializationAnnotations Indicates whether Kotlinx Serialization annotations should be added.
     */
    @get:Input
    var addKotlinxSerializationAnnotations: Boolean = false

    /**
     * Represents the configuration file used for the YAML to Kotlin generation process.
     *
     * @property configFile The file object representing the configuration file.
     *
     * @get:InputFile Indicates that the configuration file is an input file for the task.
     * This means that changes to the configuration file will trigger task re-execution.
     * If the configuration file is not specified, it defaults to null.
     *
     * @see YamlToKotlinTask
     * @see YamlToKotlinPlugin
     */
    @get:InputFile
    var configFile: File? = null

    /**
     * Determines whether verbose mode is enabled or not.
     *
     * When verbose mode is enabled, additional information will be logged during the execution of the program.
     *
     * @get:Input Indicates that this property can be configured through the Gradle build file.
     * @property verbose True if verbose mode is enabled, false otherwise.
     */
    @get:Input
    var verbose: Boolean = false

    /**
     * Boolean variable that controls whether the main class should be generated.
     *
     * By default, the value is set to `true`, indicating that the main class should be generated.
     * Set this variable to `false` if you don't want to generate the main class.
     *
     * @get:Input Indicates that this variable is an input property that can be configured externally.
     *            It can be accessed and modified during the build process.
     *
     * @property generateMainClass The value indicating whether the main class should be generated or not.
     */
    @get:Input
    var generateMainClass: Boolean = true

    /**
     * Mutable map that stores reference information.
     *
     * Key: String - The name of the reference anchor.
     * Value: ReferenceInfo - Information about the reference, including name, type, and optional properties.
     */
    private val references = mutableMapOf<String, ReferenceInfo>()

    /**
     * `anchorMap` is a mutable map that stores anchor names as keys and corresponding type names as values.
     * An anchor represents a reference to a specific point in the YAML schema.
     *
     * Usage Examples:
     * 1. Adding anchor and type to the map:
     *    anchorMap[key] = typeName
     * 2. Retrieving type name for an anchor:
     *    val typeName = anchorMap[key]
     *
     * @see YamlToKotlinTask.collectAnchors
     * @see YamlToKotlinTask.processCommonSection
     * @see YamlToKotlinTask.processDataClassesSection
     * @see YamlToKotlinTask.createNestedClass
     * @see YamlToKotlinTask.createProperty
     * @see YamlToKotlinTask.resolveReferences
     */
    private val anchorMap = mutableMapOf<String, TypeName>()

    /**
     * Represents the reference information for a specific element.
     *
     * @property name The name of the element.
     * @property type The type of the element.
     * @property properties The additional properties of the element, if any.
     */
    data class ReferenceInfo(
        val name: String, val type: String, val properties: Map<String, Any>? = null
    )

    /**
     * Generates Kotlin code from YAML schemas.
     *
     * This function reads YAML schemas from the input directory and generates corresponding Kotlin code in the output directory.
     * The package name for the generated code is specified by the `packageName` parameter.
     *
     * @throws Exception if an error occurs during the generation process.
     */
    @TaskAction
    fun generateKotlin() {
        logger.lifecycle("Generating Kotlin code from YAML schemas")
        logger.lifecycle("Input directory: ${inputDir.get().asFile.absolutePath}")
        logger.lifecycle("Output directory: ${outputDir.get().asFile.absolutePath}")
        logger.lifecycle("Package name: $packageName")

        val inputFiles = inputDir.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension == schemaFileExtension && it.name !in excludeFiles }.toList()

        logger.lifecycle("Found ${inputFiles.size} YAML files to process")

        val generatedFiles = inputFiles.flatMap { processYamlFile(it) }

        outputDir.get().asFile.mkdirs()

        logger.lifecycle("YAML to Kotlin generation completed")
        logger.lifecycle("Generated files:")
        generatedFiles.forEach { logger.lifecycle("- ${it.absolutePath}") }

        if (verbose) {
            generatedFiles.forEach { file ->
                logger.lifecycle("Content of ${file.name}:")
                logger.lifecycle(file.readText())
            }
        }
    }

    /**
     * Processes a YAML file and generates Kotlin files based on the content.
     *
     * @param file The YAML file to process.
     * @return A list of generated Kotlin files.
     */
    private fun processYamlFile(file: File): List<File> {
        val content: Map<String, Any> = file.readYamlContent()
        val outputDir = outputDir.get().asFile

        // Ensure the output directory exists
        outputDir.mkdirs()

        val components = content["Components"] as? Map<String, Any>
        val schemas = components?.get("Schemas") as? Map<String, Any> ?: emptyMap()

        collectAnchors(schemas)

        val generatedFiles = mutableListOf<File>()
        schemas.forEach { (name, schema) ->
            when {
                schema is Map<*, *> && schema["type"] == "string" && schema.containsKey("enum") -> {
                    generatedFiles.addAll(processEnumsSection(mapOf(name to schema), outputDir))
                }

                schema is Map<*, *> && schema["type"] == "object" -> {
                    generatedFiles.addAll(processDataClassesSection(mapOf(name to schema), outputDir))
                }

                schema is Map<*, *> -> {
                    // Handle simple types
                    val type = when (schema["type"]) {
                        "string" -> STRING
                        "integer" -> INT
                        "number" -> DOUBLE
                        "boolean" -> BOOLEAN
                        else -> ANY
                    }
                    val typeAlias = TypeAliasSpec.builder(name, type).build()
                    val fileSpec = FileSpec.builder(packageName, name).addTypeAlias(typeAlias).build()
                    val file = File(outputDir, "$name.kt")
                    file.parentFile.mkdirs() // Ensure parent directory exists
                    file.writeText(fileSpec.toString())
                    generatedFiles.add(file)
                }
            }
        }

        val validationFile = generateValidationClass(outputDir)
        val validateAnnotationFile = generateValidateAnnotation(outputDir)

        return generatedFiles + listOf(validationFile, validateAnnotationFile)
    }

    /**
     * Collects anchors from the given schemas and stores them in the anchorMap.
     * An anchor is collected for the following cases:
     * 1. If the value is a map with the key "type" set to "string" and contains the key "enum"
     * 2. If the value is a map with the key "type" set to "object"
     * 3. If the value is a map with any other type, in which case a basic type (STRING, INT, DOUBLE, BOOLEAN, or ANY) is assigned to the anchorMap.
     *
     * @param schemas The schemas to collect anchors from.
     */
    private fun collectAnchors(schemas: Map<String, Any>) {
        schemas.forEach { (key, value) ->
            when {
                value is Map<*, *> && value["type"] == "string" && value.containsKey("enum") -> {
                    anchorMap[key] = ClassName(packageName, key.toClassName())
                }

                value is Map<*, *> && value["type"] == "object" -> {
                    anchorMap[key] = ClassName(packageName, key.toClassName())
                }

                value is Map<*, *> -> {
                    // Handle simple types
                    val type = when (value["type"]) {
                        "string" -> STRING
                        "integer" -> INT
                        "number" -> DOUBLE
                        "boolean" -> BOOLEAN
                        else -> ANY
                    }
                    anchorMap[key] = type
                }
            }
        }
    }


    /**
     * Reads the content of a YAML file and returns it as a mapping of keys to values.
     *
     * @return A mapping of keys to values representing the content of the YAML file. If there is an error reading the file, an empty map is returned.
     */
    private fun File.readYamlContent(): Map<String, Any> {
        logger.lifecycle("Reading YAML content from file: $absolutePath")
        return try {
            val yaml = Yaml()
            yaml.load(readText()) as Map<String, Any>
        } catch (e: Exception) {
            logger.error("Error reading YAML content: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Collects references from the given content map.
     *
     * @param content the map of content to collect references from
     */
    private fun collectReferences(content: Map<String, Any>) {
        fun collectFromSection(section: Map<*, *>, type: String) {
            section.forEach { (key, value) ->
                if (value is Map<*, *> && value.containsKey("&")) {
                    val anchor = value["&"] as String
                    references[anchor] = ReferenceInfo(key.toString(), type, value as? Map<String, Any> ?: emptyMap())
                }
            }
        }

        (content["Enums"] as? Map<*, *>)?.let { collectFromSection(it, "enum") }
        (content["DataClasses"] as? Map<*, *>)?.let { collectFromSection(it, "object") }
    }

    /**
     * Processes the common section of the YAML content to generate a list of PropertySpec objects.
     *
     * @param commonContent The common section of the YAML content.
     * @return The list of PropertySpec objects generated from the common section.
     */
    private fun processCommonSection(commonContent: Any?): List<PropertySpec> {
        return when (commonContent) {
            is Map<*, *> -> commonContent.mapNotNull { (key, value) ->
                key?.toString()
                    ?.let { createProperty(it, value as? Map<String, Any> ?: emptyMap(), packageName, anchorMap) }
            }

            else -> emptyList()
        }
    }


    /**
     * Processes the enums section of the YAML content and generates corresponding enum Kotlin files.
     *
     * @param schemas The map of enums in the YAML content, where the key is the enum name and the value is the enum content.
     * @param outputDir The directory where the generated Kotlin files will be saved.
     * @return A list of generated enum Kotlin files.
     */
    private fun processEnumsSection(schemas: Map<String, Any>?, outputDir: File): List<File> {
        return schemas?.mapNotNull { (enumName, enumContent) ->
            if (enumContent is Map<*, *> && enumContent["type"] == "string" && enumContent.containsKey("enum")) {
                logger.lifecycle("Processing enum: $enumName")
                val enumValues = enumContent["enum"] as? List<*> ?: emptyList<Any>()
                val enumBuilder = TypeSpec.enumBuilder(enumName).apply {
                    enumValues.forEach { addEnumConstant(it.toString().uppercase()) }
                }
                val fileSpec = FileSpec.builder(packageName, enumName).addType(enumBuilder.build()).build()
                File(outputDir, "$enumName.kt").apply {
                    parentFile.mkdirs() // Ensure parent directory exists
                    writeText(fileSpec.toString())
                    logger.lifecycle("Generated enum file: $absolutePath")
                }
            } else null
        } ?: emptyList()
    }

    /**
     * Processes the data classes section of the YAML file and generates Kotlin data classes for each object schema.
     *
     * @param schemas The map of object schema names to their attributes.
     * @param outputDir The directory where the generated Kotlin files will be saved.
     * @return The list of generated Kotlin files.
     */
    private fun processDataClassesSection(schemas: Map<String, Any>?, outputDir: File): List<File> {
        val generatedFiles = mutableListOf<File>()
        schemas?.forEach { (className, attributes) ->
            if (attributes is Map<*, *> && attributes["type"] == "object") {
                val typeSpecBuilder = createClassBuilder(className)
                val propertiesSpec = attributes["properties"] as? Map<*, *>
                propertiesSpec?.forEach { (propertyName, propertyDefinition) ->
                    if (propertyDefinition is Map<*, *>) {
                        val resolvedDefinition = resolveReferences(propertyDefinition.toStringKeyMap())
                        val propertySpec =
                            createProperty(propertyName.toString(), resolvedDefinition, packageName, anchorMap)
                        typeSpecBuilder.addProperty(propertySpec)
                    }
                }
                val fileSpec = FileSpec.builder(packageName, className).addType(typeSpecBuilder.build())
                    .addImport("$packageName.Validate", "").addImport("kotlinx.serialization", "Serializable").build()
                val file = File(outputDir, "$className.kt")
                file.parentFile.mkdirs()
                file.writeText(fileSpec.toString())
                generatedFiles.add(file)
            }
        }
        return generatedFiles
    }

    /**
     * Creates a nested class with the given `className` and `attributes`.
     *
     * @param className the name of the nested class to create
     * @param attributes the attributes of the nested class, including properties
     * @return the created nested class represented as a `TypeSpec` object
     */
    private fun createNestedClass(className: String, attributes: Map<String, Any>): TypeSpec {
        val typeSpecBuilder = createClassBuilder(className)
        val propertiesSpec = attributes["properties"] as? Map<*, *>
        propertiesSpec?.forEach { (propertyName, propertyDefinition) ->
            if (propertyDefinition is Map<*, *>) {
                val resolvedDefinition = resolveReferences(propertyDefinition.toStringKeyMap())
                val propertySpec = createProperty(propertyName.toString(), resolvedDefinition, packageName, anchorMap)
                typeSpecBuilder.addProperty(propertySpec)
            }
        }
        return typeSpecBuilder.build()
    }

    /**
     * Generates a common file containing common properties.
     *
     * @param commonProperties a list of PropertySpec objects representing the common properties
     * @param outputDir the directory where the generated file will be written to
     * @return the generated common file
     */
    private fun generateCommonFile(commonProperties: List<PropertySpec>, outputDir: File): File {
        val className = "CommonProperties"
        val classBuilder = TypeSpec.classBuilder(className).addModifiers(KModifier.DATA).primaryConstructor(
            FunSpec.constructorBuilder().apply {
                commonProperties.forEach { addParameter(it.name, it.type) }
            }.build()
        )
        commonProperties.forEach { classBuilder.addProperty(it) }

        val fileSpec = FileSpec.builder(packageName, className).addType(classBuilder.build()).build()

        return File(outputDir, "$className.kt").apply {
            parentFile.mkdirs()
            writeText(fileSpec.toString())
        }
    }

    /**
     * Creates a TypeSpec.Builder for a class with the given className.
     *
     * @param className The name of the class to be created.
     * @return The TypeSpec.Builder for the created class.
     */
    private fun createClassBuilder(className: String): TypeSpec.Builder {
        return TypeSpec.classBuilder(className).addModifiers(KModifier.DATA)
            .addAnnotation(AnnotationSpec.builder(ClassName("kotlinx.serialization", "Serializable")).build())
            .primaryConstructor(FunSpec.constructorBuilder().build())
    }

    /**
     * Creates a `PropertySpec` object for a given property.
     *
     * @param key The name of the property.
     * @param propertyDefinition The definition of the property, represented as a map of key-value pairs.
     * @param packageName The name of the package where the property will reside.
     * @param references A map of type references used in the property definition. Defaults to an empty map.
     * @return The created `PropertySpec` object.
     * @throws IllegalArgumentException if an unsupported type or unknown reference is encountered.
     */
    private fun createProperty(
        key: String,
        propertyDefinition: Map<String, Any>,
        packageName: String,
        references: Map<String, TypeName> = emptyMap()
    ): PropertySpec {
        val propertyType = when {
            propertyDefinition.containsKey("\$ref") -> {
                val refPath = propertyDefinition["\$ref"] as String
                val refKey = refPath.split("/").last()
                anchorMap[refKey] ?: throw IllegalArgumentException("Unknown reference: $refPath")
            }

            propertyDefinition["type"] == "object" -> {
                ClassName(packageName, key.capitalize())
            }

            else -> when (val type = propertyDefinition["type"]) {
                "string" -> STRING
                "integer" -> INT
                "number" -> DOUBLE
                "boolean" -> BOOLEAN
                else -> references[type] ?: anchorMap[type] ?: throw IllegalArgumentException(
                    "Unsupported type: $type for property $key"
                )
            }
        }
        val isRequired = propertyDefinition["required"] as? Boolean == true
        val kotlinType = propertyType.copy(nullable = !isRequired)
        val propertyName = key.replaceFirstChar { it.lowercase() }
        val propertyBuilder =
            PropertySpec.builder(propertyName, kotlinType).initializer(propertyName).addModifiers(KModifier.OVERRIDE)

        // Handle validations
        val validate = propertyDefinition["validate"] as? List<Map<String, Any>>
        println("Validate for $key: $validate")
        if (validate != null) {
            val constraints = validate.mapNotNull { validation ->
                val pattern = validation["pattern"] as? String
                val value = validation["value"]
                println("Processing validation for $key: pattern=$pattern, value=$value")
                when (pattern) {
                    "isLetter" -> "isLetter()"
                    "isNumeric" -> "isNumeric()"
                    "minLength" -> "minLength($value)"
                    "maxLength" -> "maxLength($value)"
                    "regex" -> {
                        val escapedRegex = value.toString()
                            .replace("\\", "\\\\")  // Escape backslashes
                            .replace("\"", "\\\"")  // Escape double quotes
                            .replace("$", "\\$")    // Escape dollar signs
                        "regex(\"$escapedRegex\")"
                    }

                    else -> null
                }
            }
            println("Generated constraints for $key: $constraints")
            if (constraints.isNotEmpty()) {
                propertyBuilder.addAnnotation(
                    AnnotationSpec.builder(ClassName("", "Validate"))
                        .addMember(constraints.joinToString(", ") { "\"$it\"" }).build()
                )
                println("Added @Validate annotation for $key with constraints: ${constraints.joinToString(", ")}")
            }
        }

        return propertyBuilder.build()
    }

    /**
     * Converts a string to camel case by removing underscores and hyphens and capitalizing each word.
     *
     * @return The camel case version of the string.
     */
    private fun String.toCamelCase(): String {
        return split("_", "-").joinToString("") { it.capitalize() }.replaceFirstChar { it.lowercase() }
    }

    /**
     * Convert a string to a class name format.
     * Replaces underscores and hyphens with empty spaces and capitalizes each word.
     *
     * @return the converted class name
     */
    private fun String.toClassName(): String {
        return split("_", "-").joinToString("") { it.capitalize() }
    }

    /**
     * This method capitalizes the first letter of a string. If the first character is a lowercase letter,
     * it will be converted to uppercase. All other characters in the string will remain unchanged.
     *
     * @return the capitalized string
     */
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    /**
     * Resolves any references in the given definition map by replacing them with the actual referenced values.
     * If the definition does not contain any references, it is returned as is.
     * The references are resolved using the anchorMap, which contains mappings from anchor names to their corresponding types.
     *
     * @param definition The definition map to resolve references in.
     * @return The resolved definition map with references replaced, or the original definition if no references are found.
     */
    private fun resolveReferences(definition: Map<String, Any>): Map<String, Any> {
        return if (definition.containsKey("\$ref")) {
            val refPath = definition["\$ref"] as String
            val refKey = refPath.split("/").last()
            anchorMap[refKey]?.let {
                mapOf("type" to refKey)
            } ?: definition
        } else {
            definition
        }
    }

    /**
     * Generates a validation class for the given output directory.
     *
     * @param outputDir The directory where the validation class should be generated.
     * @return The generated validation class file.
     */
    private fun generateValidationClass(outputDir: File): File {
        val validationClass = TypeSpec.classBuilder("Validation").addModifiers(KModifier.PUBLIC).addType(
            TypeSpec.companionObjectBuilder().addFunction(
                FunSpec.builder("validate").addParameter("obj", ANY)
                    .returns(LIST.parameterizedBy(ClassName("", "ValidationError"))).addCode(
                        """
                            val errors = mutableListOf<ValidationError>()
                            obj::class.members.filterIsInstance<KProperty1<Any, *>>().forEach { property ->
                                property.findAnnotation<Validate>()?.let { annotation ->
                                    val value = property.get(obj)
                                    annotation.constraints.forEach { constraint ->
                                        when {
                                            constraint.startsWith("isLetter") -> {
                                                if (value is String && !value.all { it.isLetter() }) {
                                                    errors.add(ValidationError(property.name, "Must contain only letters"))
                                                }
                                            }
                                            constraint.startsWith("isNumeric") -> {
                                                if (value is String && !value.all { it.isDigit() }) {
                                                    errors.add(ValidationError(property.name, "Must contain only numeric characters"))
                                                }
                                            }
                                            constraint.startsWith("minLength") -> {
                                                val minLength = constraint.substringAfter("minLength(").substringBefore(")").toInt()
                                                if (value is String && value.length < minLength) {
                                                    errors.add(ValidationError(property.name, "Must be at least ${'$'}{minLength} characters long"))
                                                }
                                            }
                                            constraint.startsWith("maxLength") -> {
                                                val maxLength = constraint.substringAfter("maxLength(").substringBefore(")").toInt()
                                                if (value is String && value.length > maxLength) {
                                                    errors.add(ValidationError(property.name, "Must not exceed ${'$'}{maxLength} characters"))
                                                }
                                            }
                                            constraint.startsWith("regex") -> {
                                                val pattern = constraint.substringAfter("regex(\"").substringBefore("\")").toRegex()
                                                if (value is String && !pattern.matches(value)) {
                                                    errors.add(ValidationError(property.name, "Does not match the required pattern"))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            return errors
                            """.trimIndent()
                    ).build()
            ).build()
        ).build()

        val validationErrorClass =
            TypeSpec.classBuilder("ValidationError").addModifiers(KModifier.DATA).primaryConstructor(
                FunSpec.constructorBuilder().addParameter("property", String::class)
                    .addParameter("message", String::class).build()
            ).addProperty(PropertySpec.builder("property", String::class).initializer("property").build())
                .addProperty(PropertySpec.builder("message", String::class).initializer("message").build()).build()

        val fileSpec =
            FileSpec.builder(packageName, "Validation").addType(validationClass).addType(validationErrorClass)
                .addImport("kotlin.reflect", "KProperty1").addImport("kotlin.reflect.full", "findAnnotation").build()

        val file = File(outputDir, "Validation.kt")
        file.parentFile.mkdirs()
        file.writeText(fileSpec.toString())
        return file
    }


    /**
     * Generates a Kotlin annotation class `Validate` with the specified constraints.
     * The annotation is annotated with `@Target(AnnotationTarget.PROPERTY)` and `@Retention(AnnotationRetention.RUNTIME)`.
     * This method writes the generated annotation class to a file in the specified `outputDir`.
     *
     * @param outputDir The directory where the generated annotation class file will be written.
     * @return The generated annotation class file.
     */
    private fun generateValidateAnnotation(outputDir: File): File {
        val validateAnnotation =
            TypeSpec.annotationBuilder("Validate").addModifiers(KModifier.PUBLIC).primaryConstructor(
                FunSpec.constructorBuilder().addParameter(
                    ParameterSpec.builder("constraints", String::class).addModifiers(KModifier.VARARG).build()
                ).build()
            ).addProperty(
                PropertySpec.builder("constraints", Array<String>::class).initializer("constraints").build()
            ).addAnnotation(AnnotationSpec.builder(Target::class).addMember("AnnotationTarget.PROPERTY").build())
                .addAnnotation(
                    AnnotationSpec.builder(Retention::class).addMember("AnnotationRetention.RUNTIME").build()
                ).build()

        val fileSpec = FileSpec.builder(packageName, "Validate").addType(validateAnnotation).build()

        val file = File(outputDir, "Validate.kt")
        file.parentFile.mkdirs()
        file.writeText(fileSpec.toString())
        return file
    }

    /**
     * Converts a map with keys of any type and values of any type to a map with keys of type String
     * and values of type Any. Only entries with keys of type String and non-null values are included
     * in the resulting map.
     *
     * @return a map with keys of type String and values of type Any
     */
    private fun Map<*, *>.toStringKeyMap(): Map<String, Any> {
        return this.mapNotNull { (key, value) ->
            if (key is String && value != null) {
                key to value
            } else null
        }.toMap()
    }

    /**
     * The Companion class represents a collection of predefined Kotlin class names.
     * It provides convenient access to commonly used Kotlin classes.
     */
    companion object {
        /**
         * Represents a string of characters.
         *
         * This variable represents a string data type in Kotlin. Strings are immutable sequences of
         * characters and can be used to store text values.
         *
         * @property STRING The variable representing a string data type.
         */
        private val STRING: ClassName = ClassName("kotlin", "String")

        /**
         * Represents the `Int` data type in Kotlin.
         *
         * The `Int` data type is used to store integer values.
         *
         * @property INT The singleton instance of the `Int` class.
         */
        private val INT: ClassName = ClassName("kotlin", "Int")

        /**
         * Represents the Boolean data type in Kotlin.
         *
         * The `BOOLEAN` variable is a constant of type `ClassName` that represents the Boolean data type in Kotlin.
         * This variable is used to reference the Boolean type when defining properties or function parameters that require a Boolean value.
         * It provides a convenient way to access the Boolean type without having to manually specify the class name.
         *
         * Example usage:
         * ```
         * val myBoolean: BOOLEAN = true
         * ```
         *
         * @see ClassName
         */
        private val BOOLEAN: ClassName = ClassName("kotlin", "Boolean")

        /**
         * Represents the Kotlin `Double` type.
         *
         * The `Double` type is a 64-bit double-precision floating-point number in the range of approximately
         * -1.798e308 to 1.798e308, inclusive. It is a built-in primitive type in Kotlin.
         *
         * @property DOUBLE The constant value that represents the `Double` type.
         */
        private val DOUBLE: ClassName = ClassName("kotlin", "Double")

        /**
         * The `ANY` variable represents the Kotlin built-in class `kotlin.Any`.
         *
         * The `Any` class is the root of the Kotlin class hierarchy.
         * It is the superclass of all Kotlin classes.
         *
         * The `ANY` variable is used to assign the fully qualified name of the `kotlin.Any` class
         * to the `ClassName` object.
         */
        private val ANY: ClassName = ClassName("kotlin", "Any")

        /**
         * The LIST variable represents the Kotlin List class from the kotlin.collections package.
         *
         * @property LIST The List class represents an ordered collection of elements.
         * @see [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)
         */
        private val LIST: ClassName = ClassName("kotlin.collections", "List")
    }
}
