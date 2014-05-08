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
public class MessageSubCommand extends SubCommand {

    public MessageSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        if (args.length == 1) {
            return plugin.getState().getPlayerCompletions(args[0]);
        } else {
            return null;
        }
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.message")) {
            throw new PermissionException("duckchat.message");
        }
        String senderIdentifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (senderIdentifier == null) {
            throw new SenderTypeException();
        }
        if (args.length <= 1) {
            throw new UsageException();
        } else {
            String target = args[0];
            String identifier = plugin.getState().findPlayerIdentifier(target);
            if (identifier == null) {
                sender.sendMessage(plugin.translate("message.notfound", args[0]));
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
            plugin.sendMessage(sender, identifier, msg);
        }
    }

    @Override
    public String getName() {
        return "message";
    }
    
}