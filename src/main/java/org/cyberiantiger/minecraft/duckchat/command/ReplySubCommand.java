/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.cyberiantiger.minecraft.duckchat.CommandSenderState;

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
        // Usage
        if (args.length == 0) {
            throw new UsageException();
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
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                message.append(' ');
            }
            message.append(args[i]);
        }
        String msg = message.toString();
        plugin.sendMessage(sender, replyAddress, message.toString());
    }

    @Override
    public String getName() {
        return "reply";
    }
}
