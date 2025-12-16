# ModerationSMP

A comprehensive Minecraft Spigot/Paper moderation and gameplay plugin with advanced features.

## Features

### 🛡️ Moderation Tools
- **Jail System** - Temporarily jail players with automatic release
- **Freeze** - Freeze players in place
- **Vanish** - Become invisible to other players
- **NoClip** - Phase through blocks

### 🎮 Gameplay Features
- **Death Dimension** - Custom death world with puzzle challenges
  - Star Sequencer Puzzle
  - Parkour Puzzle
  - Resurrection system with player heads
- **Team System** - Create and manage player teams
- **Economy** - Full economy system with shops and auctions
- **Jobs System** - Miner, Lumberjack, Fisherman jobs
- **Home System** - Set and teleport to multiple homes
- **RTP** - Random teleportation

### 🔧 Admin Tools
- **X-ray Detection** - Automatic detection and alerts
- **Combat Log Prevention** - Combat tagging system
- **Rollback/Restore** - CoreProtect-style rollback system
- **Structure Manager** - Save and paste WorldEdit structures

### 📊 Other Features
- **Custom Scoreboard** - Dynamic player information
- **Skin Changer** - Change player skins
- **Authentication System** - Optional login system
- **God Mode** - Temporary invincibility

## Installation

1. Download the latest release from the [Releases](../../releases) page
2. Place the `.jar` file in your server's `plugins/` folder
3. Restart your server
4. Configure the plugin in `plugins/ModerationSMP/config.yml`

## Commands

### Moderation
- `/jail <player> <time> [reason]` - Jail a player
- `/unjail <player>` - Release a player from jail
- `/freeze <player>` - Freeze a player
- `/vanish` - Toggle vanish mode
- `/noclip` - Toggle noclip mode

### Gameplay
- `/team create <name>` - Create a team
- `/team invite <player>` - Invite a player to your team
- `/home [name]` - Teleport to home
- `/sethome [name]` - Set a home
- `/rtp` - Random teleport
- `/spawn` - Teleport to spawn

### Economy
- `/balance` - Check your balance
- `/pay <player> <amount>` - Pay another player
- `/shop` - Open the shop
- `/ah` - Auction house

### Admin
- `/deathdim <tp|setspawn|reset|info>` - Death dimension management
- `/co rollback` - Rollback changes
- `/structure save <name>` - Save a structure

## Building from Source

```bash
git clone https://github.com/YOUR_USERNAME/ModerationSMP.git
cd ModerationSMP
mvn clean package
```

The compiled `.jar` will be in the `target/` directory.

## Configuration

Edit `config.yml` to customize:
- Death dimension settings
- Economy settings
- Job rewards
- Shop prices
- And much more!

## Requirements

- **Minecraft Server:** Spigot/Paper 1.16+
- **Java:** 11 or higher
- **Dependencies:** WorldEdit (optional, for custom structures)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Credits

Developed by leoo9

## Support

For issues, questions, or suggestions, please open an issue on GitHub.
