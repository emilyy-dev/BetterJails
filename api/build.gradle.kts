plugins {
  id("buildlogic.java-conventions")
  `java-library`
  `maven-publish`
  signing
}

repositories {
  maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  compileOnly(libs.spigot)
  compileOnly(libs.annotations)
}

description = "betterjails-api"

java {
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        url = "https://github.com/emilyy-dev/BetterJails"

        licenses {
          license {
            name = "MIT"
            url = "https://opensource.org/licenses/MIT"
          }
        }

        developers {
          developer {
            id = "emilyy-dev"
            name = "Emilia López"
            email = "emilia.lopezf.1999@gmail.com"
            url = "https://github.com/emilyy-dev"
          }
        }

        scm {
          connection = "scm:git:git://github.com/emilyy-dev/BetterJails.git"
          developerConnection = "scm:git:ssh://github.com/emilyy-dev/BetterJails.git"
          url = "https://github.com/emilyy-dev/BetterJails/tree/v1"
        }
      }
    }
  }

  repositories {
    val username = findProperty("ossrh.user") as? String ?: return@repositories
    val password = findProperty("ossrh.password") as? String ?: return@repositories

    val repo =
        if (version.toString().endsWith("-SNAPSHOT")) {
          maven {
            name = "sonatype-staging"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
          }
        } else {
          maven {
            name = "sonatype-snapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
          }
        }

    repo.credentials {
      this.username = username
      this.password = password
    }
  }
}

signing {
  sign(publishing.publications["maven"])
}
