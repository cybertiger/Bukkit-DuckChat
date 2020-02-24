/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

import java.io.Serializable;

/**
 *
 * @author antony
 */
public abstract class Data implements Serializable {
    private static final long serialVersionUID = 0L;

    public abstract DataType getType();
    
}
