package com.github.abirmingham.gradle.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.plugins.ReportingBasePlugin

class JacocoPlugin implements Plugin<Project> {

    static String NAME = "jacoco"
    IsolatedAntBuilder antBuilder

    @Override
    void apply(Project project) {
        JacocoPluginExtension extension = createExtension(project)

        instrumentTaskTestClasses(project, extension)

        // TBD execute output
    }

    JacocoPluginExtension createExtension(Project project) {
        project.plugins.apply(ReportingBasePlugin)

        return project.extensions.create(NAME, JacocoPluginExtension).with {
            reportDir = "${project.reporting.baseDir.absolutePath}/jacoco"
            tmpDir    = "${project.buildDir}/tmp/jacoco"
            return (JacocoPluginExtension) delegate
        }
    }

    void instrumentTaskTestClasses(Project project, JacocoPluginExtension extension) {
        // Ensure test tasks exist
        project.apply plugin: 'java'

        // Set jvmArgs
        // antBuilder.withClasspath() // TBD maybe try this?
        project.getTasks().findByName('test').with {

            ant.taskdef(
                    name:      'jacocoagent',
                    classname: 'org.jacoco.ant.AgentTask',
                    classpath:  project.getBuildscript().getConfigurations().findByName('classpath').getAsPath()
            )

            ant.jacocoagent(
                    property:  'agentvmparam',
                    destfile:  "${extension.tmpDir}/jacoco.exe",
                    output:    "file",
                    append:     false,
                    dumponexit: true
            )

            jvmArgs "${ant.properties.agentvmparam}"
        }
    }
}
