plugins {
  id("buildlogic.java-conventions")
  alias(libs.plugins.shadow)
  alias(libs.plugins.run.paper)
}

repositories {
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
  maven("https://repo.essentialsx.net/releases")
  maven("https://jitpack.io")

  // mockbukkit implements paper-api
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  implementation(project(":betterjails-api"))
  implementation(platform(libs.cloud.bom))
  implementation(platform(libs.cloud.minecraft.bom))
  implementation(libs.cloud.paper)
  implementation(libs.cloud.annotations)

  annotationProcessor(platform(libs.cloud.bom))
  annotationProcessor(libs.cloud.annotations)

  compileOnly(libs.spigot)
  implementation(libs.bstats)
  compileOnly(libs.luckperms)
  compileOnly(libs.vault)
  compileOnly(libs.essentialsx) { isTransitive = false }
  compileOnly(libs.annotations)
  implementation(libs.slf4j.api)
  runtimeOnly(libs.slf4j.impl)

  testImplementation(libs.junit)
  testRuntimeOnly(libs.junit.platform)
  testImplementation(libs.mockbukkit)
}

tasks {
  assemble {
    dependsOn(shadowJar)
  }

  jar {
    archiveClassifier = "noshadow"
  }

  shadowJar {
    archiveClassifier = null

    mergeServiceFiles()
    relocate("org.bstats", "io.github.emilyydev.betterjails.bstats")
    relocate("org.incendo.cloud", "io.github.emilyydev.betterjails.cloud")
    relocate("io.leangen.geantyref", "io.github.emilyydev.betterjails.geantyref")
    relocate("org.slf4j", "io.github.emilyydev.betterjails.slf4j")
  }

  withType<Jar>().configureEach {
    manifest.attributes["paperweight-mappings-namespace"] = "mojang"
  }

  compileJava {
    options.compilerArgs = listOf("-parameters")
  }

  processResources {
    val version = project.version
    inputs.property("version", version)
    filesMatching("plugin.yml") {
      expand("version" to version)
    }
  }

  compileTestJava {
    options.release = 17
  }

  test {
    useJUnitPlatform()
  }

  runServer {
    minecraftVersion("1.21.11")
    systemProperty("disable.watchdog", true)
  }
}

description = "betterjails"
