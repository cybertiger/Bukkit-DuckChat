/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.config;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author antony
 */
public class Config {
    private String clusterName = "duckchat";
    private String nodename = null;
    private boolean useUUIDs = true;
    private String network = null;
    private String defaultChannel = "global";
    private boolean useIPv4 = true;
    private boolean notifyServerJoin = true;
    private boolean notifyServerLeave = true;
    private boolean notifyPlayerJoin = true;
    private boolean notifyPlayerLeave = true;
    private String bindAddress = "127.0.0.1";
    private Map<String,String> shortcuts = null;
    private Map<String,ChannelConfig> channels = null;

    public boolean isUseIPv4() {
        return useIPv4;
    }

    public String getBindAddress() {
        return bindAddress;
    }


    public String getClusterName() {
        return clusterName;
    }

    public String getNodeName() {
        return nodename;
    }

    public boolean isUseUUIDs() {
        return useUUIDs;
    }

    public String getNetwork() {
        return network;
    }

    public Map<String,ChannelConfig> getChannels() {
        return channels == null ? Collections.<String,ChannelConfig>emptyMap() : channels;
    }

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public Map<String,String> getShortcuts() {
        return shortcuts == null ? Collections.<String,String>emptyMap() : shortcuts;
    }

    public boolean isNotifyServerJoin() {
        return notifyServerJoin;
    }

    public boolean isNotifyServerLeave() {
        return notifyServerLeave;
    }

    public boolean isNotifyPlayerJoin() {
        return notifyPlayerJoin;
    }

    public boolean isNotifyPlayerLeave() {
        return notifyPlayerLeave;
    }
}