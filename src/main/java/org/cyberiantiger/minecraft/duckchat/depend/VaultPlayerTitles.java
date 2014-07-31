/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.depend;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.cyberiantiger.minecraft.duckchat.Main;

/**
 *
 * @author antony
 */
public class VaultPlayerTitles implements PlayerTitles {
    private final Main plugin;
    private Chat vaultChat;

    public VaultPlayerTitles(Main plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> chatProvider =
                    plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
            if (chatProvider != null) {
                vaultChat = chatProvider.getProvider();
            }
        }
    }

    public String getPrefix(final Player player) {
        if (vaultChat == null) {
            return "";
        }
        if (!plugin.getServer().isPrimaryThread()) {
            Future<String> callSyncMethod = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return getPrefix(player);
                }
            });
            try {
                return callSyncMethod.get();
            } catch (InterruptedException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
                return "";
            } catch (ExecutionException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
                return "";
            }
        }
        return vaultChat.getPlayerPrefix(player).replace('&', ChatColor.COLOR_CHAR);
    }

    public String getSuffix(final Player player) {
        if (vaultChat == null) {
            return "";
        }
        if (!plugin.getServer().isPrimaryThread()) {
            Future<String> callSyncMethod = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return getSuffix(player);
                }
            });
            try {
                return callSyncMethod.get();
            } catch (InterruptedException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
                return "";
            } catch (ExecutionException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
                return "";
            }
        }
        return vaultChat.getPlayerSuffix(player).replace('&', ChatColor.COLOR_CHAR);
    }
    
}
