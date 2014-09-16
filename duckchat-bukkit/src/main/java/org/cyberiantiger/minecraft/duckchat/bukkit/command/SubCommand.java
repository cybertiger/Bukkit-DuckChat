/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author antony
 */
public abstract class SubCommand<T extends JavaPlugin> {
    protected final T plugin;

    public SubCommand(T plugin) {
        this.plugin = plugin;
    }

    public abstract List<String> onTabComplete(CommandSender sender, String... args);

    public abstract void onCommand(CommandSender sender, String... args) throws SubCommandException;

    public abstract String getName();

    protected String findIgnoringCase(String toFind, List<String> options) {
        toFind = toFind.toLowerCase();
        for (String s : options) {
            if (s.toLowerCase().equals(toFind)) {
                return s;
            }
        }
        return null;
    }
}
