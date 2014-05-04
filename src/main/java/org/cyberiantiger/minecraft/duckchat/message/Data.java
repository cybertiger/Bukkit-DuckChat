/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.message;

import java.io.Serializable;

/**
 *
 * @author antony
 */
public abstract class Data implements Serializable {

    public abstract DataType getType();
    
}
