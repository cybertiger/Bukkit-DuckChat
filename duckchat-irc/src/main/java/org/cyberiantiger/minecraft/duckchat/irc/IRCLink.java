/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.BitSet;
import java.util.concurrent.Callable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ChannelMessageEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.MemberJoinEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.MemberLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ServerJoinEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ServerLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ServerSuspectEvent;
import org.cyberiantiger.minecraft.duckchat.irc.config.IRCAction;
import org.cyberiantiger.minecraft.duckchat.irc.config.IRCLinkConfig;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventAdapter;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLIRCConnection;

/**
 *
 * @author antony
 */
public class IRCLink {
    private final String IRC_CLIENT_IDENTITY = "__IRC_IDENTITY__";
    private final Listener listener = new Listener() {
        @EventHandler
        public void onChannelMessage(ChannelMessageEvent e) {
            synchronized (IRCLink.this) {
                if (IRC_CLIENT_IDENTITY.equals(e.getSource())) {
                    return;
                }
                if (duckToIrc.containsKey(e.getChannel())) {
                    String ircChannel = duckToIrc.get(e.getChannel());
                    if (joinedChannels.containsKey(ircChannel)) {
                        String message = ControlCodeTranslator.MINECRAFT.translate(e.getMessage(), ControlCodes.IRC, true);
                        ircConnection.doPrivmsg(duckToIrc.get(e.getChannel()), message);
                    }
                }
            }
        }
        @EventHandler
        public void onMemberJoin(MemberJoinEvent e) {
            synchronized (IRCLink.this) {
                String message = plugin.translate("member.join", e.getName(), e.getHost());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    if (joinedChannels.containsKey(channel)) {
                        ircConnection.doPrivmsg(channel, message);
                    }
                }
            }
        }

        @EventHandler
        public void onMemberLeave(MemberLeaveEvent e) {
            synchronized (IRCLink.this) {
                String message = plugin.translate("member.leave", e.getName(), e.getHost());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    if (joinedChannels.containsKey(channel)) {
                        ircConnection.doPrivmsg(channel, message);
                    }
                }
            }
        }

        @EventHandler
        public void onServerJoin(ServerJoinEvent e) {
            synchronized (IRCLink.this) {
                String message = plugin.translate("server.join", e.getName());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    if (joinedChannels.containsKey(channel)) {
                        ircConnection.doPrivmsg(channel, message);
                    }
                }
            }
        }

        @EventHandler
        public void onServerLeave(ServerLeaveEvent e) {
            synchronized (IRCLink.this) {
                String message = plugin.translate("server.leave", e.getName());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    if (joinedChannels.containsKey(channel)) {
                        ircConnection.doPrivmsg(channel, message);
                    }
                }
            }
        }

        @EventHandler
        public void onServerSuspect(ServerSuspectEvent e) {
        }
    };
    private final IRCEventAdapter ircEventAdapter = new IRCEventAdapter() {
        @Override
        public void onPrivmsg(String target, IRCUser user, String msg) {
            Runnable postAction = null;
            synchronized (IRCLink.this) {
                if (ircToDuck.containsKey(target)) {
                    final String targetChannel = ircToDuck.get(target);
                    if (msg.startsWith("\u0001ACTION ") && msg.endsWith("\u0001")) {
                        msg = msg.substring("\u0001ACTION ".length(), msg.length() - 1);
                        msg = ControlCodeTranslator.IRC.translate(msg, ControlCodes.MINECRAFT, true);
                        msg = IRCLink.this.plugin.filter(msg);
                        final String channelMessage = String.format(IRCLink.this.actionFormat, user, msg);
                        postAction = new Runnable() {
                            public void run() {
                                IRCLink.this.plugin.sendChannelMessage(nick, username, host);
                                IRCLink.this.plugin.sendChannelMessage(IRC_CLIENT_IDENTITY, targetChannel, channelMessage);
                            }
                        };
                    } else if (msg.equals(".players")) {
                        Map<String, List<String>> players = plugin.getState().getPlayersByServer(true);
                        List<String> servers = new ArrayList<String>(players.size());
                        servers.addAll(players.keySet());
                        Collections.sort(servers, String.CASE_INSENSITIVE_ORDER);
                        int serverCount = servers.size();
                        int playerCount = 0;
                        for (List<String> playerList : players.values()) {
                            Collections.sort(playerList, String.CASE_INSENSITIVE_ORDER);
                            playerCount += playerList.size();
                        }
                        String tmp = plugin.translate("irc.playerlist.header", serverCount, playerCount);
                        tmp = ControlCodeTranslator.MINECRAFT.translate(tmp, ControlCodes.IRC, true);
                        IRCLink.this.ircConnection.doPrivmsg(target, tmp);
                        for (String serverName : servers) {
                            List<String> playerList = players.get(serverName);
                            StringBuilder playerListString = new StringBuilder();
                            for (int i = 0; i < playerList.size(); i++) {
                                if (i != 0) {
                                    playerListString.append(", ");
                                }
                                playerListString.append(playerList.get(i));
                            }
                            tmp = plugin.translate("irc.playerlist.line", serverName, playerList.size(), playerListString);
                            tmp = ControlCodeTranslator.MINECRAFT.translate(tmp, ControlCodes.IRC, true);
                            IRCLink.this.ircConnection.doPrivmsg(target, tmp);
                        }
                    } else if (msg.startsWith(".command ")) {
                        final String cmd = msg.substring(".command ".length());
                        BitSet modes = null;
                        synchronized (IRCLink.this) {
                            Map<String,BitSet> users = joinedChannels.get(target);
                            if (users != null) {
                                modes = users.get(user.getNick());
                            }
                        }
                        if (modes != null && modes.get(ChannelMode.OWNER.ordinal())) {
                            postAction = new Runnable() {
                                public void run() {
                                    IRCLink.this.plugin.getServer().getScheduler().callSyncMethod(IRCLink.this.plugin, new Callable<Void>() {
                                        @Override
                                        public Void call() throws Exception {
                                            IRCLink.this.plugin.getServer().dispatchCommand(IRCLink.this.plugin.getServer().getConsoleSender(), cmd);
                                            return null;
                                        }
                                    });
                                }
                            };
                        }
                    } else {
                        msg = ControlCodeTranslator.IRC.translate(msg, ControlCodes.MINECRAFT, true);
                        msg = IRCLink.this.plugin.filter(msg);
                        final String channelMessage = String.format(IRCLink.this.messageFormat, user, msg);
                        postAction = new Runnable() {
                            public void run() {
                                IRCLink.this.plugin.sendChannelMessage(IRC_CLIENT_IDENTITY, targetChannel, channelMessage);
                            }
                        };
                    }
                } else {
                    plugin.getLogger().info(name + ": PrivMsg to: " + target + " from: " + user + " message: " +  msg);
                }
            }
            if (postAction != null) {
                postAction.run();
            }
        }

        private void afterRegistration() {
            synchronized (IRCLink.this) {
                if (!IRCLink.this.registered) {
                    IRCLink.this.registered = true;
                    IRCLink.this.plugin.getLogger().info(name + ": Connected to IRC server " + host + ":" + port);
                    if (onJoin != null) {
                        for (IRCAction action : onJoin) {
                            switch (action.getType()) {
                                case PRIV_MSG:
                                    ircConnection.doPrivmsg(action.getTarget(), action.getMessage());
                                    break;
                                case SEND:
                                    ircConnection.send(action.getMessage());
                                    break;
                            }
                        }
                    }
                    for (String ircChannel : ircToDuck.keySet()) {
                        ircConnection.doJoin(ircChannel);
                    }
                }
            }
        }

        
        @Override
        public void onRegistered() {
        }

        @Override
        public void onNotice(String target, IRCUser user, String msg) {
            plugin.getLogger().info(name + ": Notice to: " + target + " from: " + user + " message: " +  msg);
        }

        @Override
        public void onReply(int num, String value, String msg) {
            plugin.getLogger().info(name + ": Reply " + num + " = " + value + ": " + msg);
            if (num == 353) {
                Map<String,BitSet> users = null;
                synchronized(IRCLink.this) {
                    // value is something like <nick> " [=*@] " <channel>
                    // RFC says something completely different.
                    // So lets just take everything after the bloody # as the channel
                    int idx = value.indexOf('#');
                    if (idx != -1) {
                        value = value.substring(idx);
                        users = joinedChannels.get(value);
                        if (users != null) {
                            plugin.getLogger().warning("Updating user list for: " + value);
                            for (String s : msg.split(" ")) {
                                plugin.getLogger().warning("Parsing user list entry: " + s);
                                s = s.trim();
                                BitSet flags = new BitSet();
                                ChannelMode mode;
                                while (s.length() > 0 && ((mode = ChannelMode.getModeByPrefix(s.charAt(0))) != null)) {
                                    flags.set(mode.ordinal());
                                    s = s.substring(1);
                                }
                                if (s.length() > 0) {
                                    plugin.getLogger().warning("Setting flags for:" + s + " to: " + flags);
                                    users.put(s, flags);
                                }
                            }
                        } else {
                            plugin.getLogger().warning("No user list for channel: " + value);
                        }
                        plugin.getLogger().warning("User list is now: " + joinedChannels);
                    }
                }
            } else if (num == 1) {
                // Registered reply.
                afterRegistration();
            }
        }
        
        @Override
        public void onDisconnected() {
            synchronized (IRCLink.this) {
                IRCLink.this.plugin.getLogger().info(name + ": Disconnected from IRC server" + host + ":" + port);
                registered = false;
                joinedChannels.clear();
                scheduleReconnect();
            }
        }
        
        @Override
        public void onError(String msg) {
            plugin.getLogger().warning(name + ": Error: " + msg);
        }
        
        @Override
        public void onError(int num, String msg) {
            plugin.getLogger().warning(name + ": Error " + num + ": " + msg);
        }

        @Override
        public void onNick(IRCUser user, String newNick) {
            plugin.getLogger().warning("User: " + user + " changed nick to: " + newNick);
            synchronized(IRCLink.this) {
                for (Map<String,BitSet> users : joinedChannels.values()) {
                    if (users.containsKey(user.getNick())) {
                        BitSet flags = users.remove(user.getNick());
                        users.put(newNick, flags);
                    }
                }
                plugin.getLogger().warning("User list is now: " + joinedChannels);
            }
        }

        @Override
        public void onMode(String chan, IRCUser user, IRCModeParser modeParser) {
            plugin.getLogger().warning("on channel: " + chan + " user: " + user + " changed mode: " + modeParser.toString());
            Map<String,BitSet> users;
            synchronized(IRCLink.this) {
                users = joinedChannels.get(chan);
                if (users != null) {
                    ChannelModeParser parser = new ChannelModeParser(modeParser.getLine());
                    for (Map.Entry<String, ChannelModeParser.ModeChange> e : parser.getModeChanges().entrySet()) {
                        String nick = e.getKey();
                        ChannelModeParser.ModeChange change = e.getValue();
                        BitSet modes = users.get(nick);
                        if (modes != null) {
                            modes.andNot(change.getClearedModes());
                            modes.or(change.getSetModes());
                        }
                    }
                }
                plugin.getLogger().warning("User list is now: " + joinedChannels);
            }
        }

        @Override
        public void onJoin(String chan, IRCUser user) {
            plugin.getLogger().warning("User joined on: " + chan + " user: " + user);
            synchronized (IRCLink.this) {
                if (nick.equals(user.getNick())) {
                    joinedChannels.put(chan, new HashMap<String, BitSet>());
                } else {
                    Map<String,BitSet> users = joinedChannels.get(chan);
                    users.put(user.getNick(), new BitSet());
                }
                plugin.getLogger().warning("User list is now: " + joinedChannels);
            }
        }

        @Override
        public void onPart(String chan, IRCUser user, String msg) {
            plugin.getLogger().warning("User parted on: " + chan + " user: " + user + " msg: " + msg);
            synchronized (IRCLink.this) {
                if(nick.equals(user.getNick())) {
                    joinedChannels.remove(chan);
                } else {
                    Map<String,BitSet> users = joinedChannels.get(chan);
                    users.remove(user.getNick());
                }
                plugin.getLogger().warning("User list is now: " + joinedChannels);
            }
        }
    };
    private final static long[] RECONNECT_BACKOFF = new long[] { 0L, 10000L, 30000L, 60000L, 300000L }; // 0s, 10s, 30s, 1m, 5m
    private final Main plugin;
    private final String name;
    private final String messageFormat;
    private final String actionFormat;
    private final Map<String, String> duckToIrc = new HashMap<String, String>();
    private final Map<String, String> ircToDuck = new HashMap<String, String>();
    private final Map<String, Map<String,BitSet>> joinedChannels = new HashMap<String, Map<String,BitSet>>();
    private final boolean useSsl;
    private final String host;
    private final int port;
    private final String password;
    private final String nick;
    private final String username;
    private final String realmname;
    private final boolean debug;
    private IRCConnection ircConnection;
    private boolean shouldReconnect = false;
    private long lastReconnect = Long.MIN_VALUE;
    private int reconnectCount = 0;
    private boolean registered = false;
    private TimerTask reconnectTask = null;
    private final List<IRCAction> onJoin;

    public IRCLink(Main plugin, String name, boolean useSsl, String host, int port, String password, String nick, String username, String realmname, boolean debug, String messageFormat, String actionFormat, List<IRCAction> onJoin) {
        this.plugin = plugin;
        this.name = name;
        this.useSsl = useSsl;
        this.host = host;
        this.port = port;
        this.password = password;
        this.nick = nick;
        this.username = username;
        this.realmname = realmname;
        this.debug = debug;
        this.messageFormat = messageFormat;
        this.actionFormat = actionFormat;
        this.onJoin = onJoin;
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public IRCLink(Main plugin, IRCLinkConfig ircLinkConfig) {
        this(plugin, ircLinkConfig.getName(), ircLinkConfig.isSsl(), ircLinkConfig.getHost(), ircLinkConfig.getPort(), ircLinkConfig.getPassword(), ircLinkConfig.getNick(), ircLinkConfig.getUsername(), ircLinkConfig.getRealm(), ircLinkConfig.isDebug(), ircLinkConfig.getMessageFormat(), ircLinkConfig.getActionFormat(), ircLinkConfig.getOnJoin());
        Map<String,String> channels = ircLinkConfig.getChannels();
        if (channels != null) {
            for (Map.Entry<String,String> e : channels.entrySet()) {
                addChannel(e.getKey(), e.getValue());
            }
        }
    }

    private void tryConnect() {
        try {
            connect();
        } catch (IOException ioe) {
            plugin.getLogger().warning(name + ": Count not connect to " + host + ":" + port);
            scheduleReconnect();
        }
    }

    private synchronized void scheduleReconnect() {
        if (!shouldReconnect)
            return;

        long now = System.currentTimeMillis();

        if (lastReconnect + 60000L <= now) {
            // Last reconnect was over 60 seconds ago, reset reconnect counter.
            reconnectCount = 0;
        } else {
            // Increment counter, last reconnect probably failed.
            reconnectCount++;
        }
        // Work out the earliest time the next reconnect can be processed.
        long reconnectTime =  lastReconnect + RECONNECT_BACKOFF[Math.min(reconnectCount, RECONNECT_BACKOFF.length -1)];
        if (now > reconnectTime) {
            plugin.getReconnectTimer().schedule(reconnectTask = new TimerTask() {
                @Override public void run() {
                    tryConnect();
                }
            }, 0);
        } else {
            if (reconnectTask != null) {
                reconnectTask.cancel();
                reconnectTask = null;
            }
            plugin.getReconnectTimer().schedule(reconnectTask = new TimerTask() {
                @Override
                public void run() {
                    tryConnect();
                }
            }, new java.util.Date(reconnectTime));
        }
    }

    private void connect() throws IOException {
        lastReconnect = System.currentTimeMillis();
        if (useSsl) {
            ircConnection = new SSLIRCConnection(host, port, port, password, nick, username, realmname);
        } else {
            ircConnection = new IRCConnection(host, port, port, password, nick, username, realmname);
        }
        ircConnection.setEncoding("UTF-8");
        ircConnection.setDaemon(true);
        ircConnection.setColors(true);
        ircConnection.setPong(true);
        //ircConnection.setDebug(debug);
        ircConnection.setTimeout(0);
        ircConnection.addIRCEventListener(ircEventAdapter);
        ircConnection.setUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        plugin.getLogger().log(Level.WARNING, "Exception in ircbot", e);
                    }
                }
                );
        ircConnection.connect();
    }

    public synchronized void setConnected(boolean connected) {
        if (connected && !shouldReconnect) {
            shouldReconnect = true;
            scheduleReconnect();
        } else if (!connected && shouldReconnect) {
            shouldReconnect = false;
            if (reconnectTask != null) {
                reconnectTask.cancel();
                reconnectTask = null;
            }
            if (ircConnection != null) {
                ircConnection.close();
                ircConnection = null;
            }
            joinedChannels.clear();
            registered = false;
        }
    }

    public final synchronized void addChannel(String duckChannel, String ircChannel) {
        duckToIrc.put(duckChannel, ircChannel);
        ircToDuck.put(ircChannel, duckChannel);
    }
}
