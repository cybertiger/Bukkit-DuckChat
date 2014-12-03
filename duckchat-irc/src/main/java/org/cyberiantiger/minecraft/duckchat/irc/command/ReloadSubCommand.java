/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.cyberiantiger.minecraft.duckchat.irc.Main;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.UsageException;

/**
 *
 * @author antony
 */
public class ReloadSubCommand extends SubCommand<Main> {

    public ReloadSubCommand(Main plugin) {
        super(plugin, "duckchatirc.reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

    @Override
    protected void doCommand(CommandSender sender, String... args) throws SubCommandException {
        if (args.length != 0) {
            throw new UsageException();
        }
        plugin.reload();
        sender.sendMessage(plugin.translate("reload.success"));
    }

    @Override
    public String getName() {
        return "reload";
    }
    
}
