/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.message;

/**
 *
 * @author antony
 */
public class ChannelDeleteData extends Data {

    private final String name;

    public ChannelDeleteData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public DataType getType() {
        return DataType.CHANNEL_DELETE;
    }

    @Override
    public String toString() {
        return "ChannelDeleteData{" + "name=" + name + '}';
    }
}