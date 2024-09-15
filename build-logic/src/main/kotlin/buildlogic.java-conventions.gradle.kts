plugins {
  java
}

repositories {
  mavenCentral()
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
    vendor.set(JvmVendorSpec.ADOPTIUM)
  }
}

tasks {
  compileJava {
    options.release = 8
  }
}
