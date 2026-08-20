# Y-Corner GameMode

A lightweight [Paper](https://papermc.io/software/paper) plugin that assigns gamemodes by permission: trusted players join in **Creative**, everyone else joins in **Survival** and cannot switch to other modes.

Built for the [Y-Corner Minecraft Server](https://www.yoonhuh.com) to support a public Creative build world while keeping new or untrusted visitors in Survival.

## Features

- Sets gamemode automatically on **join**, **respawn**, and **plugin reload**
- Players with `ycorner.gamemode.creative` join in **Creative**
- All other players join in **Survival**
- Blocks gamemode changes for players without the creative permission (they cannot use `/gamemode`, `/gmc`, etc. to leave Survival)
- No config file — behavior is entirely permission-driven
- Works with [LuckPerms](https://luckperms.net/) (or any Bukkit permission plugin)

## Requirements

- **Paper** 1.21+ (tested on Paper 26.2)
- A permission plugin (LuckPerms recommended)
- **Important:** disable global forced gamemode in `server.properties`, or this plugin will fight it:

```properties
force-gamemode=false
gamemode=survival
```

If `force-gamemode=true` is set with `gamemode=creative`, the server will force Creative on every player regardless of permissions.

## Installation

1. Download or build `YCornerGamemode-1.2.0.jar`
2. Place it in your server's `plugins/` folder
3. Restart the server (do not use `/reload`)
4. Grant permissions (see below)

## Permissions

| Permission | Default | Description |
|---|---|---|
| `ycorner.gamemode.creative` | `false` | Player joins in Creative and may change gamemode freely |

Players **without** this permission:

- Join in Survival
- Respawn in Survival
- Cannot switch to Creative, Adventure, or Spectator (change events are cancelled)

Players **with** this permission:

- Join in Creative
- May use gamemode commands normally (subject to your other plugins, e.g. EssentialsX)

### Example: LuckPerms setup

Trusted builders get Creative; everyone else stays in the default group:

```bash
# Trusted group (your whitelist / admin builders)
/lp creategroup builder
/lp group builder permission set ycorner.gamemode.creative true

# Assign a player
/lp user <username> parent add builder
```

On the Y-Corner server, the `builder` group includes `ycorner.gamemode.creative` alongside WorldEdit and Essentials permissions. New public joiners remain in the `default` group and receive Survival only.

## How it works

```
Player joins or respawns
        │
        ▼
 Has ycorner.gamemode.creative?
        │
   yes ─┴─ no
    │       │
    ▼       ▼
Creative  Survival
    │       │
    │       └── Gamemode changes to non-Survival are blocked
    │
    └── Gamemode changes allowed (other plugins may still restrict commands)
```

The plugin registers three event handlers:

1. **`PlayerJoinEvent`** — applies the correct gamemode one tick after join (after other plugins finish setup)
2. **`PlayerRespawnEvent`** — reapplies gamemode after death respawn
3. **`PlayerGameModeChangeEvent`** — cancels unauthorized mode switches for players without the creative permission

On enable, gamemodes are also applied to any players already online.

## Building from source

### Prerequisites

- JDK 21 or newer (JDK 25 used in development)
- Paper API jar for your server version

If you run Paper locally, the API is usually already cached under:

```
libraries/io/papermc/paper/paper-api/<version>/paper-api-<version>.jar
```

### Compile (Windows PowerShell example)

```powershell
$api = "libraries\io\papermc\paper\paper-api\26.2.build.112-stable\paper-api-26.2.build.112-stable.jar"
$kyori = (Get-ChildItem "libraries\net\kyori" -Recurse -Filter "*.jar").FullName
$jetbrains = (Get-ChildItem "libraries\org\jetbrains" -Recurse -Filter "*.jar").FullName
$cp = (@($api) + $kyori + $jetbrains) -join ';'

$build = "build"
New-Item -ItemType Directory -Force -Path $build | Out-Null

javac -encoding UTF-8 -cp $cp -d $build `
  src/main/java/com/ycorner/gamemode/YCornerGamemode.java

Copy-Item src/main/resources/plugin.yml $build\plugin.yml
jar cf YCornerGamemode-1.2.0.jar -C $build .
```

### Compile (macOS / Linux)

```bash
API="libraries/io/papermc/paper/paper-api/26.2.build.112-stable/paper-api-26.2.build.112-stable.jar"
KYORI=$(find libraries/net/kyori -name '*.jar' | paste -sd: -)
JETBRAINS=$(find libraries/org/jetbrains -name '*.jar' | paste -sd: -)
CP="$API:$KYORI:$JETBRAINS"

mkdir -p build
javac -encoding UTF-8 -cp "$CP" -d build \
  src/main/java/com/ycorner/gamemode/YCornerGamemode.java

cp src/main/resources/plugin.yml build/plugin.yml
jar cf YCornerGamemode-1.2.0.jar -C build .
```

Copy the resulting JAR into `plugins/` and restart.

### Project layout

```
YCornerGamemode/
├── LICENSE
├── README.md
├── src/main/java/com/ycorner/gamemode/
│   └── YCornerGamemode.java
└── src/main/resources/
    └── plugin.yml
```

## Compatibility notes

- **EssentialsX** — compatible. Grant `essentials.gamemode.creative` separately if you want builders to use `/gmc`; YCornerGamemode only controls join/respawn enforcement and blocks unauthorized switches.
- **WorldGuard** — compatible. Region protection is independent of gamemode; Survival players still cannot build in protected regions unless they are region members.
- **OP status** — operators are not treated specially by this plugin. Grant `ycorner.gamemode.creative` explicitly, or rely on your permission plugin's OP handling.

## Commands & configuration

This plugin provides **no commands** and **no config file**. All behavior is controlled through the `ycorner.gamemode.creative` permission.

## Changelog

### 1.2.0

- Plugin is now **gamemode-only**. Command-block repair logic (`CommandBlockFixer`) was removed; it was unrelated to join/respawn gamemode enforcement.

### 1.1.0

- Initial public release: permission-based Creative vs Survival on join, respawn, and unauthorized gamemode changes.

## License

This project is licensed under the [Apache License 2.0](LICENSE).

## Contributing

Issues and pull requests are welcome on GitHub.

When contributing, please:

- Keep the plugin single-purpose and dependency-free
- Test on Paper with LuckPerms before submitting
- Match existing code style (minimal comments, clear event priorities)
