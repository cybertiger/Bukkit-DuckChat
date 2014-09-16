/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.message;

import java.util.BitSet;

/**
 * @author antony
 */
public class MemberUpdateData extends Data {

    private final String identifier;
    private BitSet flags;

    public MemberUpdateData(String identifier, BitSet flags) {
        this.identifier = identifier;
        this.flags = flags;
    }

    public String getIdentifier() {
        return identifier;
    }

    public BitSet getFlags() {
        return flags;
    }

    @Override
    public DataType getType() {
        return DataType.MEMBER_UPDATE;
    }

    @Override
    public String toString() {
        return "MemberUpdateData{" + "identifier=" + identifier + ", flags=" + flags + '}';
    }
}
