# AIGuard - Smart Chat Monitoring Plugin

## Description
AIGuard is an advanced Minecraft plugin (Spigot/Paper 1.20+) that monitors chat messages and detects banned words using Fuzzy Matching algorithms. The plugin is capable of detecting variations of banned words even when they are distorted or separated.

## Key Features

### 🔍 Advanced Detection
- **Fuzzy Matching**: Detects banned word variations like "ya 0", "you are 0", "0 loser" for configured words.
- **Advanced Algorithms**: Uses Levenshtein Distance and Cosine Similarity.
- **Adjustable Threshold**: Sensitivity level can be configured (Default: 75%).

### ⚡ Performance
- **Asynchronous Processing**: Chat is not blocked during checks.
- **Lightweight**: Algorithms optimized for high performance.
- **Efficient Memory**: Optimized memory management.

### 🌐 BungeeCord Support
- **Server Network**: Alerts are forwarded across all servers in the network.
- **Fast Communication**: Uses Plugin Messaging Channel.
- **Source Identification**: Displays the server from which the alert originated.

### 🎛️ Advanced Management
- **Flexible Configuration**: Easy editing of settings and banned words.
- **Reload**: `/aiguard reload` without restarting.
- **Detailed Permissions**: Precise control over who sees alerts.

## System Requirements
- **Server**: Spigot/Paper 1.20 or higher
- **Java**: Version 17 or higher
- **Memory**: Minimum 512MB RAM free
- **BungeeCord** (Optional): For server network support

## Installation

### Step 1: Install on Server
1. Copy the `aiguard-1.0.0.jar` file to the server's `plugins` folder.
2. Restart the server or use the `/reload` command.
3. The plugin will automatically create the `config.yml` file in the `plugins/AIGuard/` folder.

### Step 2: Initial Configuration
Edit the `plugins/AIGuard/config.yml` file:

```yaml
# Similarity threshold (0.0 - 1.0)
similarity_threshold: 0.75

# List of banned words
banned_words:
  - "idiot"
  - "dumb" 
  - "stupid"
  - "noob"

# Enable BungeeCord (if you have a server network)
bungee:
  enabled: false
  channel: "aiguard:alerts"
```

## Usage

### Commands
- `/aiguard reload` - Reload configuration.
- `/ag reload` - Shortcut for the above command.

### Permissions
- `aiwatch.admin` - Access to management commands.
- `aiwatch.alert` - Receive alerts about suspicious messages.
- `aiwatch..*` - All permissions.

## BungeeCord Setup

### On BungeeCord Server
No special installation is needed on the BungeeCord server itself.

### On Each Server in the Group
1. Set `bungee.enabled: true` in the configuration.
2. Ensure all servers use the same `channel`.
3. Set a unique `server_name` for each server.

```yaml
bungee:
  enabled: true
  channel: "aiguard:alerts"

server_name: "Survival"  # Change for each server
```

## Customization

### Adding Banned Words
```yaml
banned_words:
  - "new_word"
  - "another_word"
```

### Changing Alert Message
```yaml
messages:
  alert: "§c[AIGuard] §e{player} §fwrote: §7{message} §f| Detected: §c{banned_word} §f| Similarity: §6{similarity}%"
```

### Available Placeholders in Messages:
- `{player}` - Player name
- `{message}` - Original message
- `{banned_word}` - Detected banned word
- `{similarity}` - Similarity percentage

## Troubleshooting

### Plugin Does Not Load
- Check that Java version is 17 or higher.
- Ensure server version is 1.20 or higher.
- Check the log for errors.

### No Alerts
- Check that you have the `aiwatch.alert` permission.
- Ensure similarity threshold is not too high.
- Check that banned words are configured correctly.

### BungeeCord Issues
- Ensure all servers are connected to the same BungeeCord.
- Check that the channel is identical on all servers.
- Ensure players are connected (required for sending messages).

## Technical Support

### Logs
The plugin writes detailed logs to the server console. Check the logs for information on:
- Configuration loading
- Suspicious message detection
- BungeeCord errors

### Reporting Bugs
When reporting an issue, please include:
1. Server version (Spigot/Paper)
2. Java version
3. Configuration file
4. Error messages from the log
5. Detailed description of the problem

## License
This project is distributed under the MIT License. See LICENSE file for details.

## Credits
- **Developer**: DucksAreLavi
- **Algorithms**: Levenshtein Distance, Cosine Similarity
- **Support**: Spigot/Paper API

---

**Note**: This plugin is designed to assist in monitoring and not to replace human judgment. It is recommended to verify alerts before taking action.
