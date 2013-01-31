package com.github.abirmingham.gradle.jacoco

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin

class JacocoPlugin implements Plugin<Project> {

    static String NAME = "jacoco"

    @Override
    void apply(Project project) {
        JacocoPluginExtension extension = createExtension(project)
        instrumentTestTask(project, extension)
    }

    JacocoPluginExtension createExtension(Project project) {
        project.plugins.apply(ReportingBasePlugin)

        return project.extensions.create(NAME, JacocoPluginExtension).with {
            reportDir = "${project.reporting.baseDir.absolutePath}/jacoco"
            tmpDir    = "${project.buildDir}/tmp/jacoco"
            return (JacocoPluginExtension) delegate
        }
    }

    void instrumentTestTask(Project project, JacocoPluginExtension extension) {
        // Ensure test tasks exist
        project.apply plugin: 'java'

        project.getTasks().findByName('test').with {
            def jacocoExe    = "${extension.tmpDir}/jacoco.exe"
            def antClasspath = project.getBuildscript().getConfigurations().findByName('classpath').getAsPath()

            // Set jvmArgs
            doFirst {
                ant.taskdef(
                        name:      'jacocoagent',
                        classname: 'org.jacoco.ant.AgentTask',
                        classpath:  antClasspath,
                )

                ant.jacocoagent(
                        property:  'agentvmparam',
                        output:    'file',
                        destfile:   jacocoExe,
                        append:     false,
                        dumponexit: true,
                )
                jvmArgs "${ant.properties.agentvmparam}"
            }

            // Print report
            doLast {
                if (!new File(jacocoExe).exists()) {
                    logger.info("Skipping Jacoco report for ${project.name}. The data file is missing. (Maybe no tests ran in this module?)")
                    logger.info("The data file was expected at $jacocoExe")
                    return null
                }

                ant.taskdef(name: 'jacocoreport', classname: 'org.jacoco.ant.ReportTask', classpath: antClasspath)
                new File(extension.reportDir).mkdirs()

                ant.jacocoreport {
                    executiondata {
                        ant.file file: "$jacocoExe"
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
