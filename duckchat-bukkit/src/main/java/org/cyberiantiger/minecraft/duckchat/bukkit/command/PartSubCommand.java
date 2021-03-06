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
public class PartSubCommand extends SubCommand<Main> {

    public PartSubCommand(Main plugin) {
        super(plugin, "duckchat.part");
    }

    @Override
    protected void doCommand(CommandSender sender, String... args) throws SubCommandException {
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        if (args.length == 1) {
            String channel = findIgnoringCase(args[0], plugin.getState().getAvailableChannels(identifier));
            if (channel == null) {
                sender.sendMessage(plugin.translate("part.nochannel", args[0]));
                return;
            }
            if (!plugin.getState().getChannels(identifier).contains(channel)) {
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
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (args.length == 1) {
            return plugin.getState().getChannelCompletions(identifier, args[0]);
        } else {
            return null;
        }
    }
}