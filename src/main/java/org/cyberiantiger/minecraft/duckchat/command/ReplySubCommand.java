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
        // Permission.
        if (!sender.hasPermission("duckchat.reply")) {
            throw new PermissionException("duckchat.reply");
        }
        // Sender
        String senderIdentifier = plugin.getIdentifier(sender);
        if (senderIdentifier == null) {
            throw new SenderTypeException();
        }
        // Has current reply address.
        String replyAddress = plugin.getReplyAddress(sender);
        if (replyAddress == null) {
            sender.sendMessage(plugin.translate("reply.notfound"));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(plugin.translate("reply.address", replyAddress));
        } else {
            StringBuilder message = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    message.append(' ');
                }
                message.append(args[i]);
            }
            String msg = message.toString();
            plugin.sendMessage(sender, replyAddress, msg);
        }
    }

    @Override
    public String getName() {
        return "reply";
    }
}
