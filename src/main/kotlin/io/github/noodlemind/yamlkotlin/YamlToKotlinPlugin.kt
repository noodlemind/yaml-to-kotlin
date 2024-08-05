package io.github.noodlemind.yamlkotlin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin for converting YAML schemas to Kotlin code.
 */
class YamlToKotlinPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create the extension for configuration
        val extension = project.extensions.create("yamlToKotlin", YamlToKotlinExtension::class.java)

        // Register the task
        project.tasks.register("generateKotlinFromYaml", YamlToKotlinTask::class.java) { task ->
            task.group = "code generation"
            task.description = "Generates Kotlin classes from YAML schema definitions"

            // Input and Output
            task.inputDir.set(extension.inputDir)
            task.outputDir.set(extension.outputDir)
            task.packageName = extension.packageName

            // Schema Configuration
            task.schemaFileExtension = extension.schemaFileExtension
            task.excludeFiles = extension.excludeFiles

            // Code Generation Options
            task.generateValidations = extension.generateValidations
            task.customValidationPackage = extension.customValidationPackage
            task.overwriteExistingFiles = extension.overwriteExistingFiles
            task.customTypeMapping = extension.customTypeMapping

            // Kotlin-specific Options
            task.generateDataClasses = extension.generateDataClasses
            task.generateSealedClasses = extension.generateSealedClasses
            task.addKotlinxSerializationAnnotations = extension.addKotlinxSerializationAnnotations

            // Additional Options
            task.configFile = extension.configFile

            // Logging and Debug
            task.verbose = extension.verbose
        }

        // Make sure the generate task runs before compilation, if the compileKotlin task exists
//        project.afterEvaluate {
//            project.tasks.findByName("compileKotlin")?.dependsOn("generateKotlinFromYaml")
//        }
    }
}
