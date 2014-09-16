/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

/**
 *
 * @author antony
 */
public class ChannelPartData extends Data {
    private final String channel;
    private final String identifier;
    private final String name;

    public ChannelPartData(String channel, String identifier, String name) {
        this.channel = channel;
        this.identifier = identifier;
        this.name = name;
    }

    public String getChannel() {
        return channel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public DataType getType() {
        return DataType.CHANNEL_PART;
    }

    @Override
    public String toString() {
        return "ChannelPartData{" + "channel=" + channel + ", identifier=" + identifier + ", name=" + name + '}';
    }
}
