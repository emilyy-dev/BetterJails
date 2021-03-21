defaultTasks("clean", "licenseMain", "shadowJar")

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.github.hierynomus.license-base") version "0.15.0"
}

subprojects {

    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.github.hierynomus.license-base")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    license {
        header = rootProject.file("header.txt")
        encoding = "UTF-8"
        mapping("java", "DOUBLESLASH_STYLE")
        include("**/*.java")
    }

    group = "com.github.fefo6644.betterjails"
    version = "2.0-SNAPSHOT"

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }

        processResources {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE

            from(sourceSets.main.get().resources.srcDirs) {
                expand("pluginVersion" to version)
            }
        }
    }

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://jitpack.io")
        maven("https://libraries.minecraft.net")
        maven("https://repo.codemc.org/repository/maven-public")
    }
}
