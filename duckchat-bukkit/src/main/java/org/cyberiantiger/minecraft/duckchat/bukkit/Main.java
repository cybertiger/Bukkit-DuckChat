/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import org.cyberiantiger.minecraft.duckchat.bukkit.state.DuckReceiver;
import org.cyberiantiger.minecraft.duckchat.bukkit.state.ChatChannel;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelMessageData;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommand;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.BroadcastSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.ChannelListSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.ChannelSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.ChannelsSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.JoinSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.MeSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.MessageSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.PartSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.PermissionException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.ReloadSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.ReplySubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.RexecSubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SaySubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SenderTypeException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.UsageException;
import org.cyberiantiger.minecraft.duckchat.bukkit.config.ChannelConfig;
import org.cyberiantiger.minecraft.duckchat.bukkit.config.Config;
import org.cyberiantiger.minecraft.duckchat.bukkit.depend.PlayerTitles;
import org.cyberiantiger.minecraft.duckchat.bukkit.depend.VaultPlayerTitles;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelCreateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelJoinData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MemberCreateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MemberDeleteData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MemberUpdateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelPartData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.Data;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MessageData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.PluginMessageData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ServerCreateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.state.StateManager;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
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
    private static final String LANGUAGE = "language.yml";

    private final StateManager state = new StateManager(this);
    private final CommandSenderManager commandSenderManager = new CommandSenderManager(this);
    private final AtomicReference<PlayerTitles> playerTitles = new AtomicReference<PlayerTitles>(PlayerTitles.DEFAULT);

    private Config config;
    private Channel channel;
    /*
    private String clusterName;
    private boolean useUUIDs;
    private boolean registerPermissions;
    private String defaultChannel;
    */


    // Messages.
    private final Map<String,String> messages = new HashMap<String,String>();

    public CommandSenderManager getCommandSenderManager() {
        return commandSenderManager;
    }

    public StateManager getState() {
        return state;
    }

    public PlayerTitles getPlayerTitles() {
        return playerTitles.get();
    }

    public boolean useUUIDs() {
        return config.isUseUUIDs();
    }

    public String getDefaultChannel() {
        return config.getDefaultChannel();
    }

    public Map<String,String> getShortcuts() {
        return config.getShortcuts();
    }

    public Config getConfiguration() {
        return config;
    }

    private void connect() throws Exception {
        // XXX: Setting stuff globally is bad
        System.setProperty("java.net.preferIPv4Stack", String.valueOf(config.isUseIPv4()));
        System.setProperty("jgroups.bind_addr", config.getBindAddress());
        String nodename = config.getNodeName() == null ? getServer().getServerName() : config.getNodeName();
        if (config.getNetwork() != null) {
            File networkConfig = new File(getDataFolder(), config.getNetwork());
            channel = new JChannel(networkConfig);
        } else {
            channel = new JChannel();
        }
        if (nodename != null) {
            channel.setName(nodename);
        }
        // This is very spammy and leaves the diagnostics thread spinning after
        // a channel.close();
        channel.getProtocolStack().getTransport().disableDiagnostics();
        channel.setReceiver(new DuckReceiver(getState()));
        channel.connect(config.getClusterName());
        getState().setLocalAddress(channel.getAddress());
        channel.getState(null, 0);

        sendServerCreate();
        
        // Register our players.
        for (Player player : getServer().getOnlinePlayers()) {
            sendMemberCreate(player);
        }
        // Register console.
        sendMemberCreate(getServer().getConsoleSender());

        // Register our channels.
        for (Map.Entry<String, ChannelConfig> e : config.getChannels().entrySet()) {
            String name = e.getKey();
            ChannelConfig channelConfig = e.getValue();
            Address addr = null;
            if (channelConfig.isOwned()) {
                addr = channel.getAddress();
            }
            String messageFormat = channelConfig.getMessageFormat();
            String actionFormat = channelConfig.getActionFormat();
            BitSet flags = new BitSet();
            String permission = channelConfig.getPermission();
            flags.set(ChatChannel.FLAG_LOCAL_AUTO_JOIN, channelConfig.isLocalAutoJoin());
            flags.set(ChatChannel.FLAG_GLOBAL_AUTO_JOIN, channelConfig.isGlobalAutoJoin());
            sendData(new ChannelCreateData(addr, name, messageFormat, actionFormat, flags, permission));
        }
        
    }

    // Net
    private void disconnect() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        state.clear();
    }

    private File getConfigFile() {
        return new File(getDataFolder(), CONFIG);
    }

    private File getLanguageFile() {
        return new File(getDataFolder(), LANGUAGE);
    }

    private void copyDefault(String name, File dest) {
        if (!dest.exists()) {
            try {
                InputStream in = getClass().getClassLoader().getResourceAsStream(name);
                if (in != null) {
                    try {
                        OutputStream out = new FileOutputStream(dest);
                        try {
                            ByteStreams.copy(in, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, "Error copying default " + dest, ex);
            }
        }
    }

    private void copyDefaults() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        copyDefault(CONFIG, getConfigFile());
        copyDefault(LANGUAGE, getLanguageFile());
    }
    
    private void loadConfig() {
        config = new Config();
        try {
            Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(Config.class, getClass().getClassLoader()));
            configLoader.setBeanAccess(BeanAccess.FIELD);
            config = configLoader.loadAs(new InputStreamReader(new BufferedInputStream(new FileInputStream(getConfigFile())), Charsets.UTF_8), Config.class);
        } catch (IOException|YAMLException ex) {
            getLogger().log(Level.SEVERE, "Error loading config.yml", ex);
            getLogger().severe("Your config.yml has fatal errors, using defaults.");
        }
        this.messages.clear();
        try {
            Yaml languageLoader = new Yaml();
            Map<String, String> messages = (Map<String, String>) languageLoader.load( new InputStreamReader( new BufferedInputStream( getClass().getClassLoader().getResourceAsStream(LANGUAGE)), Charsets.UTF_8));
            for (Map.Entry<String,String> e : messages.entrySet()) {
                this.messages.put(e.getKey(), e.getValue().replace('&', ChatColor.COLOR_CHAR));
            }
            this.messages.putAll(messages);
        } catch (YAMLException ex) {
            getLogger().log(Level.SEVERE, "Error loading default language.yml", ex);
        }
        try {
            Yaml languageLoader = new Yaml();
            Map<String, String> messages = (Map<String, String>) languageLoader.load(new InputStreamReader(new BufferedInputStream(new FileInputStream(getLanguageFile())), Charsets.UTF_8));
            for (Map.Entry<String,String> e : messages.entrySet()) {
                this.messages.put(e.getKey(), e.getValue().replace('&', ChatColor.COLOR_CHAR));
            }
        } catch (IOException|YAMLException ex) {
            getLogger().log(Level.SEVERE, "Error loading language.yml", ex);
            getLogger().severe("Your language.yml has fatal errors, using defaults.");
        }
    }

    @Override
    public void onEnable() {
        copyDefaults();
        loadConfig();
        getServer().getPluginManager().registerEvents(new DuckListener(this), this);
        try {
            connect();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to open channel", ex);
            disconnect();
        }
        getServer().getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
                playerTitles.set(new VaultPlayerTitles(Main.this));
            }
        });
    }

    public void reload() {
        disconnect();
        loadConfig();
        try {
            connect();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to open channel", ex);
        }
    }

    @Override
    public void onDisable() {
        channel.close();
        channel = null;
    }

    private Map<String, SubCommand> subcommands = new LinkedHashMap<String, SubCommand>();
    {
        SubCommand channelsSubCommand = new ChannelsSubCommand(this);
        SubCommand channelSubCommand = new ChannelSubCommand(this);
        SubCommand joinSubCommand = new JoinSubCommand(this);
        SubCommand partSubCommand = new PartSubCommand(this);
        SubCommand meSubCommand = new MeSubCommand(this);
        SubCommand channelListSubCommand = new ChannelListSubCommand(this);
        SubCommand messageSubCommand = new MessageSubCommand(this);
        SubCommand replySubCommand = new ReplySubCommand(this);
        SubCommand reloadSubCommand = new ReloadSubCommand(this);
        SubCommand saySubCommand = new SaySubCommand(this);
        SubCommand broadcastSubCommand = new BroadcastSubCommand(this);
        SubCommand rexecSubCommand = new RexecSubCommand(this);

        subcommands.put("channels", channelsSubCommand);
        subcommands.put("channel", channelSubCommand);
        subcommands.put("channellist", channelListSubCommand);
        subcommands.put("chlist", channelListSubCommand);
        subcommands.put("join", joinSubCommand);
        subcommands.put("part", partSubCommand);
        subcommands.put("msg", messageSubCommand);
        subcommands.put("message", messageSubCommand);
        subcommands.put("whisper", messageSubCommand);
        subcommands.put("tell", messageSubCommand);
        subcommands.put("reply", replySubCommand);
        subcommands.put("reload", reloadSubCommand);
        subcommands.put("rexec", rexecSubCommand);
        subcommands.put("dcreload", reloadSubCommand);
        subcommands.put("broadcast", broadcastSubCommand);
        subcommands.put("bc", broadcastSubCommand);
        subcommands.put("say", saySubCommand);
        subcommands.put("me", meSubCommand);
        subcommands.put("m", messageSubCommand);
        subcommands.put("r", replySubCommand);
        subcommands.put("t", messageSubCommand);
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

    private String COLOR_CODES = "0123456789abcdefABCDEF";
    private String FORMAT_CODES = "klmnorKLMNOR";

    public String formatColors(String message, boolean allowColor, boolean allowFormat) {
        if (!allowColor && !allowFormat) {
            return message;
        }
        StringBuilder result = new StringBuilder();
        boolean special = false;
        for (int i = 0 ; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (special) {
                if ((allowColor && COLOR_CODES.indexOf(ch) != -1) || (allowFormat && FORMAT_CODES.indexOf(ch) != -1)) {
                    result.append(ChatColor.COLOR_CHAR).append(ch);
                } else {
                    result.append('&').append(ch);
                }
                special = false;
            } else {
                if (ch == '&') {
                    special = true;
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    public void sendData(Data data) {
        sendData(null, data);
    }

    public void sendData(final Address target, final Data data) {
        if (!getServer().isPrimaryThread()) {
            try {
                channel.send(target, data);
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Error sending network message", ex);
            }
        } else {
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {

                @Override
                public void run() {
                    sendData(target, data);
                }
            });
        }
    }

    public void sendServerCreate() {
        sendData(new ServerCreateData(channel.getName()));
    }

    public void sendMemberCreate(CommandSender player) {
        String playerName;
        if (player instanceof ConsoleCommandSender) {
            playerName = "console@" + channel.getName();
        } else {
            playerName = player.getName();
        }
        sendData(new MemberCreateData(getCommandSenderManager().getIdentifier(player), playerName, new BitSet()));
    }

    public void sendMemberUpdate(Player player) {
        sendData(new MemberUpdateData(getCommandSenderManager().getIdentifier(player), new BitSet()));
    }

    public void sendMemberDelete(Player player) {
        sendData(new MemberDeleteData(getCommandSenderManager().getIdentifier(player)));
    }

    public void sendJoinChannel(String channelName, CommandSender sender) {
        sendJoinChannel(channelName, getCommandSenderManager().getIdentifier(sender));
    }

    public void sendJoinChannel(String channelName, String identifier) {
        sendData(new ChannelJoinData(channelName, identifier));
    }

    public void sendPartChannel(CommandSender player, String channelName) {
        sendData(new ChannelPartData(channelName, getCommandSenderManager().getIdentifier(player), player.getName()));
    }

    public void sendChannelAction(CommandSender sender, String action) {
        String channel = getCommandSenderManager().getCurrentChannel(sender);
        if (channel == null) {
            getCommandSenderManager().sendMessage(sender, translate("chat.nochannel"));
            return;
        }
        sendChannelAction(sender, channel, action);
    }

    public void sendChannelAction(CommandSender sender, String channelName, String action) {
        String format;
        if (!getState().isChannelMember(getCommandSenderManager().getIdentifier(sender),channelName)) {
            getCommandSenderManager().sendMessage(sender, translate("chat.nochannel"));
            return;
        }
        format = getState().getChannelActionFormat(channelName);
        boolean allowColor = getCommandSenderManager().hasPermission(sender, "duckchat.chat.color");
        boolean allowFormat = getCommandSenderManager().hasPermission(sender, "duckchat.chat.format");
        action = formatColors(action, allowColor, allowFormat);
        action = String.format(
                format,
                channelName,
                getState().getLocalNodeName(),
                getCommandSenderManager().getWorld(sender),
                getCommandSenderManager().getName(sender),
                getCommandSenderManager().getPrefix(sender),
                getCommandSenderManager().getDisplayName(sender),
                getCommandSenderManager().getSuffix(sender),
                action);
        sendChannelMessage(getCommandSenderManager().getIdentifier(sender), channelName, action);
    }

    public void sendChannelMessage(CommandSender sender, String message) {
        String channel = getCommandSenderManager().getCurrentChannel(sender);
        if (channel == null) {
            getCommandSenderManager().sendMessage(sender,translate("chat.nochannel"));
            return;
        }
        sendChannelMessage(sender, channel, message);
    }

    public void sendChannelMessage(CommandSender sender, String channelName, String message) {
        String format;
        if (!getState().isChannelMember(getCommandSenderManager().getIdentifier(sender),channelName)) {
            getCommandSenderManager().sendMessage(sender, translate("chat.nochannel"));
            return;
        }
        boolean allowColor = getCommandSenderManager().hasPermission(sender, "duckchat.chat.color");
        boolean allowFormat = getCommandSenderManager().hasPermission(sender, "duckchat.chat.format");
        message = formatColors(message, allowColor, allowFormat);
        format = getState().getChannelMessageFormat(channelName);
        message = String.format(
                format,
                channelName,
                getState().getLocalNodeName(),
                getCommandSenderManager().getWorld(sender),
                getCommandSenderManager().getName(sender),
                getCommandSenderManager().getPrefix(sender),
                getCommandSenderManager().getDisplayName(sender),
                getCommandSenderManager().getSuffix(sender),
                message);
        sendChannelMessage(getCommandSenderManager().getIdentifier(sender), channelName, message);
    }

    public void sendChannelMessage(String playerIdentity, String channelName, String message) {
        sendData(new ChannelMessageData(playerIdentity, channelName, message));
    }

    public void sendMessage(CommandSender from, String to, String message) {
        Address toAddress = getState().getMemberAddress(to);
        Address fromAddress = getState().getLocalAddress();
        if (toAddress == null || fromAddress == null) {
            return;
        }
        boolean allowColor = getCommandSenderManager().hasPermission(from, "duckchat.msg.color");
        boolean allowFormat = getCommandSenderManager().hasPermission(from, "duckchat.msg.format");
        message = formatColors(message, allowColor, allowFormat);
        sendData(toAddress, new MessageData(getCommandSenderManager().getIdentifier(from), to, message));
        if (fromAddress != toAddress) {
            sendData(fromAddress, new MessageData(getCommandSenderManager().getIdentifier(from), to, message));
        }
    }

    public void sendBroadcast(String broadcast) {
        sendBroadcast(null, broadcast);
    }

    public void sendBroadcast(Address destination, String broadcast) {
        sendData(destination, new MessageData(null, null, broadcast));
    }

    public void sendPluginMessage(String channel, byte[] data) {
        sendPluginMessage(null, channel, data);
    }

    public void sendPluginMessage(Address destination, String channel, byte[] data) {
        sendData(destination, new PluginMessageData(channel, data));
    }

    public String translate(String key, Object... args) {
        if (!messages.containsKey(key)) {
            return "Unknown message:" + key;
        } else {
            return String.format(messages.get(key), args);
        }
    }

    public String getNodeName(Address addr) {
        return channel.getName(addr);
    }
}