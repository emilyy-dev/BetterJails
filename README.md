# BetterJails
A simple jails system Bukkit plugin for aimed at Minecraft 1.13 and later versions (works with older versions as well).

It lets you manage jails, jail offline players, and sentence time will be over when prisoners have been online for the required time.

## Commands and permissions
#### Commands
- `/setjail <jail name>` / Adds a new jail location in where the command is executed.
- `/deljail <jail name>` / Removes a jail location from the jails list.
- `/jails` / Prints a list of available jails.
- `/jail <player> <jail> <time>` / Sends a player to the provided jail, and won't be teleported back until they spend online the time provided.
- `/unjail <player>` / Teleports a jailed player back to where they were when jailed.
- `/betterjails` / Shows the version of the plugin.

#### Permissions
All permissions default to operators only.
- `betterjails.jail` / Lets the user execute the `/jail` command.
- `betterjails.jail.exempt` / The user with this permission won't be jailed by others (unless offline, but they don't get sent to jail).
- `betterjails.jails` / Lets the user execute the `/jails` command.
- `betterjails.unjail` / Lets the user execute the `/unjail` command.
- `betterjails.setjail` / Lets the user execute the `/setjail` command.
- `betterjails.deljail` / Lets the user execute the `/deljail` command.
- `betterjails.receivebroadcast` / Prints in the user's chat when a player has been jailed.
- `betterjails.betterjails` / Lets the user execute the `/betterjails` command.
