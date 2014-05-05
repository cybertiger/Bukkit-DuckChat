/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.cyberiantiger.minecraft.duckchat.event.ChannelMessageEvent;
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
            if (IRC_CLIENT_IDENTITY.equals(e.getSource())) {
                return;
            }
            if (duckToIrc.containsKey(e.getChannel())) {
                String message = ControlCodeTranslator.MINECRAFT.translate(e.getMessage(), ControlCodes.IRC, true);
                ircConnection.doPrivmsg(duckToIrc.get(e.getChannel()), message);
            }
        }
    };
    private final Main plugin;
    private final String format;
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

    public IRCLink(Main plugin, boolean useSsl, String host, int port, String password, String nick, String username, String realmname, String format) {
        this.plugin = plugin;
        this.useSsl = useSsl;
        this.host = host;
        this.port = port;
        this.password = password;
        this.nick = nick;
        this.username = username;
        this.realmname = realmname;
        this.format = format;

    }

    public void connect() throws IOException {
        if (useSsl) {
            ircConnection = new SSLIRCConnection(host, port, port, password, nick, username, realmname);
        } else {
            ircConnection = new IRCConnection(host, port, port, password, nick, username, realmname);
        }
        ircConnection.setDaemon(true);
        ircConnection.setColors(true);
        ircConnection.setPong(true);

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        ircConnection.addIRCEventListener(new IRCEventAdapter() {

            @Override
            public void onPrivmsg(String target, IRCUser user, String msg) {
                if (ircToDuck.containsKey(target)) {
                    msg = ControlCodeTranslator.IRC.translate(msg, ControlCodes.MINECRAFT, true);
                    IRCLink.this.plugin.sendChannelMessage(IRC_CLIENT_IDENTITY, ircToDuck.get(target), String.format(IRCLink.this.format, user, msg));
                }
            }

            @Override
            public void onRegistered() {
                IRCLink.this.plugin.getLogger().info("Connected to IRC server");
                for (String ircChannel : ircToDuck.keySet()) {
                    ircConnection.doJoin(ircChannel);
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
            
        });
        shouldReconnect = true;
        ircConnection.connect();
    }

    public void disconnect() throws IOException {
        shouldReconnect = false;
        ChannelMessageEvent.getHandlerList().unregister(listener);
        if (ircConnection.isConnected()) {
            ircConnection.doQuit();
        }
    }

    public void addChannel(String duckChannel, String ircChannel) {
        duckToIrc.put(duckChannel, ircChannel);
        ircToDuck.put(ircChannel, duckChannel);
    }
}