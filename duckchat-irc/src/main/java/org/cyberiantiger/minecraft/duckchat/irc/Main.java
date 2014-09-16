/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.cyberiantiger.minecraft.duckchat.command.SubCommand;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyberiantiger.minecraft.duckchat.command.PermissionException;
import org.cyberiantiger.minecraft.duckchat.command.SenderTypeException;
import org.cyberiantiger.minecraft.duckchat.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.command.UsageException;
import org.cyberiantiger.minecraft.duckchat.irc.command.ReloadSubCommand;
import org.cyberiantiger.minecraft.duckchat.irc.config.Config;
import org.cyberiantiger.minecraft.duckchat.irc.config.IRCLinkConfig;
import org.cyberiantiger.minecraft.duckchat.state.StateManager;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 *
 * @author antony
 */
public class Main extends JavaPlugin implements Listener {
    private static final String CONFIG = "config.yml";

    private org.cyberiantiger.minecraft.duckchat.Main duckChat;
    private final List<IRCLink> ircLinks = new ArrayList();
    private final Timer reconnectTimer = new Timer();


    // Config
    private Config config;

    // Net
    private void connect() {
        if (config != null) {
            List<IRCLinkConfig> l = config.getIrcBridges();
            if (l != null) {
                for (IRCLinkConfig ircLinkConfig : l) {
                    IRCLink ircLink = new IRCLink(this, ircLinkConfig);
                    ircLinks.add(ircLink);
                    ircLink.setConnected(true);
                }
            }
        }
    }
    
    private File getConfigFile() {
        return new File(getDataFolder(), CONFIG);
    }
    

    // Net
    private void disconnect() {
        for (IRCLink ircLink : ircLinks) {
            ircLink.setConnected(false);
        }
        ircLinks.clear();
    }

    private void load() {
        try {
            Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(Config.class, getClass().getClassLoader()));
            configLoader.setBeanAccess(BeanAccess.FIELD);
            this.config = configLoader.loadAs(new FileReader(getConfigFile()), Config.class);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Error loading configuration", ex);
        } catch (YAMLException ex) {
            getLogger().log(Level.SEVERE, "Error loading configuration", ex);
        }
    }

    @Override
    public void onEnable() {
        super.saveDefaultConfig();

        duckChat = (org.cyberiantiger.minecraft.duckchat.Main) getServer().getPluginManager().getPlugin("DuckChat");
        if (duckChat == null) {
            getLogger().severe("Disabling, DuckChat not found");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        load();
        try {
            connect();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to open channel", ex);
            disconnect();
        }
    }

    public void reload() {
        disconnect();
        load();
        connect();
    }

    @Override
    public void onDisable() {
        reconnectTimer.cancel();
        disconnect();
    }

    private Map<String, SubCommand> subcommands = new LinkedHashMap<String, SubCommand>();
    {
        subcommands.put("reload", new ReloadSubCommand(this));
    }

    private void executeCommand(CommandSender sender, SubCommand cmd, String label, String[] args) {
        try {
            cmd.onCommand(sender, args);
        } catch (SenderTypeException ex) {
            sender.sendMessage(translate("error.wrongsender"));
        } catch (PermissionException ex) {
            sender.sendMessage(translate("error.permission", ex.getPermission()));
        } catch (UsageException ex) {
            sender.sendMessage(translate(cmd.getName() + ".usage", label));
        } catch (SubCommandException ex) {
            sender.sendMessage(translate("error.generic", ex.getMessage()));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check for label matches.
        for (Map.Entry<String,SubCommand> e : subcommands.entrySet()) {
                if(label.equalsIgnoreCase(e.getKey())) {
                    executeCommand(sender, e.getValue(), label, args);
                    return true;
                }
        }
        // Check for second argument matches.
        if (args.length >= 1) {
            for (Map.Entry<String,SubCommand> e : subcommands.entrySet()) {
                if (e.getKey().equalsIgnoreCase(args[0])) {
                    label += " " + args[0];
                    String[] newArgs = new String[args.length-1];
                    System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                    executeCommand(sender, e.getValue(), label, newArgs);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        for (Map.Entry<String,SubCommand> e : subcommands.entrySet()) {
            if(label.equalsIgnoreCase(e.getKey())) {
                return e.getValue().onTabComplete(sender, args);
            } else if (args.length >= 1 && e.getKey().equalsIgnoreCase(args[0])) {
                String[] newArgs = new String[args.length-1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                return e.getValue().onTabComplete(sender, newArgs);
            }
        }
        if (args.length == 1) {
            List<String> result = new ArrayList();
            String start = args[0].toLowerCase();
            for (String s : subcommands.keySet()) {
                if (s.toLowerCase().startsWith(start)) {
                    result.add(s);
                }
            }
            return result;
        }
        return null;
    }

    public Timer getReconnectTimer() {
        return reconnectTimer;
    }

    public String translate(String key, Object... args) {
        if (config != null) {
            Map<String,String> messages = config.getMessages();
            if (messages.containsKey(key)) {
                return String.format(messages.get(key), args);
            }
        }
        return duckChat.translate(key, args);
    }

    public void sendChannelMessage(String identify, String targetChannel, String format) {
        duckChat.sendChannelMessage(identify, targetChannel, format);
    }

    public StateManager getState() {
        return duckChat.getState();
    }

    public String filter(String msg) {
        if (config != null) {
            Map<String,String> filters = config.getFilters();
            if (filters != null) {
                for (Map.Entry<String,String> e : filters.entrySet()) {
                    msg = msg.replaceAll(e.getKey(), e.getValue());
                }
            }
        }
        return msg;
    }
}