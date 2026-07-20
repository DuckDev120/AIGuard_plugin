# Quick Build Instructions - AIGuard

## Requirements
- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for downloading dependencies)

## Quick Build

### Windows (PowerShell)
```powershell
# Check Java
java -version

# Check Maven
mvn -version

# Build
mvn clean package

# The ready file will be in:
# target\aiguard-1.0.0.jar
```

### Linux/Mac (Terminal)
```bash
# Check Java
java -version

# Check Maven  
mvn -version

# Build
mvn clean package

# The ready file will be in:
# target/aiguard-1.0.0.jar
```

## Quick Installation on Server

1. **Copy the file**:
   ```
   Copy: target/aiguard-1.0.0.jar
   To: plugins/aiguard-1.0.0.jar (in server folder)
   ```

2. **Restart the server** or use `/reload`

3. **Edit Configuration**:
   ```
   Edit: plugins/AIGuard/config.yml
   ```

4. **Reload**:
   ```
   /aiguard reload
   ```

## Quick Test

### Check that the plugin works:
1. Write in chat: "idiot" (Should receive an alert)
2. Write in chat: "i d i o t" (Should receive an alert)
3. Write in chat: "hello" (Should NOT receive an alert)

### Required permissions for testing:
- `aiwatch.alert` - To receive alerts
- `aiwatch.admin` - For admin commands

## Quick Troubleshooting

### Java Error
```
Error: A JNI error has occurred
```
**Solution**: Update to Java 17 or higher

### Maven Error
```
'mvn' is not recognized
```
**Solution**: Install Maven or use an IDE with built-in Maven

### Plugin Does Not Load
**Check**:
1. Server version (1.20+)
2. `plugin.yml` file exists
3. No errors in console

### No Alerts
**Check**:
1. You have `aiwatch.alert` permission
2. The word exists in `banned_words` list
3. Similarity threshold is not too high (`similarity_threshold`)

## Project Structure
```
AIGuard/
├── src/main/java/com/duckslavi/aiguard/
│   ├── AIGuard.java              # Main class
│   ├── config/ConfigManager.java # Configuration management
│   ├── listeners/ChatListener.java # Chat listener
│   ├── utils/FuzzyMatcher.java   # Fuzzy matching
│   └── bungee/BungeeManager.java # BungeeCord support
├── src/main/resources/
│   ├── plugin.yml               # Plugin metadata
│   └── config.yml              # Default configuration
├── pom.xml                     # Maven configuration
└── README.md                   # Full documentation
```

---
**Tip**: Use an IDE like IntelliJ IDEA or Eclipse for easier development!
