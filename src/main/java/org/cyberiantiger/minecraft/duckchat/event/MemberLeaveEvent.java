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
public class MemberLeaveEvent extends Event {

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

    public MemberLeaveEvent(String host, String identifier, String name) {
        super(!Main.isServerThread());
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