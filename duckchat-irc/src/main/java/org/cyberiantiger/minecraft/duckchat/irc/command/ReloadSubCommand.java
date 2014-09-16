/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.cyberiantiger.minecraft.duckchat.irc.Main;
import org.cyberiantiger.minecraft.duckchat.command.PermissionException;
import org.cyberiantiger.minecraft.duckchat.command.SubCommand;
import org.cyberiantiger.minecraft.duckchat.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.command.UsageException;

/**
 *
 * @author antony
 */
public class ReloadSubCommand extends SubCommand<Main> {

    public ReloadSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchatirc.reload")) {
            throw new PermissionException("duckchatirc.reload");
        }
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
