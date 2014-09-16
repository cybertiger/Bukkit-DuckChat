/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

/**
 *
 * @author antony
 */
public class ChannelJoinData extends Data {
    private final String channel;
    private final String identifier;

    public ChannelJoinData(String channel, String identifier) {
        this.channel = channel;
        this.identifier = identifier;
    }

    public String getChannel() {
        return channel;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public DataType getType() {
        return DataType.CHANNEL_JOIN;
    }

    @Override
    public String toString() {
        return "ChannelJoinData{" + "channel=" + channel + ", identifier=" + identifier + '}';
    }
}
