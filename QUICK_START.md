# Quick Start - AIGuard

## What to do now:

### 1. Build the Plugin 🔨
```bash
cd c:\Users\Owner\Desktop\pl\AIGuard
mvn clean package
```

### 2. Copy to Server 📁
```
Copy: target/aiguard-1.0.0.jar
To: plugins/ (in your server folder)
```

### 3. Restart Server 🔄
Or use `/reload`

### 4. Configure Permissions 👥
```yaml
# In permissions file or permissions plugin:
groups:
  admin:
    permissions:
      - aiwatch.*
  moderator:
    permissions:
      - aiwatch.alert
```

### 5. Adjust Configuration (Optional) ⚙️
Edit: `plugins/AIGuard/config.yml`
```yaml
similarity_threshold: 0.75  # Similarity threshold (0.0-1.0)
banned_words:
  - "idiot"
  - "dumb"
  - "noob"
  # Add more words...
```

### 6. Reload ⚡
```
/aiguard reload
```

## Quick Test ✅

1. **Write in chat**: "idiot" → Should receive an alert
2. **Write in chat**: "i d i o t" → Should receive an alert  
3. **Write in chat**: "hello" → Should NOT receive an alert

## Enabling BungeeCord (If you have a server network) 🌐

1. **On each server** edit `config.yml`:
```yaml
bungee:
  enabled: true
  channel: "aiguard:alerts"

server_name: "Server_Name"  # Change for each server
```

2. **Reload on each server**:
```
/aiguard reload
```

## That's it! The plugin is ready to use! 🎉

### Useful Commands:
- `/aiguard reload` - Reload
- `/ag reload` - Shortcut

### Important Permissions:
- `aiwatch.alert` - To receive alerts
- `aiwatch.admin` - For admin commands

---

**Issues?** Read `README.md` for full details or `BUILD.md` for build troubleshooting.
