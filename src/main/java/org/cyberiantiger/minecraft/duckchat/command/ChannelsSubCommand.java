/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.cyberiantiger.minecraft.duckchat.Main;

/**
 *
 * @author antony
 */
public class ChannelsSubCommand extends SubCommand {
    public ChannelsSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.channels")) {
            throw new PermissionException("duckchat.channels");
        }
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
