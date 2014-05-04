/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.duckchat.Main;

/**
 *
 * @author antony
 */
public class MeSubCommand extends SubCommand {

    public MeSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.me")) {
            throw new PermissionException("duckchat.me");
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                throw new UsageException();
            }
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    msg.append(' ');
                }
                msg.append(args[i]);
            }
            plugin.sendChannelAction(player, msg.toString());
        } else {
            throw new SenderTypeException();
        }
    }

    @Override
    public String getName() {
        return "me";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

}
