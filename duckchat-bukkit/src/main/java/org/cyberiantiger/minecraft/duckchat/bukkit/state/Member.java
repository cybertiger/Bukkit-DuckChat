/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.state;

import java.io.Serializable;
import java.util.BitSet;
import org.jgroups.Address;

/**
 *
 * @author antony
 */
public class Member implements Serializable {
    private static final long serialVersionUID = 0L;
    
    private final Address address;
    private final String identifier;
    private final String name;
    private BitSet flags;

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

    @Override
    public String toString() {
        return "Member{" + "address=" + address + ", identifier=" + identifier + ", name=" + name + ", flags=" + flags + '}';
    }
}