# SemiHardcore-Plugins

A comprehensive semi-hardcore survival plugin for Minecraft Spigot/Paper servers, combining death challenges, team mechanics, economy, and moderation tools.

## 🌟 Overview

SemiHardcore-Plugins transforms your Minecraft server into a challenging yet fair survival experience. When players die, they're sent to a custom Death Dimension where they must complete puzzles to escape. Teammates can also revive fallen players through a special resurrection ritual.

## ✨ Key Features

### 💀 Semi-Hardcore Death System
- **Death Dimension** - Custom world where players are sent when they die
- **Star Sequencer Puzzle** - Pattern recognition challenge (Stage 1)
- **Parkour Challenge** - Platforming course with checkpoints (Stage 2)
- **Resurrection Ritual** - Teammates can revive players using their head + rare items
- **Ghost Mode** - Spectate the living world while waiting for revival
- **Keep Inventory** - Optionally keep items on death (configurable)

### 👥 Team System
- Create and manage teams with custom names
- Colored nametags and scoreboard integration
- Team homes and shared resurrection mechanics
- Team chat and member management
- Friendly fire toggle

### 💰 Economy & Jobs
- **Full Economy** - Balance system with pay, shops, and transactions
- **Shop System** - Buy/sell items with custom GUI categories
- **Auction House** - Player-to-player item trading
- **Jobs System** - Miner, Lumberjack, Fisherman with XP progression
- **Configurable Prices** - Customize all shop prices and job rewards

### 🛡️ Moderation Tools
- **Jail System** - Temporary imprisonment with auto-release timer
- **Freeze** - Freeze players in place
- **Vanish** - Become invisible (normal and super vanish with flight)
- **NoClip** - Phase through blocks
- **God Mode** - Temporary invincibility
- **Anti-X-ray** - Automatic detection with configurable punishment

### 🔧 Admin Features
- **Rollback/Restore** - CoreProtect-style block logging and restoration
- **Structure Manager** - Save/paste WorldEdit structures for custom puzzles
- **Teleport Suite** - TPA, spawn, back, and admin teleports
- **Dynamic Scoreboard** - Show team, balance, world, and player count
- **Combat Tag** - Prevent combat logging

### 🏠 Quality of Life
- **Home System** - Set and teleport to multiple homes
- **Random Teleport (RTP)** - Safe random location finder
- **Spawn Command** - Teleport to spawn with delay
- **Skin Changer** - Change player skins via Mojang API
- **Auth System** - Optional login/register protection

## 📋 Requirements

- **Minecraft Version:** 1.16+ (Spigot or Paper)
- **Java Version:** 11 or higher
- **Optional Dependencies:**
  - WorldEdit (for custom puzzle structures)

## 📥 Installation

1. Download the latest release from [Releases](https://github.com/leoo955/SemiHardcore-Plugins/releases)
2. Place `SemiHardcore-Plugins-X.X.X.jar` in your `plugins/` folder
3. Restart your server
4. Configure the plugin in `plugins/ModerationSMP/config.yml`
5. Reload with `/reload confirm` or restart again

## ⚙️ Configuration

The `config.yml` file allows you to customize:

- **Death Dimension**
  - Enable/disable the death system
  - Keep inventory on death
  - Custom spawn location
  - Puzzle difficulty settings
  
- **Economy**
  - Starting balance for new players
  - Currency symbol
  - Shop prices and categories
  
- **Jobs**
  - XP rates per action
  - Rewards and level progression
  
- **Anti-Xray**
  - Sensitivity levels
  - Auto-punishment (kick/ban)
  
- **Teams**
  - Max team size
  - Friendly fire settings

## 🎮 Commands

### Player Commands
| Command | Description |
|---------|-------------|
| `/team create <name>` | Create a new team |
| `/team invite <player>` | Invite player to your team |
| `/team join <team>` | Join a team you were invited to |
| `/team leave` | Leave your current team |
| `/team home` | Teleport to team home |
| `/home [name]` | Teleport to home |
| `/sethome [name]` | Set a home location |
| `/delhome <name>` | Delete a home |
| `/spawn` | Teleport to spawn (3s delay) |
| `/rtp` | Random teleport |
| `/balance` | Check your balance |
| `/pay <player> <amount>` | Pay another player |
| `/shop` | Open the shop |
| `/ah` | Open auction house |
| `/jobs` | Open jobs menu |
| `/revive` | Open resurrection GUI |

### Admin Commands
| Command | Description |
|---------|-------------|
| `/jail <player> <time> [reason]` | Jail a player |
| `/unjail <player>` | Release from jail |
| `/freeze <player>` | Freeze a player |
| `/vanish` | Toggle vanish mode |
| `/noclip` | Toggle noclip |
| `/god [player]` | Toggle god mode |
| `/deathdim tp <player>` | Teleport to death world |
| `/deathdim setspawn` | Set death spawn |
| `/deathdim reset <player>` | Reset player's puzzle |
| `/co rollback` | Rollback block changes |
| `/structure save <name>` | Save WorldEdit structure |
| `/xray <player>` | View xray stats |

## 🏗️ Building from Source

```bash
# Clone the repository
git clone https://github.com/leoo955/SemiHardcore-Plugins.git
cd SemiHardcore-Plugins

# Build with Maven
mvn clean package

# Find the compiled plugin
cd target
# Look for: ModerationSMP-X.X.X.jar
```

## 📖 How It Works

### Death Dimension Flow
1. Player dies → Teleported to Death Dimension
2. Must complete **Star Sequencer Puzzle** (find pattern in constellation)
3. Then complete **Parkour Challenge** (platforming with checkpoints)
4. Successfully escaping both puzzles revives the player
5. Alternatively, teammates can use **Resurrection Ritual** to revive them

### Resurrection Ritual
To revive a fallen teammate:


<img width="354" height="168" alt="Capture d&#39;écran 2025-11-30 180613" src="https://github.com/user-attachments/assets/bc16fc0c-3637-4a66-a701-3e3798407029" />

4. Confirm the ritual to bring them back

## 🎯 Permissions

All permissions follow the format: `moderation.<feature>.<action>`

Examples:
- `moderation.admin` - All admin commands
- `moderation.vanish` - Use vanish
- `moderation.deathdim.bypass` - Bypass death dimension
- `moderation.homes.unlimited` - Unlimited homes

## 🐛 Known Issues

No known issues. Please report bugs in the [Issues](https://github.com/leoo955/SemiHardcore-Plugins/issues) section.

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**LEOO955**

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

Feel free to check the [issues page](https://github.com/leoo955/SemiHardcore-Plugins/issues).

## ⭐ Show Your Support

Give a ⭐️ if this project helped you!

## 📞 Support

- **Bug Reports:** [GitHub Issues](https://github.com/leoo955/SemiHardcore-Plugins/issues)
- **Feature Requests:** [GitHub Discussions](https://github.com/leoo955/SemiHardcore-Plugins/discussions)
