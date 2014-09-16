/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.state;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jgroups.Address;

/**
 *
 * @author antony
 */
public class ChatChannel implements Serializable {
    public static final int FLAG_LOCAL_AUTO_JOIN = 0;
    public static final int FLAG_GLOBAL_AUTO_JOIN = 1;

    private final Address owner;
    private final String name;
    private String messageFormat;
    private String actionFormat;
    private BitSet flags;
    private String permission;
    private final Map<String, Member> members = new HashMap<String, Member>();

    public ChatChannel(Address owner, String name, String messageFormat, String actionFormat, BitSet flags, String permission) {
        this.owner = owner;
        this.name = name;
        this.messageFormat = messageFormat;
        this.actionFormat = actionFormat;
        this.flags = flags;
        this.permission = permission;
    }

    public Address getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public String getActionFormat() {
        return actionFormat;
    }

    public boolean isAutoJoin(Member member) {
        return (member.getAddress().equals(owner) && flags.get(FLAG_LOCAL_AUTO_JOIN)) ||
                flags.get(FLAG_GLOBAL_AUTO_JOIN);
    }

    public void addMember(Member member) {
        members.put(member.getIdentifier(), member);
    }

    public void removeMember(String identifier) {
        members.remove(identifier);
    }

    public Collection<Member> getMembers() {
        return members.values();
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public void setActionFormat(String actionFormat) {
        this.actionFormat = actionFormat;
    }

    public void setFlags(BitSet flags) {
        this.flags = flags;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    boolean isMember(String identifier) {
        return members.containsKey(identifier);
    }

    @Override
    public String toString() {
        return "ChatChannel{" + "owner=" + owner + ", name=" + name + ", messageFormat=" + messageFormat + ", actionFormat=" + actionFormat + ", flags=" + flags + ", permission=" + permission + ", members=" + members + '}';
    }
}
