package com.github.abirmingham.gradle.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin

class JacocoPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        JacocoPluginExtension extension = createExtension(project)
        instrumentTestTask(project, extension)
    }

    JacocoPluginExtension createExtension(Project project) {
        project.plugins.apply(ReportingBasePlugin)

        return project.extensions.create('jacoco', JacocoPluginExtension).with {
            reportDir = "${project.reporting.baseDir.absolutePath}/jacoco"
            tmpDir    = "${project.buildDir}/tmp/jacoco"
            return (JacocoPluginExtension) delegate
        }
    }

    void instrumentTestTask(Project project, JacocoPluginExtension extension) {
        project.apply plugin: 'java' // ensure test tasks exist


        project.getTasks().findByName('test').with {
            // Can't fetch this yet because it will resolve (e.g. lockdown) the configuration
            def lazyAntClasspath = {
                project.getBuildscript().getConfigurations().findByName('classpath').plus(
                        project.getConfigurations().findByName('compile')
                ).getAsPath()
            }

            // Can't fetch this yet because user may override extension.tmpDir
            def lazyAgentPath = {
                "${extension.tmpDir}/jacoco.exec"
            }

            doFirst {
                // Make jacoco.exec javaagent
                ant.taskdef(
                        name:      'jacocoagent',
                        classname: 'org.jacoco.ant.AgentTask',
                        classpath:  lazyAntClasspath()
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
                        classpath:  lazyAntClasspath()
                )

                ant.jacocoreport {
                    executiondata {
                        ant.file file: lazyAgentPath()
                    }
                    structure(name: project.name) {
                        classfiles {
                            fileset dir: "${project.sourceSets.main.output.classesDir}"
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
