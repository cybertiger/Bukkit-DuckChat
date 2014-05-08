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
public class ChannelListSubCommand extends SubCommand {

    public ChannelListSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        if (args.length == 1) {
            return plugin.getState().getChannelCompletions(plugin.getCommandSenderManager().getIdentifier(sender), args[0]);
        } else {
            return null;
        }
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.channellist")) {
            throw new PermissionException("duckchat.channellist");
        }
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        String channel;
        if (args.length == 0) {
            channel = plugin.getCommandSenderManager().getCurrentChannel(sender);
            if (!plugin.getState().getAvailableChannels(identifier).contains(channel)) {
                sender.sendMessage(plugin.translate("channellist.notfound", channel));
                return;
            }
        } else if (args.length == 1) {
            channel = findIgnoringCase(args[0], plugin.getState().getAvailableChannels(identifier));
            if (channel == null) {
                sender.sendMessage(plugin.translate("channellist.notfound", args[0]));
                return;
            }
        } else {
            throw new UsageException();
        }
        List<String> members = plugin.getState().getMembers(channel);
        Collections.sort(members);
        StringBuilder memberList = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            if (i != 0) {
                memberList.append(", ");
            }
            memberList.append(members.get(i));
        }
        sender.sendMessage(plugin.translate("channellist.header", channel));
        sender.sendMessage(memberList.toString());
    }

    @Override
    public String getName() {
        return "channellist";
    }
    
}
