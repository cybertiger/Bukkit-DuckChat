/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.jgroups.Address;

/**
 *
 * @author antony
 */
public class ServerLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Address addr;
    private final String name;
    public ServerLeaveEvent(Address addr, String name) {
        super(!Main.isServerThread());
        this.addr = addr;
        this.name = name;
    }

    public Address getAddr() {
        return addr;
    }

    public String getName() {
        return name;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
