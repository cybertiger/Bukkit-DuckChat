/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core.state;

import java.io.Serializable;

/**
 *
 * @author antony
 */
public abstract class State implements Serializable {

    public interface StateUpdater<T extends State> {
    }

    public interface StateProvider<T extends State, U extends StateUpdater<T>> {
        public Class<T> getStateClass();

        public T createState();

        public Class<U> getStateUpdaterInterface();

        public U createStateUpdater(T state);
    }

    public State() {
    }
}