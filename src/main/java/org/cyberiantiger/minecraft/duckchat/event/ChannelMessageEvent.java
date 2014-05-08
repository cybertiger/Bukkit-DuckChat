/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.cyberiantiger.minecraft.duckchat.Main;

/**
 *
 * @author antony
 */
public class ChannelMessageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String source;
    private final String channel;
    private final String message;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ChannelMessageEvent(String source, String channel, String message) {
        super (!Main.isServerThread());
        this.source = source;
        this.channel = channel;
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }
}
