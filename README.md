# Vhost Guard

A server-side Fabric mod that stops people from joining your Minecraft server through the raw IP. It only accepts connections that come through a hostname you configured.

## Why this exists

When a player adds your server to their list, the client sends whatever address they typed — `mc.example.com`, `192.168.1.100`, `localhost`, whatever — in the handshake packet. This mod reads that address before the player even logs in and kicks them if it isn't on your allow-list.

This is useful if you run multiple servers behind a proxy and want to make sure each one only receives traffic meant for it.

## Note about v2

This version is a Fabric mod. A Paper/Spigot plugin version is planned for v2.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.3 or newer
- Java 21

## Building

This project uses the Gradle wrapper. If you don't have a wrapper jar yet, generate one with:

```bash
gradle wrapper
```

Then build:

```bash
./gradlew build   # Linux / macOS
gradlew.bat build # Windows
```

The jar lands in `build/libs/vhost-guard-1.0.0.jar`. Drop it into your server's `mods/` folder. No need to install it on clients.

## Configuration

The first time the server starts, the mod creates `config/vhost-guard.json`.

Example:

```json
{
  "allowedHosts": [
    "mc.example.com",
    "*.example.com"
  ],
  "kickMessage": "Connect via mc.example.com. (You tried: %host%)"
}
```

- `allowedHosts`: hostnames that are allowed to join. `*.example.com` matches any subdomain, and a bare `*` allows everything.
- `kickMessage`: the message rejected players see. `%host%` is replaced with the address they connected to.

## Notes

- The mod is server-side only. Players do not need it on their client.
- It applies to `LOGIN` and `TRANSFER` intents, so server-list status pings still work normally.
- The check happens before authentication, so it does not add per-player overhead.

## License

MIT
