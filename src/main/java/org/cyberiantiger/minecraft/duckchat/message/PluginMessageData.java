/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.message;

/**
 *
 * @author antony
 */
public class PluginMessageData extends Data {
    private final String channel;
    private final byte[] data;

    public PluginMessageData(String channel, byte[] data) {
        this.channel = channel;
        this.data = data;
    }

    @Override
    public DataType getType() {
        return DataType.PLUGIN_MESSAGE;
    }

    public String getChannel() {
        return channel;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "PluginMessageData{" + "channel=" + channel + ", data=" + data + '}';
    }
}
