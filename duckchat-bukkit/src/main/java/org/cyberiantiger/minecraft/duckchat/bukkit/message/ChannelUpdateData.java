/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

import java.util.BitSet;

/**
 *
 * @author antony
 */
public class ChannelUpdateData extends Data {

    private final String name;
    private final String messageFormat;
    private final String actionFormat;
    private final BitSet flags;
    private final String permission;

    public ChannelUpdateData(String name, String messageFormat, String actionFormat, BitSet flags, String permission) {
        this.name = name;
        this.messageFormat = messageFormat;
        this.actionFormat = actionFormat;
        this.flags = flags;
        this.permission = permission;
    }

    public String getName() {
        return name;
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

    @Override
    public DataType getType() {
        return DataType.CHANNEL_UPDATE;
    }

    @Override
    public String toString() {
        return "ChannelUpdateData{" + "name=" + name + ", messageFormat=" + messageFormat + ", actionFormat=" + actionFormat + ", flags=" + flags + ", permission=" + permission + '}';
    }
}
