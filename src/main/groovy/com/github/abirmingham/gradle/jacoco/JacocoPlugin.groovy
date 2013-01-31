package com.github.abirmingham.gradle.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin

class JacocoPlugin implements Plugin<Project> {

    static String NAME = "jacoco"

    @Override
    void apply(Project project) {
        project.plugins.apply(ReportingBasePlugin)

        JacocoPluginExtension extension = createExtension(project)
//        Configuration configuration     = addConfiguration(project, extension)

        instrumentTaskTestClasses(project, extension)

        // TBD execute output
    }
//
//    Configuration addConfiguration(Project project, JacocoPluginExtension extension) {
//        def jacocoVersion = extension.version
//
//        return project.configurations.add(NAME).with {
//            visible     = false
//            transitive  = true
//            description = "The Jacoco libraries to be used for this project."
//
//            // Don't need these things, they're provided by the runtime
//            exclude group: 'ant',                 module: 'ant'
//            exclude group: 'org.apache.ant',      module: 'ant'
//            exclude group: 'org.apache.ant',      module: 'ant-launcher'
//            exclude group: 'org.codehaus.groovy', module: 'groovy'
//            exclude group: 'org.codehaus.groovy', module: 'groovy-all'
//            exclude group: 'org.slf4j',           module: 'slf4j-api'
//            exclude group: 'org.slf4j',           module: 'jcl-over-slf4j'
//            exclude group: 'org.slf4j',           module: 'log4j-over-slf4j'
//            exclude group: 'commons-logging',     module: 'commons-logging'
//            exclude group: 'log4j',               module: 'log4j'
//        }
//    }

    JacocoPluginExtension createExtension(Project project) {
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
        project.getTasks().findByName('test').with {
            ant.taskdef(name: 'jacocoagent', classname: 'org.jacoco.ant.AgentTask', classpath: project.configurations.getByName('compile').asPath)

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
