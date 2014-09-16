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
public class ChannelSubCommand extends SubCommand<Main> {

    public ChannelSubCommand(Main plugin) {
        super(plugin);
    }
    
    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.channel")) {
            throw new PermissionException("duckchat.channel");
        }
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        if (args.length == 0) {
            String currentChannel = plugin.getCommandSenderManager().getCurrentChannel(sender);
            sender.sendMessage(plugin.translate("channel.current", currentChannel));
        } else if (args.length == 1) {
            String nextChannel = findIgnoringCase(args[0], plugin.getState().getChannels(identifier));
            if (nextChannel == null) {
                sender.sendMessage(plugin.translate("channel.notfound", args[0]));
            } else {
                plugin.getCommandSenderManager().setCurrentChannel(sender, nextChannel);
                sender.sendMessage(plugin.translate("channel.success", nextChannel));
            }
        } else {
            throw new UsageException();
        }
    }

    @Override
    public String getName() {
        return "channel";
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
