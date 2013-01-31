package com.github.abirmingham.gradle.jacoco

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static junit.framework.TestCase.assertTrue
import static org.junit.Assert.assertNotNull

class JacocoPluginTest {

    Project project

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'jacoco'
    }

    @Test
    public void shouldAddExtension() {
        assertNotNull(project.extensions.getByName('jacoco'))
    }

    @Test
    public void shouldSetJvmArgs() {
        assertNotNull(project.test)

        //assertTrue(project.getTasks().findByName('test').jvmArgs.any { it.matches('.*jacoco\\.exe.*') })
        // e.g. -javaagent:/var/folders/vv/9vg9d_f975q9s_rl8gbdzbk00000gn/T/jacocoagent8666481667761165084.jar=destfile=/private/var/folders/vv/9vg9d_f975q9s_rl8gbdzbk00000gn/T/gradle40938656179684856projectDir/build/tmp/jacoco/jacoco.exe,append=false,dumponexit=true,output=file
    }
}
