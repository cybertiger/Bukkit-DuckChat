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
public class JoinSubCommand extends SubCommand<Main> {

    public JoinSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.join")) {
            throw new PermissionException("duckchat.join");
        }
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        if (args.length == 1) {
            String channel = findIgnoringCase(args[0],plugin.getState().getAvailableChannels(identifier));
            if (channel == null) {
                sender.sendMessage(plugin.translate("join.nochannel", args[0]));
                return;
            }
            if (plugin.getState().getChannels(identifier).contains(channel)) {
                sender.sendMessage(plugin.translate("join.membership", channel));
                return;
            }
            sender.sendMessage(plugin.translate("join.success", channel));
            plugin.sendJoinChannel(channel, sender);
        } else {
            throw new UsageException();
        }
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (args.length == 1 && identifier != null) {
            return plugin.getState().getChannelCompletions(identifier, args[0]);
        } else {
            return null;
        }
    }
}