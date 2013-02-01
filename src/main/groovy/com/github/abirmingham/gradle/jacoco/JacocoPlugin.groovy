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
            def antClasspath  = project.getBuildscript().getConfigurations().findByName('classpath').filter {
                it.getName().matches(/.*org\.jacoco.*/)
            }.getAsPath()

            def resolveAgentPath = { "${extension.tmpDir}/jacoco.exe" }

            doFirst {
                // Make jacoco.exe javaagent
                ant.taskdef(
                        name:      'jacocoagent',
                        classname: 'org.jacoco.ant.AgentTask',
                        classpath:  antClasspath,
                )

                ant.jacocoagent(
                        property:  'agentvmparam',
                        output:    'file',
                        destfile:   resolveAgentPath(),
                        append:     false,
                        dumponexit: true,
                )

                // Add jacoco.exe to jvmArgs
                jvmArgs "${ant.properties.agentvmparam}"
            }

            // Print report
            doLast {
                if (!new File(resolveAgentPath()).exists()) {
                    logger.info("Skipping Jacoco report for ${project.name}. The data file is missing. (Maybe no tests ran in this module?)")
                    logger.info('The data file was expected at ' + resolveAgentPath())
                    return null
                }

                ant.taskdef(name: 'jacocoreport', classname: 'org.jacoco.ant.ReportTask', classpath: antClasspath)
                new File(extension.reportDir).mkdirs()

                ant.jacocoreport {
                    executiondata {
                        ant.file file: resolveAgentPath()
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
