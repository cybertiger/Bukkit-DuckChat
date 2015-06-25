/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc.config;

/**
 *
 * @author antony
 */
public class IRCAction {
    private IRCActionType type;
    private String target;
    private String message;

    public IRCActionType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }
}
