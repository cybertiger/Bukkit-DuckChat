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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ChannelMessageEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.MemberJoinEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.MemberLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ServerJoinEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ServerLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.bukkit.event.ServerSuspectEvent;
import org.cyberiantiger.minecraft.duckchat.irc.config.IRCLinkConfig;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventAdapter;
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
                        IRCLink.this.ircConnection.doPrivmsg(target, plugin.translate("irc.playerlist.header", serverCount, playerCount));
                        for (String serverName : servers) {
                            List<String> playerList = players.get(serverName);
                            StringBuilder playerListString = new StringBuilder();
                            for (int i = 0; i < playerList.size(); i++) {
                                if (i != 0) {
                                    playerListString.append(", ");
                                }
                                playerListString.append(playerList.get(i));
                            }
                            IRCLink.this.ircConnection.doPrivmsg(target, plugin.translate("irc.playerlist.line", serverName, playerList.size(), playerListString));
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

        @Override
        public void onNotice(String target, IRCUser user, String msg) {
            plugin.getLogger().info(name + ": Notice to: " + target + " from: " + user + " message: " +  msg);
        }

        @Override
        public void onReply(int num, String value, String msg) {
            if (num == 1) {
                IRCLink.this.plugin.getLogger().info(name + ": Connected to IRC server " + host + ":" + port);
                for (String ircChannel : ircToDuck.keySet()) {
                    ircConnection.doJoin(ircChannel);
                }
            }
            plugin.getLogger().info(name + ": Reply " + num + " = " + value + ": " + msg);
        }
        
        @Override
        public void onDisconnected() {
            synchronized (IRCLink.this) {
                IRCLink.this.plugin.getLogger().info(name + ": Disconnected from IRC server" + host + ":" + port);
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
        public void onJoin(String chan, IRCUser user) {
            synchronized (IRCLink.this) {
                if (nick.equals(user.getNick())) {
                    joinedChannels.put(chan, Boolean.TRUE);
                }
            }
        }

        @Override
        public void onPart(String chan, IRCUser user, String msg) {
            synchronized (IRCLink.this) {
                if(nick.equals(user.getNick())) {
                    joinedChannels.remove(chan);
                }
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
    private final Map<String, Boolean> joinedChannels = new HashMap<String, Boolean>();
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
    private TimerTask reconnectTask = null;

    public IRCLink(Main plugin, String name, boolean useSsl, String host, int port, String password, String nick, String username, String realmname, boolean debug, String messageFormat, String actionFormat) {
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
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public IRCLink(Main plugin, IRCLinkConfig ircLinkConfig) {
        this(plugin, ircLinkConfig.getName(), ircLinkConfig.isSsl(), ircLinkConfig.getHost(), ircLinkConfig.getPort(), ircLinkConfig.getPassword(), ircLinkConfig.getNick(), ircLinkConfig.getUsername(), ircLinkConfig.getRealm(), ircLinkConfig.isDebug(), ircLinkConfig.getMessageFormat(), ircLinkConfig.getActionFormat());
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
            tryConnect();
        } else {
            if (reconnectTask != null) {
                reconnectTask.cancel();
                reconnectTask = null;
            }
            plugin.getReconnectTimer().schedule(new TimerTask() {
                @Override
                public void run() {
                    tryConnect();
                }
            }, new java.util.Date(reconnectTime));
        }
    }

    private synchronized void connect() throws IOException {
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
        ircConnection.setDebug(debug);
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
        }
    }

    public final synchronized void addChannel(String duckChannel, String ircChannel) {
        duckToIrc.put(duckChannel, ircChannel);
        ircToDuck.put(ircChannel, duckChannel);
    }
}