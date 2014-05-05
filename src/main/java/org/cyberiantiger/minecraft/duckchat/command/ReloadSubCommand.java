/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.cyberiantiger.minecraft.duckchat.Main;

/**
 *
 * @author antony
 */
public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.reload")) {
            throw new PermissionException("duckchat.reload");
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
