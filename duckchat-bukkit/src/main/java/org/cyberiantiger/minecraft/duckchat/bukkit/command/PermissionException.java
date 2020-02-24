/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.command;

/**
 *
 * @author antony
 */
public class PermissionException extends SubCommandException {
    private static final long serialVersionUID = 0L;

    private final String permission;

    public PermissionException(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
