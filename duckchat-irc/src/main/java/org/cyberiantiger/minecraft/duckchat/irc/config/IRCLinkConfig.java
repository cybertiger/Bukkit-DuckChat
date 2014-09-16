/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc.config;

import java.util.Map;

/**
 *
 * @author antony
 */
public class IRCLinkConfig {
    private String name;
    private boolean ssl;
    private String host;
    private int port;
    private String password;
    private String nick;
    private String username;
    private String realm;
    private String messageFormat;
    private String actionFormat;
    private boolean debug;
    private Map<String,String> channels;

    public String getName() {
        return name;
    }

    public boolean isSsl() {
        return ssl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getNick() {
        return nick;
    }

    public String getUsername() {
        return username;
    }

    public String getRealm() {
        return realm;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public String getActionFormat() {
        return actionFormat;
    }

    public boolean isDebug() {
        return debug;
    }

    public Map<String, String> getChannels() {
        return channels;
    }
}
