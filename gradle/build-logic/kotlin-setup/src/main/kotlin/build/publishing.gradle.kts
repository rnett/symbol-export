package build

import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("com.vanniktech.maven.publish")
}

extensionIfPresent<MavenPublishBaseExtension> {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), project.name, project.version.toString())

    pom {
        name = "Symbol Export"
        description = "A Gradle plugin and support library to export Kotlin symbols for use in code generators and compiler plugins."
        inceptionYear = "2025"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "rnett"
                name = "Ryan Nett"
                url = "https://github.com/rnett/"
            }
        }
        scm {
            url = "https://github.com/rnett/symbol-export/"
            connection = "scm:git:git://github.com/rnett/symbol-export.git"
            developerConnection = "scm:git:ssh://git@github.com/rnett/symbol-export.git"
        }
    }
}