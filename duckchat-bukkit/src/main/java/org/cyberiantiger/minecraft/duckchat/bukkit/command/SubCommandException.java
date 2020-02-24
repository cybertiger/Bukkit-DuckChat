/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.command;

/**
 *
 * @author antony
 */
public class SubCommandException extends Exception {
    private static final long serialVersionUID = 0L;

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
