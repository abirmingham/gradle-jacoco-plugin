# Gradle plugin for Jacoco

This plugin provides a gradle interface for generating a jacoco report.

## Use
```
buildscript {
    dependencies {
        classpath 'com.github.abirmingham:gradle-jacoco-plugin:1.2'
    }
}

apply plugin: 'jacoco'
```

## Configure
```
jacoco {
    // note that no configuration is required
    tmpDir    = "${buildDir}/tmp/jacoco"
    reportDir = "${project.reporting.baseDir.absolutePath}/jacoco"
    excludes  = ["**/*Controller", "com/mycompany/util/MyUntestableClass"]
}
```

## Profit
Assuming reportDir is unchanged:
```
open ./build/reports/jacoco/index.html
```


## Notes
Ant is used to instrument the test task (org.jacoco.ant.AgentTask), and to generate the report (org.jacoco.ant.ReportTask). Heavily influenced by [gschmidl/jacoco-gradle](http://github.com/gschmidl/jacoco-gradle).
