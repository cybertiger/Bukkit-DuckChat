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
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1) {
                String channel = findIgnoringCase(args[0],plugin.getAvailableChannels(player));
                if (channel == null) {
                    player.sendMessage(plugin.translate("join.nochannel", args[0]));
                    return;
                }
                if (plugin.getChannels(player).contains(channel)) {
                    player.sendMessage(plugin.translate("join.membership", channel));
                    return;
                }
                player.sendMessage(plugin.translate("join.success", channel));
                plugin.sendJoinChannel(player, channel);
            } else {
                throw new UsageException();
            }
        } else {
            throw new SenderTypeException();
        }
    }

    @Override
    public String getName() {
        return "join";
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