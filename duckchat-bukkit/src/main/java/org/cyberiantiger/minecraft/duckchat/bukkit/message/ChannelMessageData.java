/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

/**
 *
 * @author antony
 */
public class ChannelMessageData extends Data {
    private final String identifier;
    private final String channel;
    private final String message;


    public ChannelMessageData(String identifier, String channel, String message) {
        this.identifier = identifier;
        this.channel = channel;
        this.message = message;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public DataType getType() {
        return DataType.CHANNEL_MESSAGE;
    }

    @Override
    public String toString() {
        return "ChannelMessageData{" + "identifier=" + identifier + ", channel=" + channel + ", message=" + message + '}';
    }
}
