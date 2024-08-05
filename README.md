# YAML to Kotlin Gradle Plugin

![GitHub license](https://img.shields.io/github/license/noodlemind/yaml-to-kotlin)
![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fio%2Fgithub%2Fnoodlemind%2Fyaml-to-kotlin%2Fmaven-metadata.xml&label=Plugin%20Portal)

Tired of writing Kotlin data classes by hand? Let YAML do the heavy lifting! This Gradle plugin automatically generates
type-safe Kotlin data classes, complete with validation, from your YAML schema definitions.

## Why YAML to Kotlin?

* Readability: YAML's clean syntax makes it easy to define complex data structures.
* Type Safety: Generated Kotlin data classes provide compile-time checks for data integrity.
* Less Boilerplate: Say goodbye to tedious, manual data class creation.
* Maintainability:  Keep your data models in sync with your YAML schemas effortlessly.

## Key Features

* Effortless Data Modeling: Craft your data models in human-readable YAML.
* Automatic Code Generation:  Generate Kotlin data classes with a single Gradle task.
* Basic Validation:  Start with built-in validation rules, or add your own.
* Customization: Configure output directories, package names, and more.
* Future-Proof: Stay tuned for upcoming features like advanced validation and Kotlinx.serialization support!

## Installation

```kotlin
plugins {
    id("io.github.noodlemind.yaml-to-kotlin") version "1.0.0"
}

```

## Usage

1. Place your YAML schema files in the `src/main/resources/schemas` directory.
2. Configure the plugin in your `build.gradle.kts`:
   ```kotlin
	kotlin yamlToKotlin { inputDir.set(file("src/main/resources/schemas")) outputDir.set(file("build/generated/source/yaml-to-kotlin")) packageName = "com.example.generated" }
	```
3. Run the Gradle task:
   ```commandline
   ./gradlew generateKotlinFromYaml
   ```

## YAML Schema Standards

Your YAML schemas should follow these standards:

| Section                          | Attribute    | Type             | Description                                   | Required                      |
|----------------------------------|--------------|------------------|-----------------------------------------------|-------------------------------|
| `types`                          | `base`       | string           | Base type (e.g., integer, string, boolean)    | Yes                           |
| `types`                          | `minimum`    | number           | Minimum value for numeric types               | No                            |
| `types`                          | `maximum`    | number           | Maximum value for numeric types               | No                            |
| `types`                          | `minLength`  | integer          | Minimum length for string types               | No                            |
| `types`                          | `maxLength`  | integer          | Maximum length for string types               | No                            |
| `types`                          | `pattern`    | string           | Regex pattern for string types                | No                            |
| `entities`                       | `properties` | object           | Map of property names to their definitions    | Yes                           |
| `entities.properties`            | `type`       | string           | Type of the property (can be custom type)     | Yes                           |
| `entities.properties`            | `required`   | boolean          | Whether the property is required              | No                            |
| `entities.properties`            | `references` | object           | For defining relationships between entities   | No                            |
| `entities.properties.references` | `entity`     | string           | Name of the referenced entity                 | Yes (if `references` is used) |
| `entities.properties.references` | `property`   | string           | Name of the property in the referenced entity | Yes (if `references` is used) |
| `validations`                    | `name`       | string           | Name of the custom validation                 | Yes                           |
| `validations`                    | `params`     | array of strings | Parameters for the custom validation          | No                            |

## Configuration Options

| Option                               | Description                            | Default                                 |
|--------------------------------------|----------------------------------------|-----------------------------------------|
| `inputDir`                           | Directory containing YAML schema files | `src/main/resources/schemas`            |
| `outputDir`                          | Directory for generated Kotlin files   | `build/generated/source/yaml-to-kotlin` |
| `packageName`                        | Package name for generated classes     | `com.example.generated`                 |
| `schemaFileExtension`                | File extension for YAML schemas        | `yaml`                                  |
| `generateValidations`                | Generate validation code               | `true`                                  |
| `generateDataClasses`                | Generate data classes                  | `true`                                  |
| `generateSealedClasses`              | Generate sealed classes                | `false`                                 |
| `addKotlinxSerializationAnnotations` | Add kotlinx.serialization annotations  | `false`                                 |

## Examples

#### Employee Data Class

```yaml
types:
  EmployeeID:
    base: integer
    minimum: 1

  NonEmptyString:
    base: string
    minLength: 1

  Email:
    base: string
    pattern: "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"

entities:
  Employee:
    properties:
      id:
        type: EmployeeID
        required: true
      name:
        type: NonEmptyString
        required: true
      email:
        type: Email
        required: true
      department:
        type: DepartmentID
        required: true
        references:
          entity: Department
          property: id

validations:
  - name: range
    params:
      - min
      - max
  - name: emailFormat
```

#### Department Data Class

```yaml
types:
  DepartmentID:
    base: integer
    minimum: 1

  NonEmptyString:
    base: string
    minLength: 1

entities:
  Department:
    properties:
      id:
        type: DepartmentID
        required: true
      name:
        type: NonEmptyString
        required: true
      employees:
        type: array
        itemType: EmployeeID
        references:
          entity: Employee
          property: id

validations:
  - name: uniqueDepartmentName
```

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for more details.

## License

This project is licensed under the MIT License—see the [LICENSE](LICENSE) file for details.

Let's write cleaner, safer, and more maintainable Kotlin code together.
