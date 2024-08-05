package io.github.noodlemind.yamlkotlin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject

open class YamlToKotlinExtension @Inject constructor(objects: ObjectFactory) {
    // Input and Output
    val inputDir: DirectoryProperty = objects.directoryProperty()
    val outputDir: DirectoryProperty = objects.directoryProperty()
    var packageName: String = "com.example.generated"

    // Schema Configuration
    var schemaFileExtension: String = "yaml"
    var excludeFiles: List<String> = emptyList()

    // Code Generation Options
    var generateValidations: Boolean = true
    var customValidationPackage: String? = null
    var overwriteExistingFiles: Boolean = true
    var customTypeMapping: Map<String, String> = emptyMap()

    // Kotlin-specific Options
    var generateDataClasses: Boolean = true
    var generateSealedClasses: Boolean = false
    var addKotlinxSerializationAnnotations: Boolean = false

    // Additional Options
    var configFile: File? = null

    // Logging and Debug
    var verbose: Boolean = false
}
