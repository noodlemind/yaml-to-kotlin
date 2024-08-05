package io.github.noodlemind.yamlkotlin

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class YamlToKotlinPluginTest {
    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("io.github.noodlemind.yaml-to-kotlin")

        assertNotNull(project.tasks.findByName("generateKotlinFromYaml"))
    }
}
