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
public class ReplySubCommand extends SubCommand {

    public ReplySubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.reply")) {
            throw new PermissionException("duckchat.reply");
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerState state = plugin.getPlayerState(player);
            if (args.length == 0) {
                throw new UsageException();
            } else {
                if (state.getReplyAddress() == null) {
                    player.sendMessage(plugin.translate("reply.notfound"));
                    return;
                }
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    if (i != 0) {
                        message.append(' ');
                    }
                    message.append(args[i]);
                }
                String msg = message.toString();
                player.sendMessage(plugin.translate("message.sendformat", player.getName(), plugin.getPlayerName(state.getReplyAddress()), msg));
                plugin.sendMessage(player, state.getReplyAddress(), message.toString());
            }
        } else {
            throw new SenderTypeException();
        }
    }

    @Override
    public String getName() {
        return "reply";
    }
}
