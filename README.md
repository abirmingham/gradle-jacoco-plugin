# Gradle plugin for Jacoco

This plugin provides a gradle interface for generating a jacoco report.

## Usage
```
buildscript {
    repositories {
        mavenCentral()
        maven {
            url uri('http://abirmingham.github.com/repository') // upload to mavenCentral() pending
        }
    }
    dependencies {
        classpath 'com.github.abirmingham:gradle-jacoco-plugin:1.0-SNAPSHOT'
    }
}

apply plugin: 'jacoco'
```

## Configuration
```
jacoco {
    // note that no configuration is required
    // the values shown here are the overrideable defaults
    tmpDir    = "${buildDir}/tmp/jacoco"
    reportDir = "${project.reporting.baseDir.absolutePath}/jacoco"
}
```

## View output
Assuming reportDir is unchanged:
```
open ./build/reports/jacoco/index.html
```


## Implementation Notes
Ant is used to instrument the test task (org.jacoco.ant.AgentTask), and to generate the report (org.jacoco.ant.ReportTask). Heavily influenced by [gschmidl/jacoco-gradle](http://github.com/gschmidl/jacoco-gradle).
