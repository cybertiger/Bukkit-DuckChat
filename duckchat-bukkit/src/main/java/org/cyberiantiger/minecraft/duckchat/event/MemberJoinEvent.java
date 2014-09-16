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
public class MemberJoinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private String host;
    private String identifier;
    private String name;

    public MemberJoinEvent(String host, String identifier, String name) {
        super(!Bukkit.isPrimaryThread());
        this.host = host;
        this.identifier = identifier;
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }
}