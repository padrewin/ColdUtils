# ColdUtils 1.2.2

A client-side Fabric utility mod with configurable chat formatting and Player ESP.
The Chat tab can capitalize outgoing messages and add a final period.

Example: `hello everyone` -> `Hello everyone.`

The mod does not add or rewrite words. It preserves existing final punctuation
such as `.`, `!`, `?`, and `...`, and leaves commands unchanged. At the
256-character limit, it skips the extra period instead of truncating your message.
HEX and legacy formatting codes are preserved and skipped when finding the first
letter: `&#FF0000hello` -> `&#FF0000Hello.` and `&ahello&r` -> `&aHello.&r`.
Displaying these colors still requires server support and any necessary permissions.

## Settings

Open **Mods > ColdUtils > Configure** through Mod Menu.

- **Enable chat formatting**: enable or disable all message formatting in the Chat tab.
- **Capitalize first letter**: toggle capitalization independently.
- **Add final period**: toggle the final period independently.

All three settings are enabled by default. **Save & Done** applies and saves your
changes; **Cancel** discards them. Settings persist between game sessions in
`config/coldutils.properties`. Each setting can be reset to its default value.

Upgrading from Chat Polish: remove the old `chat-polish` JAR before installing
ColdUtils. On first launch, settings from `config/chatpolish.properties` are copied
to the new configuration if it does not exist. The old file is preserved; an
existing ColdUtils configuration always takes priority.

### Player ESP

Open **Mods > ColdUtils > Configure > Player ESP**:

- **Enable Player ESP**: toggle the outline effect independently of chat formatting (off by default).
- **Enable Player Names**: show colored nameplates for matching players, including
  invisible players (off by default). Independent of the ESP toggle: names work with
  outlines disabled. Turning this off restores normal Minecraft nameplate behavior.
- **Only invisible players**: on by default; turn off to outline visible players too.
- **Outline color**: RGB color picker, cyan by default.
- **Keep server name colors**: on by default; preserves the original server nameplate component, including prefix/suffix and HEX styling received by the client. Turn off to override the whole nameplate with one color.
- **Player name color** (server colors off): separate RGB color picker for the overhead name, white by default.
- **Range (blocks)**: 1–256 blocks, 64 by default, measured from your player.

Click **Save & Done** to apply. The effect uses Minecraft's glowing silhouette,
including through walls, rather than a skeleton wireframe. It affects other client
players only and does not change server entity flags, teams, or invisibility.
Disabling ESP restores vanilla rendering, including any server-provided glow.
Names use the game's nameplate renderer, so normal nameplate distance limits and
occlusion for sneaking players still apply. Rendering/shader mods may affect outlines.
Only entities sent to your client can be highlighted; server-side vanish that stops
sending the player entity cannot be detected.

## Installation

1. Install Fabric Loader for your Minecraft version.
2. Install Fabric API, Mod Menu, and Cloth Config API (Fabric), using versions that match your game.
3. Place `coldutils-1.2.2.jar` in your Minecraft instance's `mods` directory, replacing any older ColdUtils JAR.

The same ColdUtils JAR targets Minecraft 1.21.11 and the 26.x series.
Minecraft 1.21.11 uses Java 21; the 26.x versions targeted here require Java 25.
The mod only needs to be installed on the client, not on the server.

## Compatibility

The chat formatter uses Java and Fabric interfaces. Player ESP uses client-only
Mixin hooks with explicit intermediary (1.21.11) and Mojang (26.x) method selectors.
The settings
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

The compiled JAR is written to `target/coldutils-1.2.2.jar`.
The project uses Maven and does not require a Minecraft remapping build step.

### Manual ESP verification (run on both 1.21.11 and your 26.x version)

1. Join a world with another player within 64 blocks. ESP starts disabled.
2. Give that player invisibility, enable ESP and Player Names, and check the cyan silhouette and white name.
3. Put a wall between you: the silhouette should remain visible.
4. Set outline and name to different colors, change range, save, and verify they apply immediately.
5. Turn off the invisible-only filter and verify visible players are outlined too.
6. Test all four toggle combinations: neither, outline only, names only, and both.
   With both off normal rendering should return, including any existing server glow.
   Your own player and mobs should never gain ESP.
7. Reconnect/restart to verify saved settings; cancel an edit to verify it is discarded.

## Links

- [Website](https://github.com/padrewin/ColdUtils)
- [Issues](https://github.com/padrewin/ColdUtils)

## Nearby command

Type .coldnear or .coldnear [radius] in chat. Radius is an integer from 1 to 256 blocks and defaults to 256. The local command counts other players loaded in the current client world within the 3D radius, including invisible players, independently of ESP settings. It never sends the command to the server. Server-side vanish entities not sent to the client cannot be counted. The response uses the ColdUtils blue gradient prefix and shows the chosen radius.
