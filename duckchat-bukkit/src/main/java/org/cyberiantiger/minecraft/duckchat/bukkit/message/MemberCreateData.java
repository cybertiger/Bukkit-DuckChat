/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

import java.util.BitSet;

/**
 *
 * @author antony
 */
public class MemberCreateData extends Data {
    private final String identifier;
    private final String name;
    private final BitSet flags;

    public MemberCreateData(String identifier, String name, BitSet flags) {
        this.identifier = identifier;
        this.name = name;
        this.flags = flags;
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

    @Override
    public DataType getType() {
        return DataType.MEMBER_CREATE;
    }

    @Override
    public String toString() {
        return "MemberCreateData{" + "identifier=" + identifier + ", name=" + name + ", flags=" + flags + '}';
    }
}
