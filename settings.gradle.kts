plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "betterjails"

includeBuild("build-logic")

include(":betterjails-api")
project(":betterjails-api").projectDir = file("api")
include(":betterjails")
