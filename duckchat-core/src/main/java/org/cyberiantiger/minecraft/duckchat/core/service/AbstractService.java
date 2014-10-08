/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core.service;

import org.cyberiantiger.minecraft.duckchat.core.Network;

/**
 * @author antony
 */
public class AbstractService {
    protected final Network network;

    public AbstractService(Network network) {
        this.network = network;
    }
}
