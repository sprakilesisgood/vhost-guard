# Vhost Guard

A server-side Paper plugin that stops people from joining your Minecraft server through the raw IP. It only accepts connections that come through a hostname you configured.

## Why this exists

When a player adds your server to their list, the client sends whatever address they typed — `mc.example.com`, `192.168.1.100`, `localhost`, whatever — in the handshake packet. This plugin reads that address before the player fully logs in and kicks them if it isn't on your allow-list.

This is useful if you run multiple servers behind a proxy and want to make sure each one only receives traffic meant for it.

## Requirements

- Minecraft 1.21.11
- PaperMC 1.21.11
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

The jar lands in `build/libs/vhost-guard-2.0.0.jar`. Drop it into your server's `plugins/` folder. No need to install it on clients.

## Configuration

The first time the server starts, the plugin creates `plugins/VhostGuard/vhost-guard.json`.

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

- The plugin is server-side only. Players do not need it on their client.
- Server-list status pings still work normally.
- The check runs during Paper's async pre-login event, before the player fully joins.

## License

MIT