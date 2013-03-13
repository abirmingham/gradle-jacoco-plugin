package com.github.abirmingham.gradle.jacoco

class JacocoPluginExtension {
    List<String> excludes = new ArrayList<>()
    String jacocoVersion = '0.6.1.201212231917'
    String reportDir
    String tmpDir
}
