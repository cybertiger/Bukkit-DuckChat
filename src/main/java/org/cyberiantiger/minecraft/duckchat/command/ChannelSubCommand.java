/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.cyberiantiger.minecraft.duckchat.CommandSenderState;

/**
 *
 * @author antony
 */
public class ChannelSubCommand extends SubCommand {

    public ChannelSubCommand(Main plugin) {
        super(plugin);
    }
    
    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.channel")) {
            throw new PermissionException("duckchat.channel");
        }
        CommandSenderState state = plugin.getCommandSenderState(sender);
        if (state == null) {
            throw new SenderTypeException();
        }
        CommandSenderState playerState = plugin.getCommandSenderState(sender);
        if (args.length == 0) {
            String currentChannel = playerState.getCurrentChannel();
            sender.sendMessage(plugin.translate("channel.current", currentChannel));
        } else if (args.length == 1) {
            String nextChannel = findIgnoringCase(args[0], plugin.getChannels(sender));
            if (nextChannel == null) {
                sender.sendMessage(plugin.translate("channel.notfound", args[0]));
            } else {
                playerState.setCurrentChannel(nextChannel);
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
        if (args.length == 1) {
            return plugin.getChannelCompletions(sender, args[0]);
        } else {
            return null;
        }
    }
}
