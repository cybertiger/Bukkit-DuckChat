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
public class JoinSubCommand extends SubCommand {

    public JoinSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.join")) {
            throw new PermissionException("duckchat.join");
        }
        String identifier = plugin.getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        if (args.length == 1) {
            String channel = findIgnoringCase(args[0],plugin.getAvailableChannels(sender));
            if (channel == null) {
                sender.sendMessage(plugin.translate("join.nochannel", args[0]));
                return;
            }
            if (plugin.getChannels(sender).contains(channel)) {
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
        if (args.length == 1) {
            return plugin.getChannelCompletions(sender, args[0]);
        } else {
            return null;
        }
    }
}