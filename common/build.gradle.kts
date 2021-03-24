dependencies {
    compileOnlyApi("net.luckperms:api:5.3")

    compileOnlyApi("commons-lang:commons-lang:2.6")
    compileOnlyApi("com.google.guava:guava:30.1-jre")

    compileOnlyApi("org.yaml:snakeyaml:1.28")
    compileOnlyApi("com.google.code.gson:gson:2.8.6")
    compileOnlyApi("mysql:mysql-connector-java:5.1.49")
    api("com.zaxxer:HikariCP:3.4.5")

    compileOnlyApi("com.mojang:brigadier:1.0.17")
    api("net.kyori:adventure-api:4.7.0")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.7.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.7.0")
    compileOnly("net.kyori:adventure-platform-api:4.0.0-SNAPSHOT")

    api("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    compileOnlyApi("org.jetbrains:annotations:20.1.0")
}
