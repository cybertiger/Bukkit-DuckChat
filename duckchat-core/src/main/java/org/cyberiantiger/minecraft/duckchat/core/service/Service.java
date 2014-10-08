/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core.service;

import org.cyberiantiger.minecraft.duckchat.core.Network;

/**
 *
 * @author antony
 */
public interface Service {
    public interface ServiceProvider<T extends Service> {
        public Class<T> getServiceClass();
        public T createService(Network network);
    }
}
