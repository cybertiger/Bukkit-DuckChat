/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.message;

/**
 *
 * @author antony
 */
public class ServerCreateData extends Data {
    private final String name;

    public ServerCreateData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public DataType getType() {
        return DataType.SERVER_CREATE;
    }
    
}
