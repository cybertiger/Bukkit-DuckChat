/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core;

import java.io.Serializable;

/**
 *
 * @author antony
 */
public interface Actor extends Addressable, Serializable {
    public String getIdentifier();
}
