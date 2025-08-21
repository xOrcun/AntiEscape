# 🚫 AntiEscape - Advanced Minecraft Player Control Plugin

[![GitHub release](https://img.shields.io/github/v/release/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/releases)
[![GitHub license](https://img.shields.io/github/license/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/blob/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/network)
[![GitHub issues](https://img.shields.io/github/issues/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/xOrcun/AntiEscape?style=flat-square)](https://github.com/xOrcun/AntiEscape/graphs/contributors)

> **Advanced Minecraft server player control and security system with Discord integration, comprehensive logging, and multi-language support.**

---

## 🌍 **Language / Dil Seçimi**

### 🇹🇷 **Türkçe Kullanıcılar İçin:**
**Türkçe dokümantasyon için [README_TR.md](README_TR.md) dosyasını inceleyin.**

### 🇺🇸 **English Users:**
**For English documentation, continue reading below.**

---

## 🌟 **Features**

### 🔒 **Core Control System**
- **Player Control**: Take control of players and restrict their movements
- **Movement Prevention**: Block all player movements, teleports, and actions
- **Command Blocking**: Prevent controlled players from using commands
- **Chat Control**: Isolated control chat system for administrators
- **Item Drop Prevention**: Stop controlled players from dropping items
- **Damage Protection**: Protect controlled players from attacks
- **Self-Control Prevention**: Players cannot control themselves

### 🚀 **Advanced Control System**
- **Session Management**: Track control sessions with timestamps
- **Control History**: Complete history of all control actions
- **Player Notes**: Add and manage notes for each player
- **Statistics**: Track escape attempts and suspicious activities
- **Time Limits**: Set maximum control duration with auto-release
- **Notifications**: BossBar, Title, ActionBar, and Sound notifications
- **Escape Tracking**: Record when players quit during control

### 🛡️ **Advanced Security System**
- **IP Address Control**: Monitor and restrict IP addresses
- **VPN/Proxy Detection**: Detect and block VPN connections
- **Suspicious Activity**: Monitor rapid movements, commands, and chat
- **Auto-Ban System**: Automatic banning for security violations
- **Whitelist System**: Bypass security checks for trusted players
- **Security Logging**: Comprehensive security event logging
- **Ban Escalation**: Increase ban duration for repeat offenders

### 📊 **Comprehensive Logging System**
- **Separate Log Files**: Different log files for each event type
- **Configurable Logging**: Enable/disable specific log types
- **Log Rotation**: Automatic log file rotation and compression
- **Event Types**: Moves, commands, chat, damage, items, control, general
- **Detailed Information**: Timestamps, player info, location data

### 🔗 **Discord Integration**
- **Webhook System**: Send notifications to Discord channels
- **Customizable Embeds**: Fully customizable embed messages
- **Event Notifications**: Control start/end, escapes, movements, commands
- **Rich Formatting**: Custom colors, thumbnails, fields, and footers
- **Async Processing**: Non-blocking webhook delivery

### 🌍 **Multi-Language Support**
- **English & Turkish**: Full language support with easy switching
- **Configurable Messages**: All messages customizable via language files
- **Easy Localization**: Simple structure for adding new languages
- **Auto-Generation**: Language files created automatically

### 📈 **Monitoring & Statistics**
- **Real-time Monitoring**: Track active controls and player status
- **Performance Metrics**: Monitor plugin performance and usage
- **bStats Integration**: Anonymous usage statistics
- **Update Checker**: Automatic version checking and notifications

---

## 🚀 **Quick Start**

### 📋 **Requirements**
- **Minecraft Server**: 1.16+ (Spigot/Paper/Bukkit)
- **Java**: Java 8 or higher
- **Permissions**: Vault plugin (recommended)

### 📥 **Installation**

1. **Download** the latest release from [Releases](https://github.com/xOrcun/AntiEscape/releases)
2. **Place** the JAR file in your `plugins` folder
3. **Restart** your server
4. **Configure** the plugin via `config.yml`
5. **Set up** Discord webhooks (optional)

### ⚙️ **Basic Configuration**

```yaml
# Basic settings
prefix: "&6ᴀɴᴛɪᴇѕᴄᴀᴘᴇ &8▸ &7"
language: "en"
debug: false

# Control locations
control-location: "none"  # Set with /control set area
control-spawn-location: "none"  # Set with /control set return

# Auto-ban system
auto-ban:
  enabled: true
  normal-ban:
    duration: "1d"
    reason: "Security violation detected"
  escape-ban:
    duration: "7d"
    reason: "Escape attempt during control"
  ban-command: "tempban %player% %duration% %reason%"
```

---

## 📖 **Commands**

### 🔧 **Main Commands**
| Command | Description | Permission |
|---------|-------------|------------|
| `/control help` | Show help menu | `antiescape.general` |
| `/control take <player>` | Take control of a player | `antiescape.general` |
| `/control end <player> [ban]` | End control or ban player | `antiescape.general` |
| `/control chat <join/leave>` | Join/leave control chat | `antiescape.general` |

### 📍 **Location Commands**
| Command | Description | Permission |
|---------|-------------|------------|
| `/control set area` | Set control area | `antiescape.general` |
| `/control set return` | Set return location | `antiescape.general` |
| `/control delete area` | Delete control area | `antiescape.general` |
| `/control delete spawn` | Delete return location | `antiescape.general` |

### 🔍 **Advanced Control Commands**
| Command | Description | Permission |
|---------|-------------|------------|
| `/control history <player>` | Show control history | `antiescape.general` |
| `/control notes <add/clear/list> <player>` | Manage player notes | `antiescape.general` |
| `/control stats <player>` | Show player statistics | `antiescape.general` |

### 🛡️ **Security Commands**
| Command | Description | Permission |
|---------|-------------|------------|
| `/control whitelist <add/remove> <player> [reason]` | Manage whitelist | `antiescape.general` |
| `/control suspicious <player> [clear]` | Check suspicious activity | `antiescape.general` |
| `/control violations <player> [clear]` | Check violation count | `antiescape.general` |
| `/control ip <ip> [player]` | Check IP information | `antiescape.general` |

### 📊 **System Commands**
| Command | Description | Permission |
|---------|-------------|------------|
| `/control logs <list/clear/clear-all>` | Manage log files | `antiescape.general` |
| `/control discord test` | Test Discord webhook | `antiescape.general` |
| `/control reload` | Reload configuration | `antiescape.general` |
| `/control version` | Show plugin information | `antiescape.general` |

---

## 🔐 **Permissions**

### 📋 **Permission Nodes**
```yaml
antiescape.general          # Access to all basic commands
antiescape.chat            # Access to control chat
antiescape.update.notify   # Receive update notifications
```

---

## 📁 **File Structure**

```
AntiEscape/
├── config.yml              # Main configuration
├── webhook.yml             # Discord webhook settings
├── lang/
│   ├── en.yml             # English language file
│   └── tr.yml             # Turkish language file
├── logs/                   # Log files directory
│   ├── antiescape-moves.log
│   ├── antiescape-commands.log
│   ├── antiescape-chat.log
│   ├── antiescape-damage.log
│   ├── antiescape-items.log
│   ├── antiescape-control.log
│   └── antiescape-general.log
└── data/                   # Data files
    ├── control-history.yml # Control history
    ├── notes.yml          # Player notes
    └── whitelist.yml      # Security whitelist
```

---

## 🔧 **Configuration**

### 📝 **Main Configuration (config.yml)**
```yaml
# Language and Debug
language: "en"
debug: false

# Logging System
logging:
  enabled: true
  log-moves: true
  log-commands: true
  log-chat: true
  log-damage: true
  log-items: true
  log-control: true
  log-general: true
  rotation:
    enabled: true
    max-size: "10MB"
    max-files: 5
    compress: true

# Advanced Control System
advanced-control:
  enabled: true
  time-limits:
    enabled: true
    max-duration: 3600
    warning-time: 300
    auto-release: true
  history:
    enabled: true
    max-entries: 1000
  notes:
    enabled: true
    max-notes: 10
    note-length: 200
  notifications:
    enabled: true
    action-bar: true
    title: true
    sound: true
    boss-bar: true

# Advanced Security System
advanced-security:
  enabled: true
  ip-control:
    enabled: true
    max-accounts-per-ip: 3
  vpn-detection:
    enabled: false
    block-vpn: false
  suspicious-activity:
    enabled: false
    threshold: 5
    time-window: 10
  auto-ban:
    enabled: true
    escape-attempts: 3
    suspicious-activity: 5
    vpn-usage: 1
    escalation:
      enabled: true
      multiplier: 2
      max-duration: "1m"
```

### 🔗 **Discord Webhook (webhook.yml)**
```yaml
discord:
  enabled: true
  webhook-url: "YOUR_WEBHOOK_URL_HERE"
  bot-name: "AntiEscape Bot"
  bot-avatar: ""

embeds:
  control:
    start:
      title: "🔒 Control Started"
      description: "**%target%** was put under control by **%controller%**"
      color: "#FF6B6B"
      thumbnail: "%target_avatar%"
    end:
      title: "✅ Released"
      description: "**%target%** was released by **%controller%**"
      color: "#51CF66"
    escape:
      title: "🚨 Escape Attempt"
      description: "**%target%** attempted to escape during control"
      color: "#FFA500"
  movement:
    title: "🚶 Movement Attempt"
    description: "**%player%** attempted to move while controlled"
    color: "#FF6B6B"
```

---

## 🌍 **Language Support**

### 🇺🇸 **English (en.yml)**
```yaml
no-permission: "&cYou don't have permission to use this command!"
control-started: "&a%player% is now under control!"
control-finished-clean: "&a%player% has been released from control!"
control-finished-ban: "&c%player% has been banned!"
```

### 🇹🇷 **Turkish (tr.yml)**
```yaml
no-permission: "&cBu komutu kullanma yetkiniz yok!"
control-started: "&a%player% artık kontrol altında!"
control-finished-clean: "&a%player% kontrolden çıkarıldı!"
control-finished-ban: "&c%player% banlandı!"
```

---

## 📊 **Logging System**

### 📝 **Log Types**
- **Moves**: Player movement attempts
- **Commands**: Command execution attempts
- **Chat**: Chat messages
- **Damage**: Damage events
- **Items**: Item drop attempts
- **Control**: Control start/end events
- **General**: General plugin events

### 🔄 **Log Rotation**
- **Max Size**: 10MB per log file
- **Max Files**: 5 backup files
- **Compression**: Automatic compression of old logs

---

## 🛡️ **Security Features**

### 🔍 **IP Control**
- Monitor multiple accounts per IP
- Country-based restrictions
- Automatic suspicious activity detection

### 🚫 **VPN Detection**
- Real-time VPN/Proxy detection
- Configurable blocking policies
- Whitelist for trusted VPNs

### ⚠️ **Suspicious Activity**
- Rapid movement detection
- Command spam prevention
- Chat flood protection
- Automatic action logging

### 🚨 **Auto-Ban System**
- **Normal Bans**: For security violations
- **Escape Bans**: When players quit during control
- **Movement Bans**: For movement violations (configurable)
- **Ban Escalation**: Increase duration for repeat offenders

---

## 🔗 **Discord Integration**

### 📢 **Webhook Events**
- **Control Events**: Start, end, escape attempts
- **Player Actions**: Movements, commands, chat
- **Security Events**: VPN detection, suspicious activity
- **System Events**: Plugin updates, errors

### 🎨 **Customizable Embeds**
- Custom titles and descriptions
- Configurable colors and thumbnails
- Dynamic field generation
- Footer customization

---

## 📈 **Performance**

### ⚡ **Optimizations**
- Asynchronous webhook delivery
- Efficient data structures
- Configurable logging levels
- Memory-efficient caching

### 📊 **Monitoring**
- Real-time performance metrics
- Memory usage tracking
- Event processing statistics
- bStats integration

---

## 🚀 **Advanced Features**

### 🎯 **Control Sessions**
- Session tracking with UUIDs
- Duration monitoring
- Escape attempt recording
- Automatic cleanup

### 📝 **Player Notes**
- Persistent note storage
- Timestamp tracking
- Note length limits
- Easy management commands

### 📊 **Statistics System**
- Control duration tracking
- Escape attempt counting
- Suspicious activity monitoring
- IP address tracking

### 🔄 **Tab Completion**
- Smart command suggestions
- Player name autocomplete
- Context-aware completions
- No more ArrayIndexOutOfBoundsException

---

## 🔧 **Development**

### 🛠️ **Building from Source**
```bash
# Clone the repository
git clone https://github.com/xOrcun/AntiEscape.git

# Navigate to directory
cd AntiEscape

# Build with Maven
mvn clean package
```

### 📦 **Dependencies**
- **Bukkit/Spigot API**: Core Minecraft server API (1.16.5+)
- **Gson**: JSON processing
- **bStats**: Anonymous usage statistics
- **Vault**: Permission system integration

### 🧪 **Testing**
- Tested on Spigot 1.16+
- Compatible with Paper servers
- Bukkit compatibility verified
- Performance tested on large servers

---

---

## 🤝 **Contributing**

We welcome contributions! Please feel free to submit issues, feature requests, or pull requests.

### 📝 **How to Contribute**
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

### 🐛 **Reporting Issues**
- Use the GitHub issue tracker
- Provide detailed information
- Include server logs if applicable
- Specify Minecraft version

---

## 📄 **License**

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 **Acknowledgments**

- **Spigot Community** for the excellent Bukkit API
- **bStats Team** for anonymous usage statistics
- **All Contributors** who helped improve this plugin

---

## 📞 **Support**

### 🔗 **Links**
- **GitHub**: [https://github.com/xOrcun/AntiEscape](https://github.com/xOrcun/AntiEscape)
- **Discord**: [https://orcunozturk.com/discord](https://orcunozturk.com/discord)
- **Website**: [https://orcunozturk.com](https://orcunozturk.com)

### 💬 **Getting Help**
- **Discord Server**: Join our community for support
- **GitHub Issues**: Report bugs and request features
- **Documentation**: Check this README for common solutions

---

<div align="center">

**Made with ❤️ by [Orcun Ozturk](https://github.com/orcunozturk)**

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/xOrcun)
[![Discord](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://orcunozturk.com/discord)
[![Website](https://img.shields.io/badge/Website-FF6B6B?style=for-the-badge&logo=website&logoColor=white)](https://orcunozturk.com)

</div> 