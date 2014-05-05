/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.cyberiantiger.minecraft.duckchat.PlayerState;

/**
 *
 * @author antony
 */
public class MessageSubCommand extends SubCommand {

    public MessageSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        if (args.length == 1) {
            return plugin.getPlayerCompletions(args[0]);
        } else {
            return null;
        }
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.message")) {
            throw new PermissionException("duckchat.message");
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerState state = plugin.getPlayerState(player);
            if (args.length <= 1) {
                throw new UsageException();
            } else {
                String target = args[0];
                String identifier = plugin.findPlayerIdentifier(target);
                if (identifier == null) {
                    player.sendMessage(plugin.translate("message.notfound", args[0]));
                    return;
                }
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i != 1) {
                        message.append(' ');
                    }
                    message.append(args[i]);
                }
                String msg = message.toString();
                plugin.sendMessage(player, identifier, msg);
                player.sendMessage(plugin.translate("message.sendformat", player.getName(), plugin.getPlayerName(identifier), msg));
            }
        } else {
            throw new SenderTypeException();
        }
    }

    @Override
    public String getName() {
        return "message";
    }
    
}
