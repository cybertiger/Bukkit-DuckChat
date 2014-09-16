/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc.config;

import java.util.List;
import java.util.Map;

/**
 *
 * @author antony
 */
public class Config {
    
    private List<IRCLinkConfig> ircBridges;
    private Map<String,String> messages;
    private Map<String,String> filters;

    public List<IRCLinkConfig> getIrcBridges() {
        return ircBridges;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public Map<String, String> getFilters() {
        return filters;
    }
}
