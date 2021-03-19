tasks.shadowJar {
    relocate("net.kyori", "com.github.fefo6644.betterjails.lib.kyori")
    relocate("org.bstats", "com.github.fefo6644.betterjails.lib.bstats")
    relocate("io.papermc.lib", "com.github.fefo6644.betterjails.lib.paperlib")
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation(project(":common"))

    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.10.9")

    implementation("io.papermc:paperlib:1.0.6")
    implementation("org.bstats:bstats-bukkit:1.7")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
}
