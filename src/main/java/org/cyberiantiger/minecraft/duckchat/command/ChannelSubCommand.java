/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.duckchat.Main;
import org.cyberiantiger.minecraft.duckchat.PlayerState;

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
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerState playerState = plugin.getPlayerState(player);
            if (args.length == 0) {
                String currentChannel = playerState.getCurrentChannel();
                sender.sendMessage(plugin.translate("channel.current", currentChannel));
            } else if (args.length == 1) {
                String nextChannel = findIgnoringCase(args[0], plugin.getChannels(player));
                if (nextChannel == null) {
                    player.sendMessage(plugin.translate("channel.notfound", args[0]));
                } else {
                    playerState.setCurrentChannel(nextChannel);
                    player.sendMessage(plugin.translate("channel.success", nextChannel));
                }
            } else {
                throw new UsageException();
            }
        } else {
            throw new SenderTypeException();
        }
    }

    @Override
    public String getName() {
        return "channel";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        if (args.length == 1) {
            return plugin.getChannelCompletions(args[0]);
        } else {
            return null;
        }
    }
}
