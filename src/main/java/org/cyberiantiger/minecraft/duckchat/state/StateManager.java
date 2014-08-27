/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.state;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.cyberiantiger.minecraft.duckchat.event.ChannelMessageEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberJoinEvent;
import org.cyberiantiger.minecraft.duckchat.event.MemberLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.event.MessageEvent;
import org.cyberiantiger.minecraft.duckchat.event.PluginMessageEvent;
import org.cyberiantiger.minecraft.duckchat.event.ServerJoinEvent;
import org.cyberiantiger.minecraft.duckchat.event.ServerLeaveEvent;
import org.cyberiantiger.minecraft.duckchat.event.ServerSuspectEvent;
import org.jgroups.Address;
import org.jgroups.util.Util;

/**
 *
 * @author antony
 */
public class StateManager {
    final Main plugin;

    // Local server address.
    private Address localAddress = null;
    // Server names by address.
    private final Map<Address,String> servers = new HashMap<Address,String>();
    // All players, keyed on identifier.
    private final Map<String,Member> members = new HashMap<String,Member>();
    // All channels.
    private final Map<String,ChatChannel> channels = new HashMap<String,ChatChannel>();
    // State 
    private final Object LOCK = new Object();

    
    public StateManager(Main plugin) {
        this.plugin = plugin;
    }

    // Must be called with LOCK held.
    private List<Runnable> performAutoJoins(ChatChannel chatChannel) {
        List<Runnable> actions = new ArrayList<Runnable>();
        Address local = getLocalAddress();
        for (Member m : members.values()) {
            if (local.equals(m.getAddress())) {
                if (chatChannel.isAutoJoin(m)) {
                    // XXX: Should not call out with lock held.
                    
                    if (chatChannel.getPermission() == null) {
                        plugin.sendJoinChannel(chatChannel.getName(), m.getIdentifier());
                    } else {
                        final String channelName = chatChannel.getName();
                        final String permission = chatChannel.getPermission();
                        final String identifier = m.getIdentifier();
                        actions.add(new Runnable() {
                            @Override
                            public void run() {
                                if(plugin.getCommandSenderManager().hasPermission(identifier, permission)) {
                                    plugin.sendJoinChannel(channelName, identifier);
                                }
                            }
                        });
                    }
                }
            }
        }
        return actions;
    }

    // Must not be called with LOCK held.
    private List<Runnable> performAutoJoins(Member m) {
        List<Runnable> actions = new ArrayList<Runnable>();
        if (getLocalAddress().equals(m.getAddress())) {
            for (ChatChannel chatChannel : channels.values()) {
                if (chatChannel.isAutoJoin(m)) {
                    // XXX: Should not call out with lock held.
                    if (chatChannel.getPermission() == null) {
                        plugin.sendJoinChannel(chatChannel.getName(), m.getIdentifier());
                    } else {
                        final String channelName = chatChannel.getName();
                        final String permission = chatChannel.getPermission();
                        final String identifier = m.getIdentifier();
                        actions.add(new Runnable() {
                            @Override
                            public void run() {
                                if(plugin.getCommandSenderManager().hasPermission(identifier, permission)) {
                                    plugin.sendJoinChannel(channelName, identifier);
                                }
                            }
                        });
                    }
                }
            }
        }
        return actions;
    }

    public void clear() {
        synchronized (LOCK) {
            members.clear();
            channels.clear();
        }
    }

    public void setLocalAddress(Address localAddress) {
        synchronized (LOCK) {
            this.localAddress = localAddress;
        }
    }

    public Address getLocalAddress() {
        synchronized (LOCK) {
            return localAddress;
        }
    }

    public String getLocalNodeName() {
        synchronized (LOCK) {
            return servers.get(localAddress);
        }
    }


    public String getPlayerName(String identifier) {
        synchronized (LOCK) {
            Member member = members.get(identifier);
            if (member != null) {
                return member.getName();
            }
        }
        return null;
    }

    void set(InputStream in) throws Exception {
        DataInputStream dataIn = new DataInputStream(in);
        List<Runnable> actions = new ArrayList<Runnable>();
        synchronized (LOCK) {
            Map<Address,String> remoteServers = (Map<Address,String>) Util.objectFromStream(dataIn);
            List<Member> memberList = (List<Member>) Util.objectFromStream(dataIn);
            List<ChatChannel> channelList = (List<ChatChannel>) Util.objectFromStream(dataIn);
            servers.putAll(remoteServers);
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
                    actions.addAll(performAutoJoins(c));
                }
            }
        }
        for (Runnable r : actions) {
            r.run();
        }
    }

    void get(OutputStream out) throws Exception {
        DataOutputStream dataOut = new DataOutputStream(out);
        synchronized (LOCK) {
            List<Member> memberList = new ArrayList<Member>(members.size());
            List<ChatChannel> channelList = new ArrayList<ChatChannel>(channels.size());
            memberList.addAll(members.values());
            channelList.addAll(channels.values());
            Util.objectToStream(servers, dataOut);
            Util.objectToStream(memberList, dataOut);
            Util.objectToStream(channelList, dataOut);
        }
    }

    void onViewUpdated(List<Address> addresses) {
        Map<Address,String> removed = new HashMap<Address,String>();
        Map<Address,String> added = new HashMap<Address,String>();
        synchronized (LOCK) {
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
            // Work out who joined and who left.
            Iterator<Map.Entry<Address,String>> i = servers.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<Address,String> e = i.next();
                if (!addresses.contains(e.getKey())) {
                    removed.put(e.getKey(), e.getValue());
                    i.remove();
                }
            }
            for (Address addr : addresses) {
                if (!servers.containsKey(addr)) {
                    String name = plugin.getNodeName(addr);
                    if (name != null) {
                        servers.put(addr, name);
                        added.put(addr, name);
                    }
                }
            }
        }
        // Send events & broadcast messages.
        for (Map.Entry<Address,String> e : removed.entrySet()) {
            ServerLeaveEvent event = new ServerLeaveEvent(e.getKey(), e.getValue());
            plugin.getServer().getPluginManager().callEvent(event);
            plugin.getCommandSenderManager().broadcast(plugin.translate("server.leave", e.getValue()));
        }
        for (Map.Entry<Address,String> e : added.entrySet()) {
            ServerJoinEvent event = new ServerJoinEvent(e.getKey(), e.getValue());
            plugin.getServer().getPluginManager().callEvent(event);
            plugin.getCommandSenderManager().broadcast(plugin.translate("server.join", e.getValue()));
        }
    }

    void onSuspect(Address addr) {
        String name;
        synchronized(LOCK) {
            name = servers.get(addr);
        }
        ServerSuspectEvent event = new ServerSuspectEvent(addr,name);
        plugin.getServer().getPluginManager().callEvent(event);
        // plugin.getCommandSenderManager().broadcast(plugin.translate("server.suspect", name));
    }

    void onServerCreate(Address src, String name) {
        boolean created;
        synchronized(LOCK) {
            created = servers.put(src, name) == null;
        }
        if (created) {
            ServerJoinEvent event = new ServerJoinEvent(src, name);
            plugin.getServer().getPluginManager().callEvent(event);
            plugin.getCommandSenderManager().broadcast(plugin.translate("server.join", name));
        }
    }


    void onMemberCreate(Address addr, String identifier, String name, BitSet flags) {
        String serverName;
        List<Runnable> actions;
        synchronized(LOCK) {
            // Special case, due to bungee, when a player connects, they may already
            // be connected to another server, so disconnect them from the other
            // server first.
            Member member = new Member(addr, identifier, name, flags);
            Member old = members.put(identifier, member);
            if (old != null) {
                for (ChatChannel chatChannel : channels.values()) {
                    chatChannel.removeMember(identifier);
                }
            }
            serverName = servers.get(addr);
            actions = performAutoJoins(member);
        }
        MemberJoinEvent event = new MemberJoinEvent(serverName, identifier, name);
        plugin.getServer().getPluginManager().callEvent(event);
        for (Runnable r : actions) {
            r.run();
        }
    }

    void onMemberUpdate(String identifier, BitSet flags) {
        synchronized(LOCK) {
            Member member = members.get(identifier);
            if (member != null) {
                member.setFlags(flags);
            }
        }
    }

    void onMemberDelete(Address addr, String identifier) {
        boolean left = false;
        String serverName = null;
        String name = null;
        synchronized(LOCK) {
            // Extra check added so MemberLeaveEvent is not fired on switching server.
            Member member = members.get(identifier);
            if (member != null) {
                if (member.getAddress().equals(addr)) {
                    members.remove(identifier);
                    for (ChatChannel chatChannel : channels.values()) {
                        chatChannel.removeMember(identifier);
                    }
                    serverName = servers.get(addr);
                    name = member.getName();
                    left = true;
                }
            }
        }
        if (left) {
            MemberLeaveEvent event = new MemberLeaveEvent(serverName, identifier, name);
            plugin.getServer().getPluginManager().callEvent(event);
        }
    }

    void onChannelCreate(Address owner, String name, String messageFormat, String actionFormat, BitSet flags, final String permission) {
        boolean update;
        List<Runnable> actions = null;
        synchronized (LOCK) {
            update = channels.containsKey(name);
            if (!update) {
                ChatChannel chatChannel = new ChatChannel(owner, name, messageFormat, actionFormat, flags, permission);
                channels.put(name, chatChannel);
                actions = performAutoJoins(chatChannel);
            }
        }
        if (update) {
            onChannelUpdate(name, messageFormat, actionFormat, flags, permission);
        } else {
            plugin.getCommandSenderManager().registerPermission(permission);
        }
        if (actions != null) {
            for (Runnable r : actions) {
                r.run();
            }
        }
    }

    void onChannelUpdate(String name, String messageFormat, String actionFormat, BitSet flags, final String permission) {
        synchronized (LOCK) {
            if (!channels.containsKey(name)) {
                plugin.getLogger().log(Level.WARNING, "Tried to modify non existant channel: {0}", name);
                return;
            }
            ChatChannel chatChannel = channels.get(name);
            chatChannel.setMessageFormat(messageFormat);
            chatChannel.setActionFormat(actionFormat);
            chatChannel.setFlags(flags);
            chatChannel.setPermission(permission);
        }
        plugin.getCommandSenderManager().registerPermission(permission);
    }

    void onChannelDelete(String name) {
        synchronized (LOCK) {
            if (!channels.containsKey(name)) {
                plugin.getLogger().log(Level.WARNING, "Tried to delete a non existant channel: {0}", name);
                return;
            }
            channels.remove(name);
        }
    }

    void onChannelJoin(String channelName, String identifier) {
        synchronized (LOCK) {
            Member member = members.get(identifier);
            if (member == null) {
                plugin.getLogger().log(Level.WARNING, "Non existant user tried to join a channel: {0}", identifier);
                return;
            }
            ChatChannel chatChannel = channels.get(channelName);
            if (chatChannel == null) {
                plugin.getLogger().log(Level.WARNING, "User tried to join a non existant channel: {0}", channelName);
                return;
            }
            chatChannel.addMember(member);
        }
    }

    void onChannelPart(String channelName, String identifier) {
        synchronized (LOCK) {
            Member member = members.get(identifier);
            if (member == null) {
                plugin.getLogger().log(Level.WARNING, "Non existant user tried to part a channel: {0}", identifier);
                return;
            }
            ChatChannel chatChannel = channels.get(channelName);
            if (chatChannel == null) {
                plugin.getLogger().log(Level.WARNING, "User tried to part a non existant channel: {0}", channelName);
                return;
            }
            chatChannel.removeMember(identifier);
        }
    }

    void onChannelMessage(String channel, String identifier, String message) {
        ChannelMessageEvent channelMessageEvent = new ChannelMessageEvent(identifier, channel, message);
        plugin.getServer().getPluginManager().callEvent(channelMessageEvent);
        List<String> targets = new ArrayList<String>();
        synchronized (LOCK) {
            ChatChannel chatChannel = channels.get(channel);
            if (chatChannel != null) {
                for (Member dest : chatChannel.getMembers()) {
                    if (dest.getAddress().equals(localAddress)) {
                        targets.add(dest.getIdentifier());
                    }
                }
            }
        }
        for (String i : targets) {
            plugin.getCommandSenderManager().sendMessage(i, message);
        }
    }

    void onMessage(String from, String to, String message) {
        MessageEvent messageEvent = new MessageEvent(from, to, message);
        plugin.getServer().getPluginManager().callEvent(messageEvent);
        boolean broadcast = false;
        Map<String,String> messages = new HashMap<String,String>();
        synchronized (LOCK) {
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
                    plugin.getCommandSenderManager().setReplyAddress(to, from);
                    // Private messages.
                    if (localAddress.equals(toMember.getAddress())) {
                        messages.put(to, plugin.translate("message.receiveformat", fromMember.getName(), toMember.getName(), message));
                    }
                    if (localAddress.equals(fromMember.getAddress())) {
                        messages.put(from, plugin.translate("message.sendformat", fromMember.getName(), toMember.getName(), message));
                    }
                } else {
                    if (localAddress.equals(toMember.getAddress())) {
                        messages.put(to, plugin.translate("message.echoto", fromMember.getName(), toMember.getName(), message));
                    }
                }
            } else {
                messages.put(null, plugin.translate("message.broadcast", message));
                broadcast = true;
            }
        }
        if (broadcast) {
            plugin.getCommandSenderManager().broadcast(messages.get(null));
        } else {
            for (Map.Entry<String,String> e : messages.entrySet()) {
                plugin.getCommandSenderManager().sendMessage(e.getKey(), e.getValue());
            }
        }
    }

    public List<String> getPlayerCompletions(String toComplete) {
        toComplete = toComplete.toLowerCase();
        List<String> result = new ArrayList<String>();
        synchronized (LOCK) {
            for (Member member : members.values()) {
                if (member.getName().toLowerCase().startsWith(toComplete)) {
                    result.add(member.getName());
                }
            }
        }
        return result;
    }

    public List<String> getChannelCompletions(String identifier, String toComplete) {
        toComplete = toComplete.toLowerCase();
        List<String> result = new ArrayList<String>();
        for (String s : getAvailableChannels(identifier)) {
            if (s.toLowerCase().contains(toComplete)) {
                result.add(s);
            }
        }
        return result;
    }

    public String findPlayerIdentifier(String name) {
        name = name.toLowerCase();
        String result = null;
        synchronized(LOCK) {
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

    public List<String> getAvailableChannels(String identifier) {
        Map<String, String> channelPermissions = new HashMap<String, String>();
        List<String> ret = new ArrayList<String>();
        synchronized (LOCK) {
            for (ChatChannel chatChannel : channels.values()) {
                String permission = chatChannel.getPermission();
                if (permission != null) {
                    channelPermissions.put(chatChannel.getName(), permission);
                    continue;
                }
                ret.add(chatChannel.getName());
            }
        }
        for (Map.Entry<String, String> e : channelPermissions.entrySet()) {
            if (plugin.getCommandSenderManager().hasPermission(identifier, e.getValue())) {
                ret.add(e.getKey());
            }
        }
        return ret;
    }

    public List<String> getAutoJoinChannels(String identifier) {
        Map<String,String> channelPermissions = new HashMap<String,String>();
        List<String> ret = new ArrayList<String>();
        synchronized (LOCK) {
            for (ChatChannel chatChannel : channels.values()) {
                String permission = chatChannel.getPermission();
                if (permission != null) {
                    channelPermissions.put(chatChannel.getName(), permission);
                    continue;
                }
                Address address = chatChannel.getOwner();
                if (address != null && !address.equals(localAddress)) {
                    continue;
                }
                ret.add(chatChannel.getName());
            }
        }
        for (Map.Entry<String, String> e : channelPermissions.entrySet()) {
            if (plugin.getCommandSenderManager().hasPermission(identifier, e.getValue())) {
                ret.add(e.getKey());
            }
        }
        return ret;
    }

    public List<String> getChannels(String identifier) {
        List<String> ret = new ArrayList<String>();
        synchronized (LOCK) {
            for (ChatChannel chatChannel : channels.values()) {
                if (chatChannel.isMember(identifier)) {
                    ret.add(chatChannel.getName());
                }
            }
        }
        return ret;
    }

    public List<String> getMembers(String channelName) {
        List<String> ret = new ArrayList<String>();
        synchronized(LOCK) {
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
        synchronized (LOCK) {
            Map<Address, List<String>> result = new HashMap<Address,List<String>>();
            for (Address addr : servers.keySet()) {
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
                res2.put(servers.get(e.getKey()), e.getValue());
            }
            return res2;
        }
    }

    public boolean isChannelAvailable(String identifier, String channelName) {
        return getAvailableChannels(identifier).contains(channelName);
    }
    public boolean isChannelMember(String identifier, String channelName) {
        return getChannels(identifier).contains(channelName);
    }

    public String getChannelActionFormat(String channelName) {
        synchronized(LOCK) {
            ChatChannel channel = channels.get(channelName);
            return channel == null ? null : channel.getActionFormat();
        }
    }

    public String getChannelMessageFormat(String channelName) {
        synchronized(LOCK) {
            ChatChannel channel = channels.get(channelName);
            return channel == null ? null : channel.getMessageFormat();
        }
    }

    public Address getMemberAddress(String to) {
        synchronized (LOCK) {
            Member member = members.get(to);
            return member == null ? null : member.getAddress();
        }
    }

    public void onPluginMessageData(String channel, byte[] data) {
        PluginMessageEvent event = new PluginMessageEvent(channel, data);
        plugin.getServer().getPluginManager().callEvent(event);
    }
}