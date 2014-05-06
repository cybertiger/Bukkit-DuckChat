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
public class PartSubCommand extends SubCommand {

    public PartSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.part")) {
            throw new PermissionException("duckchat.part");
        }
        String identifier = plugin.getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        if (args.length == 1) {
            String channel = findIgnoringCase(args[0], plugin.getAvailableChannels(sender));
            if (channel == null) {
                sender.sendMessage(plugin.translate("part.nochannel", args[0]));
                return;
            }
            if (!plugin.getChannels(sender).contains(channel)) {
                sender.sendMessage(plugin.translate("part.membership", channel));
                return;
            }
            sender.sendMessage(plugin.translate("part.success", channel));
            plugin.sendPartChannel(sender, channel);
        } else {
            throw new UsageException();
        }
    }

    @Override
    public String getName() {
        return "part";
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