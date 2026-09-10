package dev.padrewin.coldutils;

public final class ChatFormatter {
    private ChatFormatter() {}

    public static String format(String message) {
        return format(message, ColdUtilsSettings.DEFAULT);
    }

    public static String format(String message, ColdUtilsSettings settings) {
        if (!settings.enabled() || (!settings.capitalization() && !settings.period())) return message;
        String text = message.strip();
        if (text.isEmpty() || text.startsWith("/")) return message;
        for (int offset = 0; settings.capitalization() && offset < text.length();) {
            int codeLength = formattingLength(text, offset);
            if (codeLength > 0) {
                offset += codeLength;
                continue;
            }
            int point = text.codePointAt(offset);
            if (Character.isLetter(point)) {
                text = text.substring(0, offset)
                        + new String(Character.toChars(Character.toUpperCase(point)))
                        + text.substring(offset + Character.charCount(point));
                break;
            }
            offset += Character.charCount(point);
        }
        int last = -1;
        int visibleEnd = 0;
        for (int offset = 0; offset < text.length();) {
            int codeLength = formattingLength(text, offset);
            if (codeLength > 0) {
                offset += codeLength;
                continue;
            }
            int point = text.codePointAt(offset);
            offset += Character.charCount(point);
            if (!Character.isWhitespace(point)) {
                last = point;
                visibleEnd = offset;
            }
        }
        // Minecraft chat is limited to 256 UTF-16 characters. Never drop user text.
        if (settings.period() && last != -1 && last != '.' && last != '!' && last != '?' && last != 0x2026
                && text.length() < 256) {
            text = text.substring(0, visibleEnd) + "." + text.substring(visibleEnd);
        }
        return text;
    }

    private static int formattingLength(String text, int offset) {
        if (text.charAt(offset) != '&' || offset + 1 >= text.length()) return 0;
        char code = Character.toLowerCase(text.charAt(offset + 1));
        if (code == '#' && offset + 8 <= text.length()) {
            for (int i = offset + 2; i < offset + 8; i++) {
                if ("0123456789abcdefABCDEF".indexOf(text.charAt(i)) < 0) return 0;
            }
            return 8;
        }
        return "0123456789abcdefklmnor".indexOf(code) >= 0 ? 2 : 0;
    }
}
