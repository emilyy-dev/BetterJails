import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

dependencies {
    compileOnlyApi("net.luckperms", "api", "5.3")

    compileOnlyApi("commons-lang", "commons-lang", "2.6")
    compileOnlyApi("com.google.guava", "guava", "21.0")

    compileOnlyApi("org.yaml", "snakeyaml", "1.27")
    compileOnlyApi("com.google.code.gson", "gson", "2.8.0")
    compileOnlyApi("mysql", "mysql-connector-java", "8.0.25")
    api("com.zaxxer", "HikariCP", "4.0.3")

    api("com.mojang", "brigadier", "1.0.18")
    api("net.kyori", "adventure-api", "4.8.0")
    compileOnlyApi("net.kyori", "adventure-api", "4.8.0")
    compileOnlyApi("net.kyori", "adventure-text-serializer-gson", "4.8.0")
    compileOnlyApi("net.kyori", "adventure-text-serializer-plain", "4.8.0")
    compileOnlyApi("net.kyori", "adventure-platform-api", "4.0.0-SNAPSHOT")

    api("org.slf4j", "slf4j-api", "1.7.30")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", "2.14.1")

    compileOnlyApi("org.jetbrains", "annotations", "21.0.1")
}

tasks.processResources {
    val translationsFolder = rootProject.file("translations")
    sourceSets.main.get().resources.srcDirs.forEach { resourcesFolder ->
        val destinationZip = File(resourcesFolder, "translations.zip")
        destinationZip.outputStream().use { dest ->
            ZipOutputStream(dest).use { zip ->
                translationsFolder.listFiles()?.forEach { file ->
                    zip.putNextEntry(ZipEntry(file.name))
                    file.inputStream().use { translationFile -> translationFile.transferTo(zip) }
                    zip.closeEntry()
                }
            }
        }
    }
}
