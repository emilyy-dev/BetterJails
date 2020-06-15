# BetterJails
#### Resource in [spigotmc.org](https://www.spigotmc.org/resources/betterjails.76001/) & [dev.bukkit.org](https://dev.bukkit.org/projects/betterjails)
A simple jails system Bukkit plugin aimed for most Minecraft versions.

It lets you manage jails, jail offline players, and sentence time will be over when prisoners have been online for the required time.

## Commands and permissions
#### Commands
- `/setjail <jail name>` / Adds a new jail location where the command is executed.
- `/deljail <jail name>` / Removes a jail location from the jails list.
- `/jails` / Prints a list of available jails.
- `/jail <player> <jail> <time>` / Sends a player to the provided jail, and won't be teleported back until the time provided has passed. Time format must match against this rule: `^(\d{1,10}(\.\d{1,2})?)[yMwdhms]$`. If all that sounds like gibberish, you can check [here](https://onlinetexttools.com/generate-text-from-regex?regex=%5E(%5Cd%7B1%2C5%7D(%5C.%5Cd%7B1%2C2%7D)%3F)%5ByMwdhms%5D%24&results=10) for some random examples of valid times.
- `/jail info <player>` / Will print out in the chat some information about the jailed player stored in the player data file.
- `/unjail <player>` / Teleports a jailed player back to where they were when jailed.
- `/betterjails` / Prints the version of the plugin.
- `/betterjails reload` / Reloads files into memory.
- `/betterjails save` / Saves files from memory.

#### Permissions
All permissions default to operators only unless otherwise noted.
- `betterjails.moderator` / Sets to `true` some permissions, and `false` a couple ones, specifically, sets to `false` `betterjails.setjail`, `betterjails.deljail`, `betterjails.betterjails.reload` and `betterjails.betterjails.save`. Defaulted to `false`.
- `betterjails.admin` / Sets to `true` all the permissions below one. Defaulted to `false`.
- `betterjails.jail` / Lets the user execute the `/jail` command.
- `betterjails.jail.exempt` / The user with this permission cannot be jailed by others (unless offline, but it runs check when joining).
- `betterjails.jails` / Lets the user execute the `/jails` command.
- `betterjails.unjail` / Lets the user execute the `/unjail` command.
- `betterjails.setjail` / Lets the user execute the `/setjail` command.
- `betterjails.deljail` / Lets the user execute the `/deljail` command.
- `betterjails.receivebroadcast` / Prints in the user's chat when a player has been jailed/unjailed.
- `betterjails.betterjails` / Lets the user execute the `/betterjails` command. Permission defaults to true for all users.
- `betterjails.betterjails.reload` / Lets the user execute the `/betterjails reload` command.
- `betterjails.betterjails.save` / Lets the user execute the `/betterjails save` command.

## Configurations
**backupLocation:**
Coordinates of an unjail backup location, just in case the prisoner's last location is found corrupt.
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
The time interval in minutes between each time when data files get saved. Set to 0 to disable the autosaving feature.

Value: Integer (a whole number, c'mon...)
___
**messages:**
Various messages that pop up when doing commands.
___
## Sub-commands execution
**This is pretty straight-forward.**
All of this is inside `subcommands.yml`.

The commands under `as-prisoner` will be executed as if the prisoner performed them, and the ones under `as-console` will be executed as if they were sent from the console.

All the commands under `on-jail` will be executed when the player is sent to jail; and the ones under `on-release` will be executed when the player gets released from jail.

**All the commands will only be performed when the player is online. If s/he is jailed/released while offline, they will be executed when s/he gets online.**
