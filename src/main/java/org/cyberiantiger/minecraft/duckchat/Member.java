/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat;

import java.io.Serializable;
import java.util.BitSet;
import org.jgroups.Address;

/**
 *
 * @author antony
 */
public class Member implements Serializable {
    
    private final Address address;
    private final String identifier;
    private final String name;
    private BitSet flags;
    private String replyAddress;

    public Member(Address address, String identifier, String name, BitSet flags) {
        this.address = address;
        this.identifier = identifier;
        this.name = name;
        this.flags = flags;
    }

    public Address getAddress() {
        return address;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public BitSet getFlags() {
        return flags;
    }

    public void setFlags(BitSet flags) {
        this.flags = flags;
    }

    public String getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(String replyAddress) {
        this.replyAddress = replyAddress;
    }

    @Override
    public String toString() {
        return "Member{" + "address=" + address + ", identifier=" + identifier + ", name=" + name + ", flags=" + flags + ", replyAddress=" + replyAddress + '}';
    }

}