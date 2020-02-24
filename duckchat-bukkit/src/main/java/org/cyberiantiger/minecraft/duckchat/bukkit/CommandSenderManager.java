/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.metadata.Metadatable;

/**
 *
 * @author antony
 */
public class CommandSenderManager {
    private static final String REPLY_ADDRESS = "duckchat.replyAddress";
    private static final String CURRENT_CHANNEL = "duckchat.currentChannel";

    private final Main plugin;

    public CommandSenderManager(Main plugin) {
        this.plugin = plugin;
    }

    public String getIdentifier(CommandSender player) {
        if (player instanceof Player) {
            if (plugin.useUUIDs()) {
                return ((Player)player).getUniqueId().toString();
            } else {
                return player.getName();
            }
        } else if (player instanceof ConsoleCommandSender) {
            return "dc:console:" + plugin.getState().getLocalAddress();
        }
        return null;
    }
    
    public CommandSender getPlayer(String identifier) {
        if (identifier == null) {
            return null;
        } else if (identifier.startsWith("dc:")) {
            if (identifier.equals("dc:console:" + plugin.getState().getLocalNodeName())) {
                return plugin.getServer().getConsoleSender();
            } else {
                return null;
            }
        } else {
            if (plugin.useUUIDs()) {
                return plugin.getServer().getPlayer(UUID.fromString(identifier));
            } else {
                return plugin.getServer().getPlayer(identifier);
            }
        }
    }

    public String getName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return plugin.translate("sender.console", sender.getName(), plugin.getState().getLocalNodeName());
        }
        return sender.getName();
    }

    public String getDisplayName(CommandSender sender) {
        if (sender instanceof Player) {
            return getDisplayName((Player)sender);
        }
        return getName(sender);
    }

    public String getDisplayName(final Player player) {
        if (!plugin.getServer().isPrimaryThread()) {
            Future<String> callSyncMethod = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return getDisplayName(player);
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
        return player.getDisplayName();
    }

    public String getWorld(CommandSender sender) {
        if (sender instanceof Player) {
            return getWorld((Player)sender);
        }
        return "";
    }

    public String getWorld(final Player sender) {
        if (!plugin.getServer().isPrimaryThread()) {
            Future<String> callSyncMethod = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return getWorld(sender);
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
        return sender.getWorld().getName();
    }
    
    public String getPrefix(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return plugin.getPlayerTitles().getPrefix(player);
        }
        return "";
    }

    public String getSuffix(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return plugin.getPlayerTitles().getSuffix(player);
        }
        return "";
    }

    public boolean hasPermission(String identifier, String permission) {
        return hasPermission(getPlayer(identifier), permission);
    }

    public boolean hasPermission(final CommandSender sender, final String permission) {
        if (sender == null) {
            return false;
        }
        if (!plugin.getServer().isPrimaryThread()) {
            Future<Boolean> callSyncMethod = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return hasPermission(sender, permission);
                }
            });
            try {
                return callSyncMethod.get();
            } catch (InterruptedException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
                return false;
            } catch (ExecutionException ex) {
                plugin.getLogger().log(Level.WARNING, null, ex);
                return false;
            }
        }
        return sender.hasPermission(permission);
    }

    public void sendMessage(final String identifier, final String message) {
        final CommandSender sender = getPlayer(identifier);
        sendMessage(sender, message);
    }

    public void sendMessage(final CommandSender sender, final String message) {
        if (sender instanceof Player) {
            // This is threadsafe, or close enough.
            ((Player)sender).sendRawMessage(message);
        } else if (sender instanceof ConsoleCommandSender) {
            // So is this.
            ((ConsoleCommandSender)sender).sendRawMessage(message);
        } else {
            if (sender == null) {
                return;
            }
            if (plugin.getServer().isPrimaryThread()) {
                sender.sendMessage(message);
            } else {
                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        // This definitely is not.
                        sender.sendMessage(message);
                    }
                });
            }
        }
    }

    private final AtomicReference<String> consoleReplyAddress = 
            new AtomicReference<String>(null);

    public void setReplyAddress(final String target, final String replyTo) {
        final CommandSender sender = getPlayer(target);
        if (sender != null) setReplyAddress(sender, replyTo);
    }

    public void setReplyAddress(final CommandSender sender, final String replyTo) {
        if (sender instanceof Metadatable) {
            if (plugin.getServer().isPrimaryThread()) {
                // Fuck the metadata API and the horse it rode in on.
                Metadatable m = (Metadatable) sender;
                m.setMetadata(REPLY_ADDRESS, new MetadataValueAdapter(plugin) {
                    @Override
                    public Object value() {
                        return replyTo;
                    }
                    
                    @Override
                    public void invalidate() {}
                });
            } else {
                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        setReplyAddress(sender, replyTo);
                    }
                });
            }
        } else if (sender instanceof ConsoleCommandSender) {
            consoleReplyAddress.set(replyTo);
        }
    }

    public String getReplyAddress(final String target) {
        final CommandSender sender = getPlayer(target);
        return sender == null ? null : getReplyAddress(sender);
    }

    public String getReplyAddress(final CommandSender target) {
        if (target instanceof Metadatable) {
            if (plugin.getServer().isPrimaryThread()) {
                // Fuck the metadata API and the horse it rode in on.
                Metadatable m = (Metadatable) target;
                if (m.hasMetadata(REPLY_ADDRESS)) {
                    List<MetadataValue> metadata = m.getMetadata(REPLY_ADDRESS);
                    return metadata.get(0).asString();
                }
            } else {
                Future<String> callSyncMethod = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        return getReplyAddress(target);
                    }
                });
                try {
                    return callSyncMethod.get();
                } catch (InterruptedException ex) {
                    plugin.getLogger().log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    plugin.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        } else if (target instanceof ConsoleCommandSender) {
            return consoleReplyAddress.get();
        }
        return null;
    }

    public void broadcast(final String message) {
        if (plugin.getServer().isPrimaryThread()) {
            plugin.getServer().broadcastMessage(message);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

                @Override
                public void run() {
                    broadcast(message);
                }
            });
        }
    }

    private final AtomicReference<String> consoleCurrentChannel = 
            new AtomicReference<String>(null);

    public void setCurrentChannel(final CommandSender sender, final String channel) {
        if (sender instanceof Metadatable) {
            if (plugin.getServer().isPrimaryThread()) {
                // Fuck the metadata API and the horse it rode in on.
                Metadatable m = (Metadatable) sender;
                m.setMetadata(CURRENT_CHANNEL, new MetadataValueAdapter(plugin) {
                    @Override
                    public Object value() {
                        return channel;
                    }
                    
                    @Override
                    public void invalidate() {}
                });
            } else {
                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        setCurrentChannel(sender, channel);
                    }
                });
            }
        } else if (sender instanceof ConsoleCommandSender) {
            consoleCurrentChannel.set(channel);
        }
    }

    public String getCurrentChannel(final CommandSender sender) {
        if (sender instanceof Metadatable) {
            if (plugin.getServer().isPrimaryThread()) {
                // Fuck the metadata API and the horse it rode in on.
                Metadatable m = (Metadatable) sender;
                if (m.hasMetadata(CURRENT_CHANNEL)) {
                    List<MetadataValue> metadata = m.getMetadata(CURRENT_CHANNEL);
                    String result = metadata.get(0).asString();
                    if (result != null) {
                        return result;
                    }
                }
            } else {
                Future<String> callSyncMethod = plugin.getServer().getScheduler().callSyncMethod(plugin, new Callable<String>() {
                    
                    @Override
                    public String call() throws Exception {
                        return getCurrentChannel(sender);
                    }
                });
                try {
                    return callSyncMethod.get();
                } catch (InterruptedException ex) {
                    plugin.getLogger().log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    plugin.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            String result = consoleCurrentChannel.get();
            return result == null ? plugin.getDefaultChannel() : result;

        }
        return plugin.getDefaultChannel();
    }
}
