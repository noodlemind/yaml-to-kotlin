import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.22"
    id("com.gradle.plugin-publish") version "1.2.1"
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.noodlemind"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.yaml:snakeyaml:2.0")
    implementation("com.squareup:kotlinpoet:1.14.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
    kotlinJavaToolchain.toolchain.use(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}


tasks.test {
    useJUnitPlatform()
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
            version = "1.0.0"
            from(components["java"])
        }
    }
}
