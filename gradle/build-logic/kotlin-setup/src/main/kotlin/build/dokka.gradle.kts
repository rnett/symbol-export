package build

plugins {
    id("org.jetbrains.dokka")
}

the<org.jetbrains.dokka.gradle.DokkaExtension>().apply {
    dokkaPublications.configureEach {
        suppressObviousFunctions = true
        suppressInheritedMembers = false

        val moduleFile = layout.projectDirectory.file("Module.md")
        if (moduleFile.asFile.exists())
            includes.from(moduleFile)
    }
    dokkaSourceSets.configureEach {
        suppressGeneratedFiles = true
        jdkVersion = 17
    }
}