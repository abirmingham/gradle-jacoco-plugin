package com.github.abirmingham.gradle.jacoco

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertNotNull

public class JacocoPluginTest {

    Project project

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'jacoco'
    }

    @Test
    public void shouldAddConfiguration() {
        assertNotNull project.configurations.getByName("jacoco")
    }
}
