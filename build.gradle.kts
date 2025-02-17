import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask


plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradle.plugin-publish") version "1.2.1"
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.noodlemind"
version = "1.1.2"


repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")

    implementation("org.yaml:snakeyaml:2.0")
    implementation("com.squareup:kotlinpoet:1.18.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.24")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation(gradleTestKit())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.named("compileKotlin", KotlinCompilationTask::class.java) {
    compilerOptions {
        apiVersion.set(KOTLIN_2_0)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.withType<GeneratePluginDescriptors> {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

gradlePlugin {
    website.set("https://github.com/noodlemind/yaml-to-kotlin")
    vcsUrl.set("https://github.com/noodlemind/yaml-to-kotlin.git")
    plugins {
        create("yamlToKotlin") {
            id = "io.github.noodlemind.yaml-to-kotlin"
            implementationClass = "io.github.noodlemind.yamlkotlin.YamlToKotlinPlugin"
            displayName = "YAML to Kotlin Generator"
            description = "Generates Kotlin classes from YAML schema definitions"
            tags.set(listOf("yaml", "kotlin", "code-generation"))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/noodlemind/yaml-to-kotlin")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("gpr") {
            groupId = "io.github.noodlemind"
            artifactId = "yaml-to-kotlin"
            version = project.version.toString()

            pom {
                name.set("YAML to Kotlin Generator")
                description.set("Generates Kotlin classes from YAML schema definitions")
                url.set("https://github.com/noodlemind/yaml-to-kotlin")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("noodlemind")
                        name.set("Krish")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/noodlemind/yaml-to-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/noodlemind/yaml-to-kotlin.git")
                    url.set("https://github.com/noodlemind/yaml-to-kotlin")
                }
            }

            from(components["java"])

            pom.withXml {
                asNode().appendNode("packaging", "gradle-plugin")
            }
        }
    }
}
