/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.config;

/**
 *
 * @author antony
 */
public class ChannelConfig {
    private boolean owned = false;
    private boolean localAutoJoin = false;
    private boolean globalAutoJoin = true;
    private String messageFormat = "[%1$s %6$s] %8$s";
    private String actionFormat = "[%1$s] %6$s %8$s";
    private String permission = null;
    private long spamWindow = -1L;
    private int spamThreshold = -1;
    private long repeatWindow = -1L;
    private int repeatThreshold = -1;

    public boolean isOwned() {
        return owned;
    }

    public boolean isLocalAutoJoin() {
        return localAutoJoin;
    }

    public boolean isGlobalAutoJoin() {
        return globalAutoJoin;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public String getActionFormat() {
        return actionFormat;
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
}
