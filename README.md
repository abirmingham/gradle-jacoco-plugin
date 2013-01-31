# Gradle plugin for Jacoco (Under Construction)

This plugin provides a gradle interface for generating a jacoco report.

## Usage
```
buildscript {
    repositories {
        mavenCentral()
        maven {
            url uri('http://github.com/abirmingham/gradle-jacoco-plugin/repository')
        }
    }
    dependencies {
        classpath 'com.github.abirmingham:gradle-jacoco-plugin:[VERSION]'
    }
}

apply plugin: 'jacoco'

jacoco {
    TBD
}
```

## Implementation Notes
Ant is used to instrument the test task (org.jacoco.ant.AgentTask), and to generate the report (org.jacoco.ant.ReportTask). Heavily influenced by [gschmidl/jacoco-gradle](http://github.com/gschmidl/jacoco-gradle).
