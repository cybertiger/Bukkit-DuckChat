/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.cyberiantiger.minecraft.duckchat.message.ChannelMessageData;
import org.cyberiantiger.minecraft.duckchat.command.SubCommand;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
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
import org.cyberiantiger.minecraft.duckchat.command.SaySubCommand;
import org.cyberiantiger.minecraft.duckchat.command.SenderTypeException;
import org.cyberiantiger.minecraft.duckchat.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.command.UsageException;
import org.cyberiantiger.minecraft.duckchat.event.ChannelMessageEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberJoinEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.irc.ControlCodes;
import org.cyberiantiger.minecraft.duckchat.irc.IRCLink;
import org.cyberiantiger.minecraft.duckchat.message.ChannelCreateData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelJoinData;
import org.cyberiantiger.minecraft.duckchat.message.MemberCreateData;
import org.cyberiantiger.minecraft.duckchat.message.MemberDeleteData;
import org.cyberiantiger.minecraft.duckchat.message.MemberUpdateData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelPartData;
import org.cyberiantiger.minecraft.duckchat.message.MessageData;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.util.Util;

/**
 *
 * @author antony
 */
public class Main extends JavaPlugin implements Listener {

    private String clusterName;
    private Channel channel;
    private boolean useUUIDs;
    private boolean registerPermissions;
    private String defaultChannel;
    private Chat vaultChat;

    private final List<IRCLink> ircLinks = new ArrayList();

    // Players, and their current state.
    private final Map<String,CommandSenderState> commandSenderStates = new HashMap<String, CommandSenderState>();

    // Messages.
    private final Map<String,String> messages = new HashMap<String,String>();
    // All players, keyed on identifier.
    private final Map<String,Member> members = new HashMap<String,Member>();
    // All channels.
    private final Map<String,ChatChannel> channels = new HashMap<String,ChatChannel>();
    // State 
    private final Object STATE_LOCK = new Object();

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
        channel.setReceiver(new DuckReceiver(this));
        channel.connect(clusterName);
        channel.getState(null, 0);
        
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
                ChannelCreateData registerPacket = new ChannelCreateData(addr, key, messageFormat, actionFormat, flags, permission);
                channel.send(null, registerPacket);
            }
        }
        
        if (config.isConfigurationSection("irc-bridges")) {
            ConfigurationSection bridgesSection = config.getConfigurationSection("irc-bridges");
            for (String key : bridgesSection.getKeys(false)) {
                if (!bridgesSection.isConfigurationSection(key)) {
                    continue;
                }
                ConfigurationSection bridgeSection = bridgesSection.getConfigurationSection(key);
                boolean useSsl = bridgeSection.getBoolean("ssl", false);
                String host = bridgeSection.getString("host", "localhost");
                int port = bridgeSection.getInt("port", 6667);
                String password = bridgeSection.getString("password", "");
                String nick = bridgeSection.getString("nick", "DuckChat");
                String username = bridgeSection.getString("username", "bot");
                String realm = bridgeSection.getString("realm", "localhost");
                String messageFormat = bridgeSection.getString("messageFormat", "<%s> %s");
                String actionFormat = bridgeSection.getString("actionFormat", "*%s %s");

                IRCLink ircLink = new IRCLink(this, useSsl, host, port, password, nick, username, realm, messageFormat, actionFormat);

                if (bridgeSection.isConfigurationSection("channels")) {
                    ConfigurationSection bridgeChannelSection = bridgeSection.getConfigurationSection("channels");
                    for (String duckChannel : bridgeChannelSection.getKeys(false)) {
                        if (bridgeChannelSection.isString(duckChannel)) {
                            ircLink.addChannel(duckChannel, bridgeChannelSection.getString(duckChannel));
                        }
                    }
                }
                try {
                    ircLink.connect();
                    ircLinks.add(ircLink);
                } catch (IOException ex) {
                    getLogger().log(Level.WARNING, "Error connecting to IRC", ex);
                }
            }
        }
    }

    private void disconnect() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        for (IRCLink ircLink : ircLinks) {
            try {
                ircLink.disconnect();
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, null, ex);
            }
        }
        ircLinks.clear();
        members.clear();
        channels.clear();
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
            getPluginLoader().disablePlugin(this);
            return;
        }

        getServer().getScheduler().runTask(this, new Runnable() {
            @Override
            public void run() {
                RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
                if (chatProvider != null) {
                    vaultChat = chatProvider.getProvider();
                }
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

    public String getName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return translate("sender.console", sender.getName(), channel.getName());
        }
        return sender.getName();
    }

    public String getDisplayName(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player)sender).getDisplayName();
        }
        return getName(sender);
    }

    public String getWorld(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player)sender).getWorld().getName();
        }
        return "";
    }

    public String getPrefix(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (vaultChat != null) {
                return vaultChat.getPlayerPrefix(player).replace('&', ControlCodes.MINECRAFT_CONTROL_CODE);
            }
        }
        return "";
    }

    public String getSuffix(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (vaultChat != null) {
                return vaultChat.getPlayerSuffix(player).replace('&', ControlCodes.MINECRAFT_CONTROL_CODE);
            }
        }
        return "";
    }

    public String getPlayerName(String identifier) {
        synchronized (STATE_LOCK) {
            Member member = members.get(identifier);
            if (member != null) {
                return member.getName();
            }
        }
        return null;
    }

    // Must call with STATE_LOCK held.
    private void performAutoJoins(ChatChannel chatChannel) {
        Address local = getLocalAddress();
        for (Member m : members.values()) {
            if (local.equals(m.getAddress())) {
                if (chatChannel.isAutoJoin(m)) {
                    CommandSender sender = getPlayer(m.getIdentifier());
                    if (chatChannel.getPermission() == null || sender.hasPermission(chatChannel.getPermission())) {
                        sendJoinChannel(chatChannel.getName(), m.getIdentifier());
                    }
                }
            }
        }
    }

    // Must call with STATE_LOCK held.
    private void performAutoJoins(Member m) {
        if (getLocalAddress().equals(m.getAddress())) {
            for (ChatChannel chatChannel : channels.values()) {
                if (chatChannel.isAutoJoin(m)) {
                    CommandSender sender = getPlayer(m.getIdentifier());
                    if (chatChannel.getPermission() == null || sender.hasPermission(chatChannel.getPermission())) {
                        sendJoinChannel(chatChannel.getName(), m.getIdentifier());
                    }
                }
            }
        }
    }

    void setState(InputStream in) throws IOException, Exception {
        DataInputStream dataIn = new DataInputStream(in);
        synchronized (STATE_LOCK) {
            List<Member> memberList = (List<Member>) Util.objectFromStream(dataIn);
            List<ChatChannel> channelList = (List<ChatChannel>) Util.objectFromStream(dataIn);
            for (Member m : memberList) {
                // These should never be local.
                members.put(m.getIdentifier(), m);
            }
            for (ChatChannel c : channelList) {
                ChatChannel old = channels.put(c.getName(), c);
                if (old != null) {
                    // If we already knew about a channel, merge the member list.
                    for (Member m : old.getMembers()) {
                        c.addMember(m);
                    }
                } else {
                    performAutoJoins(c);
                }
            }
        }
    }

    void getState(OutputStream out) throws Exception {
        DataOutputStream dataOut = new DataOutputStream(out);
        synchronized (STATE_LOCK) {
            List<Member> memberList = new ArrayList<Member>(members.size());
            List<ChatChannel> channelList = new ArrayList<ChatChannel>(channels.size());
            memberList.addAll(members.values());
            channelList.addAll(channels.values());
            Util.objectToStream(memberList, dataOut);
            Util.objectToStream(channelList, dataOut);
        }
    }

    void viewUpdated(List<Address> addresses) {
        synchronized (STATE_LOCK) {
            // Remove stale members entries.
            Iterator<Map.Entry<String, Member>> i2 = members.entrySet().iterator();
            while (i2.hasNext()) {
                Map.Entry<String, Member> e = i2.next();
                if (!addresses.contains(e.getValue().getAddress())) {
                    i2.remove();
                }
            }
            // Remove channels and channel memberships owned by failed node.
            Iterator<Map.Entry<String, ChatChannel>> i3 = channels.entrySet().iterator();
            while (i3.hasNext()) {
                Map.Entry<String, ChatChannel> e = i3.next();
                Address owner = e.getValue().getOwner();
                if (owner != null && !addresses.contains(owner)) {
                    i3.remove();
                    continue;
                }
                Iterator<Member> i4 = e.getValue().getMembers().iterator();
                while (i4.hasNext()) {
                    Member m = i4.next();
                    if (!addresses.contains(m.getAddress())) {
                        i4.remove();
                    }
                }
            }
        }
    }

    void onCreateMember(Address addr, String identifier, String name, BitSet flags) {
        synchronized(STATE_LOCK) {
            // Special case, due to bungee, when a player connects, they may already
            // be connected to another server, so disconnect them from the other
            // server first.
            Member member = new Member(addr, identifier, name, flags);
            Member old = members.put(identifier, member);
            if (old != null) {
                for (ChatChannel chatChannel : channels.values()) {
                    chatChannel.removeMember(identifier);
                }
                member.setReplyAddress(old.getReplyAddress());
            }
            performAutoJoins(member);
        }
        MemberJoinEvent event = new MemberJoinEvent(channel.getName(addr), identifier, name);
        getServer().getPluginManager().callEvent(event);
    }

    void onUpdateMember(String identifier, BitSet flags) {
        synchronized(STATE_LOCK) {
            Member member = members.get(identifier);
            if (member != null) {
                member.setFlags(flags);
            }
        }
    }

    void deleteMember(Address addr, String identifier) {
        boolean left = false;
        String name = null;
        synchronized(STATE_LOCK) {
            Member member = members.get(identifier);
            if (member != null) {
                name = member.getName();
                if (member.getAddress().equals(addr)) {
                    members.remove(identifier);
                    for (ChatChannel chatChannel : channels.values()) {
                        chatChannel.removeMember(identifier);
                    }
                    left = true;
                }
            }
        }
        if (left && name != null) {
            MemberLeaveEvent event = new MemberLeaveEvent(channel.getName(addr), identifier, name);
            getServer().getPluginManager().callEvent(event);
        }
    }

    void createChannel(Address owner, String name, String messageFormat, String actionFormat, BitSet flags, final String permission) {
        synchronized (STATE_LOCK) {
            if (channels.containsKey(name)) {
                // TODO: Only warn for creating duplicate and different channels.
                getLogger().log(Level.WARNING, "Tried to create duplicate channel: {0}", name);
                updateChannel(name, messageFormat, actionFormat, flags, permission);
                return;
            }
            ChatChannel chatChannel = new ChatChannel(owner, name, messageFormat, actionFormat, flags, permission);
            channels.put(name, chatChannel);
            Address local = getLocalAddress();
            performAutoJoins(chatChannel);
        }
        registerPermission(permission);
    }

    void updateChannel(String name, String messageFormat, String actionFormat, BitSet flags, final String permission) {
        synchronized (STATE_LOCK) {
            if (!channels.containsKey(name)) {
                getLogger().log(Level.WARNING, "Tried to modify non existant channel: {0}", name);
                return;
            }
            ChatChannel chatChannel = channels.get(name);
            chatChannel.setMessageFormat(messageFormat);
            chatChannel.setActionFormat(actionFormat);
            chatChannel.setFlags(flags);
            chatChannel.setPermission(permission);
        }
        registerPermission(permission);
    }

    void deleteChannel(String name) {
        synchronized (STATE_LOCK) {
            if (!channels.containsKey(name)) {
                getLogger().log(Level.WARNING, "Tried to delete a non existant channel: {0}", name);
                return;
            }
            channels.remove(name);
        }
    }

    void joinChannel(String channelName, String identifier) {
        synchronized (STATE_LOCK) {
            Member member = members.get(identifier);
            if (member == null) {
                getLogger().log(Level.WARNING, "Non existant user tried to join a channel: {0}", identifier);
                return;
            }
            ChatChannel chatChannel = channels.get(channelName);
            if (chatChannel == null) {
                getLogger().log(Level.WARNING, "User tried to join a non existant channel: {0}", channelName);
                return;
            }
            chatChannel.addMember(member);
        }
    }

    void partChannel(String channelName, String identifier) {
        synchronized (STATE_LOCK) {
            Member member = members.get(identifier);
            if (member == null) {
                getLogger().log(Level.WARNING, "Non existant user tried to part a channel: {0}", identifier);
                return;
            }
            ChatChannel chatChannel = channels.get(channelName);
            if (chatChannel == null) {
                getLogger().log(Level.WARNING, "User tried to part a non existant channel: {0}", channelName);
                return;
            }
            chatChannel.removeMember(identifier);
        }
    }

    void messageChannel(String channel, String identifier, String message) {
        getServer().getPluginManager().callEvent(new ChannelMessageEvent(identifier, channel, message));
        synchronized (STATE_LOCK) {
            ChatChannel chatChannel = channels.get(channel);
            Address localAddress = getLocalAddress();
            if (chatChannel != null) {
                for (Member dest : chatChannel.getMembers()) {
                    if (dest.getAddress().equals(localAddress)) {
                        getPlayer(dest.getIdentifier()).sendMessage(message);
                    }
                }
            }
        }
    }

    void message(String from, String to, String message) {
        synchronized (STATE_LOCK) {
            Member fromMember;
            if (from != null) {
                fromMember = members.get(from);
                if (fromMember == null) {
                    return;
                }
            } else {
                fromMember = null;
            }
            Member toMember;
            if (to != null) {
                toMember = members.get(to);
                if (toMember == null) {
                    return;
                }
            } else {
                toMember = null;
            }
            if (toMember != null) {
                if (fromMember != null) {
                    // Set reply address if this is not a broadcast or echoto.
                    toMember.setReplyAddress(from);
                    // Private message.
                    CommandSender toPlayer = getPlayer(to);
                    if (toPlayer != null) {
                        toPlayer.sendMessage(translate("message.receiveformat", fromMember.getName(), toMember.getName(), message));
                    }
                    CommandSender fromPlayer = getPlayer(from);
                    if (fromPlayer != null) {
                        fromPlayer.sendMessage(translate("message.sendformat", fromMember.getName(), toMember.getName(), message));
                    }
                } else {
                    // Targetted echo.
                    CommandSender toPlayer = getPlayer(to);
                    if (toPlayer != null) {
                        toPlayer.sendMessage(translate("message.echoto", toMember.getName(), message));
                    }
                }
            } else {
                // Broadcast.
                getServer().broadcastMessage(translate("message.broadcast", message));
            }
        }
    }

    public Address getLocalAddress() {
        return channel.getAddress();
    }

    private void registerPermission(final String permission) {
        if (!registerPermissions)
            return;
        if (permission == null)
            return;
        // Run at a later time, we don't care when.
        getServer().getScheduler().runTask(this, new Runnable() {
            
            @Override
            public void run() {
                Permission perm = getServer().getPluginManager().getPermission(permission);
                if (perm == null) {
                    Permission permObj = new Permission(permission, null, PermissionDefault.OP);
                    getServer().getPluginManager().addPermission(permObj);
                    permObj.recalculatePermissibles();
                }
            }
        });
    }

    public List<String> getPlayerCompletions(String toComplete) {
        toComplete = toComplete.toLowerCase();
        List<String> result = new ArrayList<String>();
        synchronized (STATE_LOCK) {
            for (Member member : members.values()) {
                if (member.getName().toLowerCase().startsWith(toComplete)) {
                    result.add(member.getName());
                }
            }
        }
        return result;
    }

    public List<String> getChannelCompletions(CommandSender sender, String toComplete) {
        toComplete = toComplete.toLowerCase();
        List<String> result = new ArrayList<String>();
        for (String s : getAvailableChannels(sender)) {
            if (s.toLowerCase().contains(toComplete)) {
                result.add(s);
            }
        }
        return result;
    }

    public String findPlayerIdentifier(String name) {
        name = name.toLowerCase();
        String result = null;
        synchronized(STATE_LOCK) {
            for (Member m : members.values()) {
                String lowerCaseName = m.getName().toLowerCase();
                if (lowerCaseName.equals(name)) {
                    // Exact match.
                    return m.getIdentifier();
                }
                if (lowerCaseName.contains(name)) {
                    // Substring match, only return if there is only 1.
                    if (result != null) {
                        return null; // Multiple matches.
                    } else {
                        result = m.getIdentifier();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get a list of channels available to a player.
     * 
     * @param player
     * @return 
     */
    public List<String> getAvailableChannels(CommandSender player) {
        List<String> ret = new ArrayList<String>();
        synchronized (STATE_LOCK) {
            for (ChatChannel chatChannel : channels.values()) {
                String permission = chatChannel.getPermission();
                if (permission != null && !player.hasPermission(permission)) {
                    continue;
                }
                ret.add(chatChannel.getName());
            }
        }
        return ret;
    }

    /**
     * Get a list of channels to autojoin.
     * 
     * @param player
     * @return 
     */
    public List<String> getAutoJoinChannels(CommandSender player) {
        List<String> ret = new ArrayList<String>();
        synchronized (STATE_LOCK) {
            for (ChatChannel chatChannel : channels.values()) {
                String permission = chatChannel.getPermission();
                if (permission != null && !player.hasPermission(permission)) {
                    continue;
                }
                Address address = chatChannel.getOwner();
                if (address != null && !address.equals(getLocalAddress())) {
                    continue;
                }
                ret.add(chatChannel.getName());
            }
        }
        return ret;
    }


    /**
     * Get a list of channels a player has joined.
     * 
     * @param player
     * @return 
     */
    public List<String> getChannels(CommandSender player) {
        List<String> ret = new ArrayList<String>();
        synchronized (STATE_LOCK) {
            String identifier = getIdentifier(player);
            if (identifier != null) {
                for (ChatChannel chatChannel : channels.values()) {
                    String permission = chatChannel.getPermission();
                    if (permission != null && !player.hasPermission(permission)) {
                        continue;
                    }
                    if (chatChannel.isMember(identifier)) {
                        ret.add(chatChannel.getName());
                    }
                }
            }
        }
        return ret;
    }

    public List<String> getMembers(String channelName) {
        List<String> ret = new ArrayList<String>();
        synchronized(STATE_LOCK) {
            ChatChannel channel = channels.get(channelName);
            if (channel != null) {
                for (Member member : channel.getMembers()) {
                    ret.add(member.getName());
                }
            }
        }
        return ret;
    }

    public Map<String,List<String>> getPlayersByServer(boolean onlyPlayers) {
        synchronized (STATE_LOCK) {
            Map<Address, List<String>> result = new HashMap<Address,List<String>>();
            for (Address addr : channel.getView().getMembers()) {
                result.put(addr, new ArrayList<String>());
            }
            for (Member m : members.values()) {
                if (!onlyPlayers || !m.getIdentifier().startsWith("dc:"))  {
                    if (result.containsKey(m.getAddress())) {
                        result.get(m.getAddress()).add(m.getName());
                    } else {
                        List<String> tmp = new ArrayList<String>();
                        tmp.add(m.getName());
                        result.put(m.getAddress(), tmp);
                    }
                }
            }
            Map<String, List<String>> res2 = new HashMap<String, List<String>>(result.size());
            for (Map.Entry<Address, List<String>> e : result.entrySet()) {
                res2.put(channel.getName(e.getKey()), e.getValue());
            }
            return res2;
        }
    }

    public String getIdentifier(CommandSender player) {
        if (player instanceof Player) {
            if (useUUIDs) {
                return ((Player)player).getUniqueId().toString();
            } else {
                return player.getName();
            }
        } else if (player instanceof ConsoleCommandSender) {
            return "dc:console:" + channel.getName();
        }
        return null;
    }

    public CommandSender getPlayer(String identifier) {
        if (identifier.startsWith("dc:")) {
            if (identifier.equals("dc:console:" + channel.getName())) {
                return getServer().getConsoleSender();
            } else {
                return null;
            }
        } else {
            if (useUUIDs) {
                return getServer().getPlayer(UUID.fromString(identifier));
            } else {
                return getServer().getPlayer(identifier);
            }
        }
    }

    public String getReplyAddress(String identifier) {
        synchronized(STATE_LOCK) {
            Member member = members.get(identifier);
            return member == null ? null : member.getReplyAddress();
        }
    }

    public String getReplyAddress(CommandSender sender) {
        String identifier = getIdentifier(sender);
        if (identifier == null) {
            return null;
        }
        return getReplyAddress(identifier);
    }

    public CommandSenderState getCommandSenderState(CommandSender player) {
        String identifier = getIdentifier(player);
        if (identifier == null) {
            return null;
        }
        synchronized (STATE_LOCK) {
            CommandSenderState ret = commandSenderStates.get(identifier);
            if (ret == null) {
                ret = new CommandSenderState(defaultChannel);
                commandSenderStates.put(identifier, ret);
            }
            return ret;
        }
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

        subcommands.put("channels", channelsSubCommand);
        subcommands.put("channel", channelSubCommand);
        subcommands.put("channellist", channelListSubCommand);
        subcommands.put("chlist", channelListSubCommand);
        subcommands.put("join", joinSubCommand);
        subcommands.put("part", partSubCommand);
        subcommands.put("me", meSubCommand);
        subcommands.put("m", messageSubCommand);
        subcommands.put("msg", messageSubCommand);
        subcommands.put("message", messageSubCommand);
        subcommands.put("r", replySubCommand);
        subcommands.put("reply", replySubCommand);
        subcommands.put("reload", reloadSubCommand);
        subcommands.put("dcreload", reloadSubCommand);
        subcommands.put("say", saySubCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (Map.Entry<String,SubCommand> e : subcommands.entrySet()) {
            try {
                if(label.equalsIgnoreCase(e.getKey())) {
                    e.getValue().onCommand(sender, args);
                    return true;
                } else if (args.length >= 1 && e.getKey().equalsIgnoreCase(args[0])) {
                    label += " " + args[0];
                    String[] newArgs = new String[args.length-1];
                    System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                    e.getValue().onCommand(sender, newArgs);
                    return true;
                }
            } catch (SenderTypeException ex) {
                sender.sendMessage(translate("error.wrongsender"));
                return true;
            } catch (PermissionException ex) {
                sender.sendMessage(translate("error.permission", ex.getPermission()));
                return true;
            } catch (UsageException ex) {
                sender.sendMessage(translate(e.getValue().getName() + ".usage", label));
                return true;
            } catch (SubCommandException ex) {
                sender.sendMessage(translate("error.generic", ex.getMessage()));
                return true;
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

    public void sendMemberCreate(CommandSender player) {
        try {
            String playerName;
            if (player instanceof ConsoleCommandSender) {
                playerName = "console@" + channel.getName();
            } else {
                playerName = player.getName();
            }
            channel.send(null, new MemberCreateData(getIdentifier(player), playerName, new BitSet()));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error sending network message", ex);
        }
    }

    public void sendMemberUpdate(Player player) {
        try {
            channel.send(null, new MemberUpdateData(getIdentifier(player), new BitSet()));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error sending network message", ex);
        }
    }

    public void sendMemberDelete(Player player) {
        try {
            channel.send(null, new MemberDeleteData(getIdentifier(player)));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error sending network message", ex);
        }
    }

    public void sendJoinChannel(String channelName, CommandSender sender) {
        sendJoinChannel(channelName, getIdentifier(sender));
    }

    public void sendJoinChannel(String channelName, String identifier) {
        try {
            channel.send(null, new ChannelJoinData(channelName, identifier));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error sending network message", ex);
        }
    }

    public void sendPartChannel(CommandSender player, String channelName) {
        try {
            channel.send(null, new ChannelPartData(channelName, getIdentifier(player), player.getName()));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error sending network message", ex);
        }
    }

    public boolean sendChannelAction(CommandSender player, String action) {
        CommandSenderState state = getCommandSenderState(player);
        if (state == null) {
            return false;
        }
        if (state.getCurrentChannel() == null) {
            player.sendMessage(translate("chat.nochannel"));
            return false;
        }
        return sendChannelAction(player, state.getCurrentChannel(), action);
    }

    public boolean sendChannelAction(CommandSender player, String channelName, String action) {
        String format;
        synchronized(STATE_LOCK) {
            ChatChannel chatChannel = channels.get(channelName);
            if (chatChannel == null) {
                player.sendMessage(translate("chat.nochannel"));
                return false;
            }
            if (!chatChannel.isMember(getIdentifier(player))) {
                player.sendMessage(translate("chat.nochannel"));
                return false;
            }
            format = chatChannel.getActionFormat();
        }
        action = String.format(
                format,
                channelName,
                channel.getName(),
                getWorld(player),
                getName(player),
                getPrefix(player),
                getDisplayName(player),
                getSuffix(player),
                action);
        return sendChannelMessage(getIdentifier(player), channelName, action);
    }

    public boolean sendChannelMessage(CommandSender player, String message) {
        CommandSenderState state = getCommandSenderState(player);
        if (state.getCurrentChannel() == null) {
            player.sendMessage(translate("chat.nochannel"));
            return false;
        }
        return sendChannelMessage(player, state.getCurrentChannel(), message);
    }

    public boolean sendChannelMessage(CommandSender player, String channelName, String message) {
        String format;
        synchronized(STATE_LOCK) {
            ChatChannel chatChannel = channels.get(channelName);
            if (chatChannel == null) {
                player.sendMessage(translate("chat.nochannel"));
                return false;
            }
            if (!chatChannel.isMember(getIdentifier(player))) {
                player.sendMessage(translate("chat.nochannel"));
                return false;
            }
            format = chatChannel.getMessageFormat();
        }
        message = String.format(
                format,
                channelName,
                channel.getName(),
                getWorld(player),
                getName(player),
                getPrefix(player),
                getDisplayName(player),
                getSuffix(player),
                message);
        return sendChannelMessage(getIdentifier(player), channelName, message);
    }

    public boolean sendChannelMessage(String playerIdentity, String channelName, String message) {
        try {
            channel.send(null, new ChannelMessageData(playerIdentity, channelName, message));
            return true;
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error sending network message", ex);
        }
        return false;
    }

    public void sendMessage(CommandSender from, String to, String message) {
        try {
            channel.send(null, new MessageData(getIdentifier(from), to, message));
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Error sending network message", ex);
        }

    }

    public String translate(String key, Object... args) {
        if (!messages.containsKey(key)) {
            return "Unknown message:" + key;
        } else {
            return String.format(messages.get(key), args);
        }
    }

}