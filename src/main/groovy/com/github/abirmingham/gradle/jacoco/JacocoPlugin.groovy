package com.github.abirmingham.gradle.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.artifacts.Configuration

class JacocoPlugin implements Plugin<Project> {

    Configuration antConfiguration
    JacocoPluginExtension extension

    @Override
    void apply(Project project) {
        createExtension(project)
        createAntConfiguration(project)
        instrumentTestTask(project)
    }

    void createAntConfiguration(Project project) {
        antConfiguration = project.configurations.add('jacoco').with {
            visible = false
            transitive = true
            description = "The jacoco libraries to be used for this project."
            // Don't need these things, they're provided by the runtime
            exclude group: 'ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant-launcher'
            exclude group: 'org.codehaus.groovy', module: 'groovy'
            exclude group: 'org.codehaus.groovy', module: 'groovy-all'
            exclude group: 'org.slf4j', module: 'slf4j-api'
            exclude group: 'org.slf4j', module: 'jcl-over-slf4j'
            exclude group: 'org.slf4j', module: 'log4j-over-slf4j'
            exclude group: 'commons-logging', module: 'commons-logging'
            exclude group: 'log4j', module: 'log4j'
            return (Configuration) delegate
        }
    }

    void createExtension(Project project) {
        project.plugins.apply(ReportingBasePlugin)

        extension = project.extensions.create('jacoco', JacocoPluginExtension).with {
            reportDir = "${project.reporting.baseDir.absolutePath}/jacoco"
            tmpDir    = "${project.buildDir}/tmp/jacoco"
            return (JacocoPluginExtension) delegate
        }
    }

    void instrumentTestTask(Project project) {
        project.apply plugin: 'java' // ensure test tasks exist

        project.getTasks().findByName('test').with {
            def lazyAgentPath = { // Can't fetch this yet because user may override extension.tmpDir
                "${extension.tmpDir}/jacoco.exec"
            }

            doFirst {
                project.dependencies {
                    jacoco "org.jacoco:org.jacoco.agent:${extension.jacocoVersion}"
                    jacoco "org.jacoco:org.jacoco.ant:${extension.jacocoVersion}"
                }

                // Make jacoco.exec javaagent
                ant.taskdef(
                        name:      'jacocoagent',
                        classname: 'org.jacoco.ant.AgentTask',
                        classpath:  antConfiguration.getAsPath()
                )

                ant.jacocoagent(
                        property:  'agentvmparam',
                        output:    'file',
                        destfile:   lazyAgentPath(),
                        append:     false,
                        dumponexit: true,
                )

                // Add jacoco.exec to jvmArgs
                jvmArgs "${ant.properties.agentvmparam}"
            }

            // Print report
            doLast {
                if (!new File(lazyAgentPath()).exists()) {
                    logger.info("Skipping Jacoco report for ${project.name}. The data file is missing. (Maybe no tests ran in this module?)")
                    logger.info('The data file was expected at ' + lazyAgentPath())
                    return null
                }

                new File(extension.reportDir).mkdirs()

                ant.taskdef(
                        name:      'jacocoreport',
                        classname: 'org.jacoco.ant.ReportTask',
                        classpath:  antConfiguration.getAsPath()
                )

                def excludeString = extension.excludes
                        .collect({ it.matches(".*\\.\\w+\$") ? it : it + ".class" })
                        .join(' ')

                ant.jacocoreport {
                    executiondata {
                        ant.file file: lazyAgentPath()
                    }
                    structure(name: project.name) {
                        classfiles {
                            fileset dir: "${project.sourceSets.main.output.classesDir}", excludes: excludeString
                        }
                        sourcefiles {
                            project.sourceSets.main.java.srcDirs.each {
                                fileset(dir: it.absolutePath)
                            }
                        }
                    }
                    xml destfile: "${extension.reportDir}/jacoco.xml"
                    html destdir: "${extension.reportDir}"
                }
            }
        }
    }
}
