/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.MemberJoinEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.MemberLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.PluginMessageEvent;
import sun.nio.cs.StandardCharsets;

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
        if (plugin.getConfiguration().isNotifyPlayerJoin()) {
            e.setJoinMessage(null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCommandPreProcessEvent(PlayerCommandPreprocessEvent e) {
        String identifier = plugin.getCommandSenderManager().getIdentifier(e.getPlayer());
        if (identifier == null) {
            return;
        }
        for (Map.Entry<String,String> ee : plugin.getShortcuts().entrySet()) {
            String key = ee.getKey();
            if (e.getMessage().startsWith(key)) {
                String destChannel = ee.getValue();
                e.setCancelled(true);
                String msg = e.getMessage().substring(key.length());
                if (msg.length() > 0) {
                    plugin.sendChannelMessage(e.getPlayer(), destChannel, msg);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.sendMemberDelete(e.getPlayer());
        if (plugin.getConfiguration().isNotifyPlayerLeave()) {
            e.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        plugin.sendMemberDelete(e.getPlayer());
        e.setLeaveMessage(null);
    }

    @EventHandler
    public void onMemberJoin(MemberJoinEvent e) {
        if (plugin.getConfiguration().isNotifyPlayerJoin()) {
            plugin.getCommandSenderManager().broadcast(plugin.translate("member.join", e.getName(), e.getHost()));
        }
    }

    @EventHandler
    public void onMemberLeave(MemberLeaveEvent e) {
        if (plugin.getConfiguration().isNotifyPlayerLeave()) {
            plugin.getCommandSenderManager().broadcast(plugin.translate("member.leave", e.getName(), e.getHost()));
        }
    }
    
    @EventHandler
    public void onPluginMessageEvent(final PluginMessageEvent e) {
        if (Bukkit.isPrimaryThread()) {
            if ("command".equals(e.getChannel())) {
                final String cmd = new String(e.getData(), Charsets.UTF_8);
                
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            }
        } else {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    onPluginMessageEvent(e);
                }
            });
        }
    }
}