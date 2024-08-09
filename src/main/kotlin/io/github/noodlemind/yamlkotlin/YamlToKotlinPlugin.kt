package io.github.noodlemind.yamlkotlin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The YamlToKotlinPlugin class is a plugin that generates Kotlin classes from YAML schema definitions. It implements the Plugin<Project> interface.
 *
 * @property apply The apply function overrides the apply function from the Plugin interface and is called when the plugin is applied to a project.
 * @param Project The Project object represents the project to which the plugin is applied.
 *
 * The apply function performs the following steps:
 * 1. Creates an extension named "yamlToKotlin" for configuring the plugin.
 * 2. Registers a task named "generateKotlinFromYaml" for generating Kotlin classes from YAML schema definitions.
 * 3. Configures the task with input and output directories, package name, schema file extension, excluded files, code generation options, Kotlin-specific options, additional options
 * , and logging and debug settings.
 * 4. Sets up a dependency between the generate task and the compileKotlin task if the compileKotlin task exists.
 */
class YamlToKotlinPlugin : Plugin<Project> {
    /**
     * Applies the YAML to Kotlin plugin to the given [project]. This plugin generates Kotlin classes from YAML schema definitions.
     *
     * @param project the project to which the plugin should be applied
     */
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

        // Make sure the generate task runs before compilation if the compileKotlin task exists
        project.afterEvaluate {
            project.tasks.findByName("compileKotlin")?.dependsOn("generateKotlinFromYaml")
        }
    }
}
