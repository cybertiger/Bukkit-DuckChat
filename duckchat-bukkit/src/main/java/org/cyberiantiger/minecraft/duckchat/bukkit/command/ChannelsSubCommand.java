/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.cyberiantiger.minecraft.duckchat.bukkit.Main;

/**
 *
 * @author antony
 */
public class ChannelsSubCommand extends SubCommand<Main> {
    public ChannelsSubCommand(Main plugin) {
        super(plugin, "duckchat.channels");
    }

    @Override
    protected void doCommand(CommandSender sender, String... args) throws SubCommandException {
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        List<String> available = plugin.getState().getAvailableChannels(identifier);
        List<String> joined = plugin.getState().getChannels(identifier);
        Collections.sort(available, String.CASE_INSENSITIVE_ORDER);
        sender.sendMessage(plugin.translate("channels.header"));
        for (String channelName : available) {
            if (joined.contains(channelName)) {
                sender.sendMessage(plugin.translate("channels.joinedchannel", channelName));
            } else {
                sender.sendMessage(plugin.translate("channels.channel", channelName));
            }
        }
        sender.sendMessage(plugin.translate("channels.footer"));
    }

    @Override
    public String getName() {
        return "channels";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }
}
