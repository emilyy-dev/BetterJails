# BetterJails


#### Resource in [spigotmc.org](https://www.spigotmc.org/resources/betterjails.76001/) & [dev.bukkit.org](https://dev.bukkit.org/projects/betterjails)

A simple jails system Bukkit plugin aimed for most Minecraft versions.

It lets you manage jails, jail offline players, and sentence time will be over when prisoners have
been online for the required time.


### Sections

* [Commands and permissions](#Commands-and-permissions)
* [Configuration](#Configuration)
* [Sub-commands execution](#Sub-commands-execution)
* [Developer API](#Developer-API)
* [Compiling](#Compiling)


## Commands and permissions


### Commands

* `/setjail <jail name>` / Adds a new jail location where the command is executed.
* `/deljail <jail name>` / Removes a jail location from the jails list.
* `/modjail <jail name> releaselocation set` / Sets the location where the command is executed to be the
  "release location" of the jail. This is where players will be put when they are released from that
  specific jail. This is optional. If not provided, the player will be released to wherever they were
  before imprisonment.
* `/modjail <jail name> releaselocation clear` / Clears the "release location" of the jail.
* `/jails` / Prints a list of available jails.
* `/jail <player> <jail> <time> [reason]` / Sends a player to the provided jail, and won't be teleported back
  until the time provided has passed. Time format matches inputs in the format of `2d15h7m12s`.
  The command optionally takes an imprisonment reason.
* `/jail info <player>` / Will print out in the chat some information about the jailed player stored
  in the player data file.
* `/unjail <player>` / Teleports a jailed player back to where they were when jailed.
* `/betterjails` / Prints the version of the plugin.
* `/betterjails reload` / Reloads files into memory.
* `/betterjails save` / Saves files from memory.


### Permissions

All permissions default to operators only unless otherwise noted.

* `betterjails.jail` / Lets the user execute the `/jail` command.
* `betterjails.jail.exempt` / The user with this permission cannot be jailed by others (unless
  offline, but it runs check when joining).
* `betterjails.jails` / Lets the user execute the `/jails` command.
* `betterjails.unjail` / Lets the user execute the `/unjail` command.
* `betterjails.setjail` / Lets the user execute the `/setjail` command.
* `betterjails.modjail` / Lets the user execute the `/modjail` command.
* `betterjails.deljail` / Lets the user execute the `/deljail` command.
* `betterjails.receivebroadcast` / Prints in the user's chat when a player has been jailed/unjailed.
* `betterjails.betterjails` / Lets the user execute the `/betterjails` command. Permission defaults
  to true for all users.
* `betterjails.betterjails.reload` / Lets the user execute the `/betterjails reload` command.
* `betterjails.betterjails.save` / Lets the user execute the `/betterjails save` command.


## Configuration

**backupLocation:**
Coordinates of an unjail backup location, just in case the prisoner's last location is found
corrupt.

___

**offlineTime:**
Whether the prisoner has to be online for sentence time to count.

Value: Boolean (`true` or `false`)

___

**changeGroup:**
Whether the player's group should be changed **when jailed**.

**Requires [Vault](https://dev.bukkit.org/projects/vault/files)!**

Value: Boolean (`true` or `false`)

___

**prisonerGroup:**
If `changeGroup` is set to `true`, the group name to which the player should be moved when jailed.

Value: String (group name)

___

**autoSaveTimeInMinutes:**
The time interval in minutes between each time when data files get saved. Set to 0 to disable the
autosaving feature.

Value: Integer (a whole number, c'mon...)

___

**messages:**
Various messages that pop up when doing commands.

___


## Sub-commands execution

**This is pretty straight-forward.**
All of this is inside `subcommands.yml`.

The commands under `as-prisoner` will be executed as if the prisoner performed them, and the ones
under `as-console` will be executed as if they were sent from the console.

All the commands under `on-jail` will be executed when the player is sent to jail; and the ones
under `on-release` will be executed when the player gets released from jail.

**All the commands will only be performed when the player is online. If they are jailed/released
while offline, they will be executed when they get online.**


## Developer API

BetterJails now ships with a developer API! Plugin developers can now interact with B.J., listen to
specific events, create jails, release prisoners and much more.

The [`BetterJails` interface](https://github.com/emilyy-dev/BetterJails/blob/v1/api/src/main/java/com/github/fefo/betterjails/api/BetterJails.java) is the heart of the API, in there you can access all the other interfaces you need to work with B.J.:
* A [`PrisonerManager`](https://github.com/emilyy-dev/BetterJails/blob/v1/api/src/main/java/com/github/fefo/betterjails/api/model/prisoner/PrisonerManager.java) in which you can retrieve prisoners, jail players and release prisoners
* A [`JailManager`](https://github.com/emilyy-dev/BetterJails/blob/v1/api/src/main/java/com/github/fefo/betterjails/api/model/jail/JailManager.java) in which you can create and delete jails
* An [`EventBus`](https://github.com/emilyy-dev/BetterJails/blob/v1/api/src/main/java/com/github/fefo/betterjails/api/event/EventBus.java) where you can subscribe (or "listen") to certain events dispatched throughout the functioning of the plugin.

You can get an instance of the `BetterJails` interface through the services manager as follows:

````java
public class MyPlugin extends JavaPlugin {

  private BetterJails betterJails;

  public BetterJails getBetterJails() {
    return this.betterJails;
  }

  @Override
  public void onEnable() {
    this.betterJails = getServer().getServicesManager().load(BetterJails.class);
  }
}
````

Don't forget to add `"BetterJails"` as `depend`/`softdepend` to your `plugin.yml` :)

The API is published in Maven Central Repository and snapshots are published to OSS Sonatype Snapshots repository.
Importing the BetterJails API to your project depends on how you build your plugin.


### Maven

If you are using Maven, all you have to do is add the dependency itself that will be pulled from Central
```xml
<dependency>
    <groupId>io.github.emilyy-dev</groupId>
    <artifactId>betterjails-api</artifactId>
    <version>1.5</version>
    <scope>provided</scope>
</dependency>
```


### Gradle

Same principle applies if you are using Gradle to build your plugin, but you need to specify the `mavenCentral()` repo:


##### Groovy DSL

```groovy
repositories {
    mavenCentral()
}

dependencies {
    compileOnly 'io.github.emilyy-dev:betterjails-api:1.5'
}
```


##### Kotlin DSL

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.emilyy-dev:betterjails-api:1.5")
}
```


### Manually

If you want to manually add the API dependency to your classpath, you can obtain the jar by [downloading it from here](https://repo1.maven.org/maven2/io/github/emilyy-dev/betterjails-api/1.5/).


## Compiling

You can compile this plugin by cloning the repository and running `./gradlew build` in the root
directory of the project, you can find the final jar in `./betterjails/build/libs/betterjails-1.5.jar`.
