/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberJoinEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberLeaveEvent;

/**
 *
 * @author antony
 */
public class DuckListener implements Listener {
    private final Main plugin;

    public DuckListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        plugin.sendChannelMessage(e.getPlayer(), e.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.sendMemberCreate(e.getPlayer());
        e.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.sendMemberDelete(e.getPlayer());
        e.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        plugin.sendMemberDelete(e.getPlayer());
        e.setLeaveMessage(null);
    }

    @EventHandler
    public void onMemberJoin(MemberJoinEvent e) {
        plugin.getCommandSenderManager().broadcast(plugin.translate("member.join", e.getName(), e.getHost()));
    }

    @EventHandler
    public void onMemberLeave(MemberLeaveEvent e) {
        plugin.getCommandSenderManager().broadcast(plugin.translate("member.leave", e.getName(), e.getHost()));
    }
}