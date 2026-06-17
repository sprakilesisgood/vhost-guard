# Vhost Guard

Stop players from joining your Minecraft server through the raw IP. Vhost Guard only lets people in when they connect through a hostname you have allowed.

## Why this exists

When someone adds your server to their list, the client sends whatever address they typed — `mc.example.com`, `192.168.1.100`, `localhost`, anything — in the handshake packet. This plugin reads that address before the player finishes logging in and kicks them if it is not on your allow-list.

If you run several servers behind a proxy, this helps make sure each one only gets the traffic meant for it.

## Requirements

- Minecraft 1.21.11
- PaperMC 1.21.11
- Java 21

## Building

This project uses the Gradle wrapper. If you do not have a wrapper jar yet, generate one with:

```bash
gradle wrapper
```

Then build:

```bash
./gradlew build   # Linux / macOS
gradlew.bat build # Windows
```

The jar ends up in `build/libs/vhost-guard-2.0.0.jar`. Put it in your server's `plugins/` folder. Players do not need it on their client.

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

- Server-side only. Players do not need it on their client.
- Server-list status pings still work normally.
- The check runs during Paper's async pre-login event, before the player fully joins.

## License

MIT
