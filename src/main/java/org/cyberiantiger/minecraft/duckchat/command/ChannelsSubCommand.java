/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> available = plugin.getAvailableChannels(player);
            List<String> joined = plugin.getChannels(player);
            Collections.sort(available);
            player.sendMessage(plugin.translate("channels.header"));
            for (String channelName : available) {
                if (joined.contains(channelName)) {
                    player.sendMessage(plugin.translate("channels.joinedchannel", channelName));
                } else {
                    player.sendMessage(plugin.translate("channels.channel", channelName));
                }
            }
            player.sendMessage(plugin.translate("channels.footer"));
        } else {
            throw new SenderTypeException();
        }
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
