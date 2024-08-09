package io.github.noodlemind.yamlkotlin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject

/**
 * An extension class that provides configuration options for converting YAML schemas to Kotlin code.
 *
 * @property inputDir The directory containing the YAML schema files.
 * @property outputDir This directory contains the generated Kotlin code.
 * @property packageName The package name for the generated Kotlin classes.
 * @property schemaFileExtension The file extension for the YAML schema files.
 * @property excludeFiles A list of file names to be excluded during conversion.
 * @property generateValidations Indicates whether to generate validation code for the converted classes.
 * @property customValidationPackage The package name for custom validation classes.
 * @property overwriteExistingFiles Indicates whether to overwrite existing Kotlin files in the output directory.
 * @property customTypeMapping A mapping of custom type names in the YAML schema to corresponding Kotlin type names.
 * @property generateDataClasses Indicates whether to generate Kotlin data classes for the converted classes.
 * @property generateSealedClasses Indicates whether to generate Kotlin sealed classes for the converted classes.
 * @property addKotlinxSerializationAnnotations Indicates whether to add Kotlinx Serialization annotations to the converted classes.
 * @property configFile The configuration file for additional options.
 * @property verbose Indicates whether to enable verbose logging during the conversion process.
 *
 * @constructor Creates an instance of YamlToKotlinExtension.
 * @param objects An instance of the ObjectFactory class used for creating properties.
 *
 * @see YamlToKotlinPlugin
 * @see YamlToKotlinTask
 */
open class YamlToKotlinExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Represents the input directory for YAML schemas to be used in the conversion process.
     *
     * The input directory should contain the YAML schema files that need to be converted to Kotlin code.
     *
     * @property inputDir A [DirectoryProperty][org.gradle.api.file.DirectoryProperty] object representing the input directory.
     */
    val inputDir: DirectoryProperty = objects.directoryProperty()
    /**
     * Represents the directory where the generated outputs are stored.
     *
     * The output directory is used by the `generateKotlinFromYaml` task, which generates Kotlin classes from YAML schema definitions.
     * It is specified by setting the `outputDir` property of the `YamlToKotlinExtension` class.
     *
     * Example usage:
     * ```kotlin
     * val extension = project.extensions.create("yamlToKotlin", YamlToKotlinExtension::class.java)
     * extension.outputDir.set(File("path/to/output/directory"))
     * ```
     *
     * @see YamlToKotlinExtension
     */
    val outputDir: DirectoryProperty = objects.directoryProperty()
    /**
     * This variable represents the package name for the generated Kotlin code.
     *
     * It is a mutable property and its default value is "com.example.generated".
     *
     * This property is part of the [YamlToKotlinExtension] class, which is used for configuring the YAML to Kotlin code generation.
     *
     * The [YamlToKotlinPlugin] applies this configuration to a Gradle project, and the [generateKotlinFromYaml] task uses this property value for generating Kotlin classes from YAML
     *  schema definitions.
     *
     * To set a custom package name, assign a new value to this property.
     *
     * @see YamlToKotlinExtension
     * @see YamlToKotlinPlugin
     * @see generateKotlinFromYaml
     */
    var packageName: String = "com.example.generated"
    /**
     * The file extension for the schema file.
     */
    var schemaFileExtension: String = "yaml"
    /**
     * List of files to exclude from processing during YAML to Kotlin conversion.
     * These files are skipped and not included in the generated Kotlin code.
     */
    var excludeFiles: List<String> = emptyList()
    /**
     * A boolean flag indicating whether validations should be generated.
     * Validations are generated based on the YAML schema definitions.
     * If set to true, the generated Kotlin classes include validation code.
     * If set to false, the generated Kotlin classes does not include validation code.
     * By default, generateValidations is set to true.
     *
     * @see YamlToKotlinExtension
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinTask
     * @see YamlToKotlinPlugin.apply
     * @see YamlToKotlinPlugin.YamlToKotlinExtension.generateValidations
     * @see YamlToKotlinPlugin.YamlToKotlinTask.generateValidations
     */
    var generateValidations: Boolean = true
    /**
     * The customValidationPackage variable represents the package name where custom validation
     * classes and functions will be generated. It is a nullable String type that defaults to null.
     *
     * This variable is used in the YamlToKotlinExtension class to define the package where custom
     * validation code will be generated. It is also used in the YamlToKotlinPlugin class and the
     * apply function to pass the customValidationPackage value to the YamlToKotlinTask class for
     * code generation.
     *
     * @see YamlToKotlinExtension
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinTask
     */
    var customValidationPackage: String? = null
    /**
     * Specifies whether existing files should be overwritten during the code generation process.
     * If set to true, any existing generated files will be overwritten with new ones. If set to false,
     * existing files will be skipped and not overwritten.
     *
     * @property overwriteExistingFiles true if existing files should be overwritten, false otherwise
     *
     * @see YamlToKotlinExtension
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinTask
     */
    var overwriteExistingFiles: Boolean = true
    /**
     * A variable that represents a custom type mapping.
     *
     * This variable is a `Map` that maps a YAML schema type to its corresponding Kotlin type.
     * It is used in the context of converting YAML schemas to Kotlin code.
     * The keys of the map are strings representing the YAML schema types,
     * and the values are strings representing the corresponding Kotlin types.
     *
     * For example, if the YAML schema contains a type "integer" and we want to represent it as an `Int` in Kotlin,
     * we can add an entry to the `customTypeMapping` map with the key "integer" and the value "Int".
     *
     * By default, the `customTypeMapping` is initialized as an empty map (`emptyMap()`).
     * This means that no custom type mappings are defined by default,
     * and the YAML types will be converted to their default Kotlin types according to the conversion rules.
     *
     * Usage example:
     * ```kotlin
     * // Create the extension for configuration
     * val extension = project.extensions.create("yamlToKotlin", YamlToKotlinExtension::class.java)
     *
     * // Set a custom type mapping
     * extension.customTypeMapping = mapOf("integer" to "Int")
     * ```
     *
     * @see YamlToKotlinExtension
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinTask
     */
    var customTypeMapping: Map<String, String> = emptyMap()
    /**
     * Indicates whether or not to generate data classes during the conversion process.
     * Data classes are classes in Kotlin that are automatically generated to hold data, with
     * automatically implemented `equals()`, `hashCode()`, and `toString()` methods.
     * By default, data classes will be generated.
     *
     * @property generateDataClasses Indicates whether or not to generate data classes. Default is `true`.
     * @see YamlToKotlinExtension
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinTask
     */
    var generateDataClasses: Boolean = true
    /**
     * Indicates whether sealed classes should be generated during the conversion process.
     * Sealed classes are a feature in Kotlin that allows defining a closed hierarchy of subclasses.
     * When enabled, the YAML to Kotlin conversion process will generate sealed classes for appropriate data structures.
     * By default, sealed classes generation is disabled.
     *
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinExtension
     * @see YamlToKotlinTask
     *
     * @property generateSealedClasses Whether sealed classes should be generated
     */
    var generateSealedClasses: Boolean = false
    /**
     * This variable determines whether to add kotlinx.serialization annotations during the code generation process.
     * By default, the value is set to false.
     * If set to true, the generated Kotlin classes will include annotations from the kotlinx.serialization library,
     * which can be used for serialization of objects.
     *
     * @sample YamlToKotlinPlugin
     * @sample YamlToKotlinExtension
     * @sample YamlToKotlinTask
     *
     * @property addKotlinxSerializationAnnotations Determines whether to add kotlinx.serialization annotations during code generation.
     * @property addKotlinxSerializationAnnotations Its default value is false.
     *
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinExtension
     * @see YamlToKotlinTask
     */
    var addKotlinxSerializationAnnotations: Boolean = false
    /**
     * The `configFile` variable represents the configuration file used by the YamlToKotlinExtension class for generating Kotlin classes from YAML schema definitions.
     *
     * This variable is nullable and defaults to `null`. It should be set to the path of the configuration file to be used. If it isn't set or set to `null`, the extension
     *  uses default configuration values.
     *
     * Example usage in the `YamlToKotlinPlugin` class:
     * ```kotlin
     * extension.configFile = File("config.yaml")
     * ```
     *
     * Example usage in the `YamlToKotlinPlugin` function:
     * ```kotlin
     * task.configFile = extension.configFile
     * ```
     *
     * @see YamlToKotlinExtension
     * @see YamlToKotlinPlugin
     * @see YamlToKotlinTask
     */
    var configFile: File? = null
    /**
     * A boolean variable that determines whether to enable verbose logging.
     *
     * Setting this variable to true enables verbose logging, providing more detailed information during runtime.
     *
     * @property verbose The boolean value indicating whether verbose logging is enabled or not. The default value is `false`.
     *
     * @sample YamlToKotlinExtension
     * @sample YamlToKotlinPlugin
     * @sample apply
     */
    var verbose: Boolean = false
}