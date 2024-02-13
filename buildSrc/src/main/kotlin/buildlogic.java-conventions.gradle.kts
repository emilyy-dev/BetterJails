plugins {
  java
}

repositories {
  mavenCentral()
}

group = "io.github.emilyy-dev"
version = "1.5-SNAPSHOT"

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
    vendor.set(JvmVendorSpec.ADOPTIUM)
  }
}

tasks {
  compileJava {
    options.release = 8
  }
}
