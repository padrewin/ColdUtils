# Chat Polish 1.1.0

A client-side Fabric mod that capitalizes the first letter of outgoing chat
messages and adds a final period.

Example: `hello everyone` -> `Hello everyone.`

The mod does not add or rewrite words. It preserves existing final punctuation
such as `.`, `!`, `?`, and `...`, and leaves commands unchanged. At the
256-character limit, it skips the extra period instead of truncating your message.
HEX and legacy formatting codes are preserved and skipped when finding the first
letter: `&#FF0000hello` -> `&#FF0000Hello.` and `&ahello&r` -> `&aHello.&r`.
Displaying these colors still requires server support and any necessary permissions.

## Settings

Open **Mods > Chat Polish > Configure** through Mod Menu.

- **Enable Chat Polish**: enable or disable all message formatting.
- **Capitalize first letter**: toggle capitalization independently.
- **Add final period**: toggle the final period independently.

All three settings are enabled by default. **Save & Done** applies and saves your
changes; **Cancel** discards them. Settings persist between game sessions in
`config/chatpolish.properties`. Each setting can be reset to its default value.

## Installation

1. Install Fabric Loader for your Minecraft version.
2. Install Fabric API, Mod Menu, and Cloth Config API (Fabric), using versions that match your game.
3. Place `chat-polish-1.1.0.jar` in your Minecraft instance's `mods` directory, replacing any older Chat Polish JAR.

The same Chat Polish JAR targets Minecraft 1.21.11 and the 26.x series.
Minecraft 1.21.11 uses Java 21; the 26.x versions targeted here require Java 25.
The mod only needs to be installed on the client, not on the server.

## Compatibility

The chat formatter uses Java and Fabric interfaces without mixins. The settings
menu uses a runtime adapter for Mod Menu and Cloth Config to avoid compile-time
dependencies on version-specific Minecraft screen classes.

The mod metadata allows 1.21.11 and 26.x, including future releases in the 26 series.
This does not guarantee compatibility with future API changes. In-game testing is
still required; successful compilation and local tests do not replace it.

## IntelliJ

Use **File > Open**, select `pom.xml` in this directory, and open it as a project.
Select JDK 21 for both the project SDK and Maven Runner.
In the Maven tool window, run **Lifecycle > package**.

Alternatively, with Maven installed, run:

```sh
mvn package
```

The compiled JAR is written to `target/chat-polish-1.1.0.jar`.
The project uses Maven and does not require a Minecraft remapping build step.

## Links

- [Website](https://github.com/padrewin/ChatPolish)
- [Issues](https://github.com/padrewin/ChatPolish)
