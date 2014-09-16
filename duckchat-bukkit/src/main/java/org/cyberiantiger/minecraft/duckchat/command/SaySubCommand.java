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
public class SaySubCommand extends SubCommand<Main> {

    public SaySubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        if (!sender.hasPermission("duckchat.say")) {
            throw new PermissionException("duckchat.say");
        }
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                msg.append(' ');
            }
            msg.append(args[i]);
        }
        plugin.sendChannelMessage(sender, msg.toString());
    }

    @Override
    public String getName() {
        return "say";
    }
}