/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.cyberiantiger.minecraft.duckchat.bukkit.Main;

/**
 *
 * @author antony
 */
public class ReplySubCommand extends SubCommand<Main> {

    public ReplySubCommand(Main plugin) {
        super(plugin, "duckchat.reply");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

    @Override
    protected void doCommand(CommandSender sender, String... args) throws SubCommandException {
        // Sender
        String senderIdentifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (senderIdentifier == null) {
            throw new SenderTypeException();
        }
        // Has current reply address.
        String replyAddress = plugin.getCommandSenderManager().getReplyAddress(sender);
        if (replyAddress == null) {
            sender.sendMessage(plugin.translate("reply.notfound"));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(plugin.translate("reply.address", plugin.getState().getPlayerName(replyAddress)));
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
