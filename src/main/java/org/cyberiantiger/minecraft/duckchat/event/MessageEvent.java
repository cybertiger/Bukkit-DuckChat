/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author antony
 */
public class MessageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String fromIdentifier;
    private final String toIdentifier;
    private final String message;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public MessageEvent(String fromIdentifier, String toIdentifier, String message) {
        super(!Bukkit.isPrimaryThread());
        this.fromIdentifier = fromIdentifier;
        this.toIdentifier = toIdentifier;
        this.message = message;
    }

    public String getFromIdentifier() {
        return fromIdentifier;
    }

    public String getToIdentifier() {
        return toIdentifier;
    }

    public String getMessage() {
        return message;
    }
}
