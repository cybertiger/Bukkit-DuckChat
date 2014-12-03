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
public class MeSubCommand extends SubCommand<Main> {

    public MeSubCommand(Main plugin) {
        super(plugin, "duckchat.me");
    }

    @Override
    protected void doCommand(CommandSender sender, String... args) throws SubCommandException {
        String identifier = plugin.getCommandSenderManager().getIdentifier(sender);
        if (identifier == null) {
            throw new SenderTypeException();
        }
        if (args.length == 0) {
            throw new UsageException();
        }
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                msg.append(' ');
            }
            msg.append(args[i]);
        }
        plugin.sendChannelAction(sender, msg.toString());
    }

    @Override
    public String getName() {
        return "me";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

}
