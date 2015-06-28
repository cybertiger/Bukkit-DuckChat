/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

/**
 *
 * @author antony
 */
public enum ChannelMode {
    OWNER('~', 'q'),
    ADMIN('&', 'a'),
    OPERATOR('@', 'o'),
    HALF_OPERATOR('%', 'h'),
    VOICE('+', 'v');
    private final char modePrefix;
    private final char modeFlag;

    private ChannelMode(char modePrefix, char modeFlag) {
        this.modePrefix = modePrefix;
        this.modeFlag = modeFlag;
    }

    public char getModePrefix() {
        return modePrefix;
    }

    public char getModeFlag() {
        return modeFlag;
    }

    public static ChannelMode getModeByPrefix(char modeChar) {
        for (ChannelMode mode : values()) {
            if (mode.getModePrefix() == modeChar)
                return mode;
        }
        return null;
    }

    public static ChannelMode getModeByFlag(char modeChar) {
        for (ChannelMode mode : values()) {
            if (mode.getModeFlag() == modeChar)
                return mode;
        }
        return null;
    }
}
