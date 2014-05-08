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
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.cyberiantiger.minecraft.duckchat.event.ChannelMessageEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberJoinEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.event.ServerJoinEvent;
import org.cyberiantiger.minecraft.duckchat.event.ServerLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.event.ServerSuspectEvent;
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
                if (!isConnected())
                    return;
                if (IRC_CLIENT_IDENTITY.equals(e.getSource())) {
                    return;
                }
                if (duckToIrc.containsKey(e.getChannel())) {
                    String message = ControlCodeTranslator.MINECRAFT.translate(e.getMessage(), ControlCodes.IRC, true);
                    ircConnection.doPrivmsg(duckToIrc.get(e.getChannel()), message);
                }
            }
        }
        @EventHandler
        public void onMemberJoin(MemberJoinEvent e) {
            synchronized (IRCLink.this) {
                if (!isConnected())
                    return;
                String message = plugin.translate("member.join", e.getName(), e.getHost());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    ircConnection.doPrivmsg(channel, message);
                }
            }
        }

        @EventHandler
        public void onMemberLeave(MemberLeaveEvent e) {
            synchronized (IRCLink.this) {
                if (!isConnected())
                    return;
                String message = plugin.translate("member.leave", e.getName(), e.getHost());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    ircConnection.doPrivmsg(channel, message);
                }
            }
        }

        @EventHandler
        public void onServerJoin(ServerJoinEvent e) {
            synchronized (IRCLink.this) {
                if (!isConnected())
                    return;
                String message = plugin.translate("server.join", e.getName());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    ircConnection.doPrivmsg(channel, message);
                }
            }
        }

        @EventHandler
        public void onServerLeave(ServerLeaveEvent e) {
            synchronized (IRCLink.this) {
                if (!isConnected())
                    return;
                String message = plugin.translate("server.leave", e.getName());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    ircConnection.doPrivmsg(channel, message);
                }
            }
        }

        @EventHandler
        public void onServerSuspect(ServerSuspectEvent e) {
            synchronized (IRCLink.this) {
                if (!isConnected())
                    return;
                String message = plugin.translate("server.suspect", e.getName());
                message = ControlCodeTranslator.MINECRAFT.translate(message, ControlCodes.IRC, true);
                for (String channel : duckToIrc.values()) {
                    ircConnection.doPrivmsg(channel, message);
                }
            }
        }
    };
    private final IRCEventAdapter ircEventAdapter = new IRCEventAdapter() {
            @Override
            public void onPrivmsg(String target, IRCUser user, String msg) {
                if (ircToDuck.containsKey(target)) {
                    String targetChannel = ircToDuck.get(target);
                    if (msg.startsWith("\u0001ACTION ") && msg.endsWith("\u0001")) {
                        msg = msg.substring("\u0001ACTION ".length(), msg.length() - 1);
                        msg = ControlCodeTranslator.IRC.translate(msg, ControlCodes.MINECRAFT, true);
                        IRCLink.this.plugin.sendChannelMessage(IRC_CLIENT_IDENTITY, targetChannel, String.format(IRCLink.this.actionFormat, user, msg));
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
                        IRCLink.this.plugin.sendChannelMessage(IRC_CLIENT_IDENTITY, targetChannel, String.format(IRCLink.this.messageFormat, user, msg));
                    }
                }
            }

            @Override
            public void onRegistered() {
                IRCLink.this.plugin.getLogger().info("Connected to IRC server");
                for (String ircChannel : ircToDuck.keySet()) {
                    ircConnection.doJoin(ircChannel);
                }
                synchronized (IRCLink.this) {
                    IRCLink.this.haveRegistered = true;
                }
            }

            @Override
            public void onDisconnected() {
                IRCLink.this.plugin.getLogger().info("Disconnected from IRC server");
                if (IRCLink.this.shouldReconnect) {
                    try {
                        IRCLink.this.disconnect();
                        IRCLink.this.connect();
                    } catch (IOException ex) {
                        IRCLink.this.plugin.getLogger().log(Level.SEVERE, "Error reconnecting to IRC.");
                    }
                }
            }
            
    };
    private final Main plugin;
    private final String messageFormat;
    private final String actionFormat;
    private final HashMap<String, String> duckToIrc = new HashMap<String, String>();
    private final HashMap<String, String> ircToDuck = new HashMap<String, String>();
    private final boolean useSsl;
    private final String host;
    private final int port;
    private final String password;
    private final String nick;
    private final String username;
    private final String realmname;
    private IRCConnection ircConnection;
    private boolean shouldReconnect = false;
    private boolean haveRegistered = false;

    public IRCLink(Main plugin, boolean useSsl, String host, int port, String password, String nick, String username, String realmname, String messageFormat, String actionFormat) {
        this.plugin = plugin;
        this.useSsl = useSsl;
        this.host = host;
        this.port = port;
        this.password = password;
        this.nick = nick;
        this.username = username;
        this.realmname = realmname;
        this.messageFormat = messageFormat;
        this.actionFormat = actionFormat;
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    private synchronized boolean isConnected() {
        return ircConnection != null && ircConnection.isConnected() && haveRegistered;
    }

    public synchronized void connect() throws IOException {
        if (useSsl) {
            ircConnection = new SSLIRCConnection(host, port, port, password, nick, username, realmname);
        } else {
            ircConnection = new IRCConnection(host, port, port, password, nick, username, realmname);
        }
        ircConnection.setDaemon(true);
        ircConnection.setColors(true);
        ircConnection.setPong(true);

        ircConnection.addIRCEventListener(ircEventAdapter);

        shouldReconnect = true;
        ircConnection.connect();
    }

    public synchronized void disconnect() throws IOException {
        shouldReconnect = false;
        haveRegistered = false;
        if (ircConnection != null && ircConnection.isConnected()) {
            ircConnection.doQuit();
        }
        ircConnection = null;
    }

    public void addChannel(String duckChannel, String ircChannel) {
        duckToIrc.put(duckChannel, ircChannel);
        ircToDuck.put(ircChannel, duckChannel);
    }
}