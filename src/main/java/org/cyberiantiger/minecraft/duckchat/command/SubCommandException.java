/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

/**
 *
 * @author antony
 */
public class SubCommandException extends Exception {

    public SubCommandException() {
    }

    public SubCommandException(String message) {
        super(message);
    }

    public SubCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubCommandException(Throwable cause) {
        super(cause);
    }
}
