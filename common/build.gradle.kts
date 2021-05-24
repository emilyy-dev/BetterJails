import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

dependencies {
    compileOnlyApi("net.luckperms:api:5.3")

    compileOnlyApi("commons-lang:commons-lang:2.6")
    compileOnlyApi("com.google.guava:guava:21.0")

    compileOnlyApi("org.yaml:snakeyaml:1.27")
    compileOnlyApi("com.google.code.gson:gson:2.8.0")
    compileOnlyApi("mysql:mysql-connector-java:8.0.25")
    api("com.zaxxer:HikariCP:4.0.3")

    compileOnlyApi("com.mojang:brigadier:1.0.17")
    api("net.kyori:adventure-api:4.7.0")
    compileOnlyApi("net.kyori:adventure-api:4.7.0")
    compileOnlyApi("net.kyori:adventure-text-serializer-gson:4.7.0")
    compileOnlyApi("net.kyori:adventure-text-serializer-plain:4.7.0")
    compileOnlyApi("net.kyori:adventure-platform-api:4.0.0-SNAPSHOT")

    api("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    compileOnlyApi("org.jetbrains:annotations:20.1.0")
}

tasks.processResources {
    val translationsFolder = rootProject.file("translations")
    val resourcesFolder = sourceSets.main.get().resources.srcDirs.firstOrNull() ?: return@processResources
    val destinationZip = File(resourcesFolder, "translations.zip")

    destinationZip.outputStream().use {
        ZipOutputStream(it).use { zip ->
            translationsFolder.listFiles()?.forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { translationFile -> translationFile.transferTo(zip) }
                zip.closeEntry()
            }
        }
    }
}
