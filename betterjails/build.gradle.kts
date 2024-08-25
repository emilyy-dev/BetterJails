plugins {
  id("buildlogic.java-conventions")
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("xyz.jpenilla.run-paper") version "2.2.3"
}

repositories {
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  maven("https://repo.essentialsx.net/releases")
  maven("https://jitpack.io")

  // mockbukkit implements paper-api
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  implementation(project(":betterjails-api"))

  compileOnly(libs.spigot)
  implementation(libs.bstats)
  compileOnly(libs.luckperms)
  compileOnly(libs.vault) { isTransitive = false }
  compileOnly(libs.essentialsx) { isTransitive = false }
  compileOnly(libs.annotations)

  testImplementation(libs.junit)
  testImplementation(libs.mockbukkit)
}

tasks {
  assemble {
    dependsOn(shadowJar)
  }

  jar { archiveClassifier = "noshadow" }
  shadowJar {
    archiveClassifier = null
    relocate("org.bstats", "io.github.emilyydev.betterjails.bstats")
  }

  processResources {
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
    minecraftVersion("1.20.4")
  }
}

description = "betterjails"
