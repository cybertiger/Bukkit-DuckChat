/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.state;

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
    private final Map<String, Member> members = new HashMap<String, Member>();
    private ChatChannelMetadata metadata;

    public ChatChannel(Address owner, String name, ChatChannelMetadata metadata) {
        this.owner = owner;
        this.name = name;
        this.metadata = metadata;
    }

    public Address getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }


    public boolean isAutoJoin(Member member) {
        BitSet flags = metadata.getFlags();
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


    boolean isMember(String identifier) {
        return members.containsKey(identifier);
    }

    public ChatChannelMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ChatChannelMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ChatChannel{" + "owner=" + owner + ", name=" + name + ", members=" + members + ", metadata=" + metadata + '}';
    }
}
