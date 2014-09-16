/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat;

import org.cyberiantiger.minecraft.duckchat.state.DuckReceiver;
import org.cyberiantiger.minecraft.duckchat.state.ChatChannel;
import org.cyberiantiger.minecraft.duckchat.message.ChannelMessageData;
import org.cyberiantiger.minecraft.duckchat.command.SubCommand;
import java.io.File;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.cyberiantiger.minecraft.duckchat.command.BroadcastSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.ChannelListSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.ChannelSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.ChannelsSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.JoinSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.MeSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.MessageSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.PartSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.PermissionException;
import org.cyberiantiger.minecraft.duckchat.command.ReloadSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.ReplySubCommand;
import org.cyberiantiger.minecraft.duckchat.command.RexecSubCommand;
import org.cyberiantiger.minecraft.duckchat.command.SaySubCommand;
import org.cyberiantiger.minecraft.duckchat.command.SenderTypeException;
import org.cyberiantiger.minecraft.duckchat.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.command.UsageException;
import org.cyberiantiger.minecraft.duckchat.depend.PlayerTitles;
import org.cyberiantiger.minecraft.duckchat.depend.VaultPlayerTitles;
import org.cyberiantiger.minecraft.duckchat.message.ChannelCreateData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelJoinData;
import org.cyberiantiger.minecraft.duckchat.message.MemberCreateData;
import org.cyberiantiger.minecraft.duckchat.message.MemberDeleteData;
import org.cyberiantiger.minecraft.duckchat.message.MemberUpdateData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelPartData;
import org.cyberiantiger.minecraft.duckchat.message.Data;
import org.cyberiantiger.minecraft.duckchat.message.MessageData;
import org.cyberiantiger.minecraft.duckchat.message.PluginMessageData;
import org.cyberiantiger.minecraft.duckchat.message.ServerCreateData;
import org.cyberiantiger.minecraft.duckchat.state.StateManager;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;

/**
 *
 * @author antony
 */
public class Main extends JavaPlugin implements Listener {

    private final StateManager state = new StateManager(this);
    private final CommandSenderManager commandSenderManager = new CommandSenderManager(this);
    private final AtomicReference<PlayerTitles> playerTitles = new AtomicReference<PlayerTitles>(PlayerTitles.DEFAULT);

    private String clusterName;
    private Channel channel;
    private boolean useUUIDs;
    private boolean registerPermissions;
    private String defaultChannel;


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
        return useUUIDs;
    }

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public boolean getRegisterPermissions() {
        return registerPermissions;
    }


    // Net
    private void connect() throws Exception {
        FileConfiguration config = getConfig();
        String nodename = config.getString("nodename");
        if (config.isString("network")) {
            File networkConfig = new File(getDataFolder(), config.getString("network"));
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
        channel.connect(clusterName);
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
        if (config.isConfigurationSection("channels")) {
            ConfigurationSection channelsSection = config.getConfigurationSection("channels");
            for (String key : channelsSection.getKeys(false)) {
                ConfigurationSection channelSection = channelsSection.getConfigurationSection(key);
                Address addr = null;
                if (channelSection.getBoolean("owned", true)) {
                    addr = channel.getAddress();
                }
                String messageFormat = channelSection.getString("messageFormat", "[%1$s %5$s%6$s%7$s@%2$s] %8$s");
                String actionFormat = channelSection.getString("actionFormat", "[%1$s] %5$s%6$s%7$s@%2$s %8$s");
                BitSet flags = new BitSet();
                String permission = channelSection.getString("permission");
                flags.set(ChatChannel.FLAG_LOCAL_AUTO_JOIN, channelSection.getBoolean("localAutoJoin", false));
                flags.set(ChatChannel.FLAG_GLOBAL_AUTO_JOIN, channelSection.getBoolean("globalAutoJoin", false));
                sendData(new ChannelCreateData(addr, key, messageFormat, actionFormat, flags, permission));
            }
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

    private void load() {
        FileConfiguration config = getConfig();
        clusterName = config.getString("clusterName", "duckchat");
        defaultChannel = config.getString("defaultChannel");
        useUUIDs = config.getBoolean("useUUIDs", true);
        registerPermissions = config.getBoolean("registerPermissions", true);
        
        if (config.isConfigurationSection("messages")) {
            ConfigurationSection messageSection = config.getConfigurationSection("messages");
            for (String key : messageSection.getKeys(true)) {
                if (messageSection.isString(key)) {
                    messages.put(key, messageSection.getString(key).replace('&', '\u00a7'));
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new DuckListener(this), this);
        load();
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
        reloadConfig();
        load();
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