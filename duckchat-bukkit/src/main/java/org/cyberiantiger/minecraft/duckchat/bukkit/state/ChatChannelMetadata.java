/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.state;

import java.util.BitSet;

/**
 *
 * @author antony
 */
public class ChatChannelMetadata {

    private final String messageFormat;
    private final String actionFormat;
    private final BitSet flags;
    private final String permission;
    private final long spamWindow;
    private final int spamThreshold;
    private final long repeatWindow;
    private final int repeatThreshold;


    public ChatChannelMetadata(String messageFormat, String actionFormat, BitSet flags, String permission, long spamWindow, int spamThreshold, long repeatWindow, int repeatThreshold) {
        this.messageFormat = messageFormat;
        this.actionFormat = actionFormat;
        this.flags = flags;
        this.permission = permission;
        this.spamWindow = spamWindow;
        this.spamThreshold = spamThreshold;
        this.repeatWindow = repeatWindow;
        this.repeatThreshold = repeatThreshold;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public String getActionFormat() {
        return actionFormat;
    }

    public BitSet getFlags() {
        return flags;
    }

    public String getPermission() {
        return permission;
    }

    public long getSpamWindow() {
        return spamWindow;
    }

    public int getSpamThreshold() {
        return spamThreshold;
    }

    public long getRepeatWindow() {
        return repeatWindow;
    }

    public int getRepeatThreshold() {
        return repeatThreshold;
    }

    @Override
    public String toString() {
        return "ChatChannelMetadata{" + "messageFormat=" + messageFormat + ", actionFormat=" + actionFormat + ", flags=" + flags + ", permission=" + permission + ", spamWindow=" + spamWindow + ", spamThreshold=" + spamThreshold + ", repeatWindow=" + repeatWindow + ", repeatThreshold=" + repeatThreshold + '}';
    }
}
