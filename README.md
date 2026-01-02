# FrostRTP

A lightweight, safe, and highly configurable Random Teleport (RTP) plugin for Spigot/Paper Minecraft servers.

## Features

- **Safe teleportation** - avoids lava, water, fire, cacti, and other dangerous blocks
- **Multi-world support** - define different RTP types for different worlds
- **Per-type cooldowns** with bypass permission (`rtp.bypass`)
- **Permission-based RTP types** (e.g., `/rtp nether`, `/rtp end`)
- **Configurable min/max radius** settings
- **"Near" mode** - teleport only if enough players are online in the target world
- **Full hex color support** (`&#RRGGBB`) in messages
- **In-game reload** with `/nrtp reload` (permission: `rtp.admin.reload`)
- **Async location search** - no TPS lag!

> **Note:** Uses `/nrtp` as the default command (to avoid conflicts). Change it in plugin.yml if needed.

## Installation

1. Drop the jar file into your plugins folder
2. Start/restart your server
3. Use `/nrtp [type]` in-game!

## Permissions

- `rtp.default`
- `rtp.nether`
- `rtp.bypass`
- `rtp.admin.reload`

**Made with ❤️ by MakimaDev** | Compatible with Paper 1.21+