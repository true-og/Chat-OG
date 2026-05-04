# Chat-OG
Chat plugin for [TrueOG](https://github.com/true-og/true-og)

## Features
- Adds LuckPerms prefix, Unions-OG tag, and LuckPerms suffix to chat messages.
- Formats chat messages with MiniMessage if the player sending them has the `chat-og.color` permission, with legacy `&` and `§` color code support.
- Translates chat messages using any OpenAI(-compatible) API. Click any chat or Discord message to translate it on-demand, and run command `/translatesettings <language>` to pick your preferred language (preferences persisted in KeyDB).
- Discord bridge with custom emoji (Minecraft → Discord), animated NQN emoji support, clickable attachment links, and Unicode emoji-to-shortcode conversion (Emoji 17.0).
- Multi-channel chat: general (`/g` / `/gc`), staff (`/s` / `/sc`), premium (`/p` / `/pc`) and developer (`/d` / `/dc`), each with its own Discord webhook.
- Private messaging (`/msg`, `/whisper`, `/pm`) with `/r` and `/reply` shortcuts.
- Forwards joins, quits, kicks, advancements, deaths and broadcasts to Discord, with full Vanish-OG awareness so vanished players never leak into Discord.
- LuckPerms-driven Discord role color mapping so that player rank based formatting permissions carry over to Discord.
- Pings in chat alert the mentioned player. Words both ways from Minecraft <-> Discord.
- Censor `@everyone`, `@here` and role mentions on the Discord side.
- API for other plugins to send messages through the Discord bridge.
- Run command `/chatconfigreload` to fully reload the config and the Discord bridge at runtime.

## Building
```./gradlew clean build eclipse --warning-mode all```

## Emoji Converter Credits
- https://github.com/mathiasbynens/emoji-test-regex-pattern
- https://github.com/amio/emoji.json/blob/HEAD/scripts/gen.js (this project is using a modified version, it's in the repository's root folder also called `gen.js`)
