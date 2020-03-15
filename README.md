# BetterJails
### Resource in [SpigotMC](https://www.spigotmc.org/resources/betterjails.76001/)
A simple jails system Bukkit plugin aimed for most Minecraft versions.

It lets you manage jails, jail offline players, and sentence time will be over when prisoners have been online for the required time.

## Commands and permissions
#### Commands
- `/setjail <jail name>` / Adds a new jail location where the command is executed.
- `/deljail <jail name>` / Removes a jail location from the jails list.
- `/jails` / Prints a list of available jails.
- `/jail <player> <jail> <time>` / Sends a player to the provided jail, and won't be teleported back until they spend online the time provided.
- `/unjail <player>` / Teleports a jailed player back to where they were when jailed.
- `/betterjails` / Prints the version of the plugin.
- `/betterjails reload` / Reloads files into memory.
- `/betterjails save` / Saves files from memory.

#### Permissions
All permissions default to operators only.
- `betterjails.jail` / Lets the user execute the `/jail` command.
- `betterjails.jail.exempt` / The user with this permission won't be jailed by others (unless offline, but it runs check on join).
- `betterjails.jails` / Lets the user execute the `/jails` command.
- `betterjails.unjail` / Lets the user execute the `/unjail` command.
- `betterjails.setjail` / Lets the user execute the `/setjail` command.
- `betterjails.deljail` / Lets the user execute the `/deljail` command.
- `betterjails.receivebroadcast` / Prints in the user's chat when a player has been jailed.
- `betterjails.betterjails` / Lets the user execute the `/betterjails` command.
- `betterjails.betterjails.reload` / Lets the user reload the files into memory by doing `/betterjails reload`.
- `betterjails.betterjails.save` / Lets the user save the files from memory by doing `/betterjails save`.
