tasks.shadowJar {
    listOf(
        "net.kyori",
        "com.zaxxer.hikari",
        "org.bstats",
        "io.papermc.lib"
    ).forEach { relocate(it, "io.github.emilyydev.betterjails.lib.$it") }
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))

    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.destroystokyo.paper:paper-mojangapi:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }

    implementation("io.papermc:paperlib:1.0.6")
    implementation("org.bstats:bstats-bukkit:1.7")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
}
