/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

/**
 *
 * @author antony
 */
public class MemberDeleteData extends Data {

    private final String identifier;

    public MemberDeleteData(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public DataType getType() {
        return DataType.MEMBER_DELETE;
    }

    @Override
    public String toString() {
        return "MemberDeleteData{" + "identifier=" + identifier + '}';
    }
}
