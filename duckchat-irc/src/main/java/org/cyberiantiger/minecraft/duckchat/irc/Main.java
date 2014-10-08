/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.PermissionException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SenderTypeException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.UsageException;
import org.cyberiantiger.minecraft.duckchat.bukkit.state.StateManager;
import org.cyberiantiger.minecraft.duckchat.irc.config.Config;
import org.cyberiantiger.minecraft.duckchat.irc.config.IRCLinkConfig;
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

    private IRCCommandExecutor commandExecutor;
    private org.cyberiantiger.minecraft.duckchat.bukkit.Main duckChat;
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
        commandExecutor = new IRCCommandExecutor(this);
        getCommand("duckchatirc").setExecutor(commandExecutor);
        getCommand("duckchatirc").setTabCompleter(commandExecutor);
        duckChat = (org.cyberiantiger.minecraft.duckchat.bukkit.Main) getServer().getPluginManager().getPlugin("DuckChat");
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