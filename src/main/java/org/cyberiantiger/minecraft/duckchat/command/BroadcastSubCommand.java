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
public class BroadcastSubCommand extends SubCommand<Main>
{

    public BroadcastSubCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public void onCommand(CommandSender sender, String... args) throws SubCommandException {
        boolean global = args.length > 0 && "-g".equals(args[0]);
        if (args.length == (global ? 1 : 0)) {
            throw new UsageException();
        }
        StringBuilder msg = new StringBuilder();
        int argLength = args.length -1;
        for (int i = global ? 1 : 0; i < argLength; i++) {
            msg.append(args[i]);
            msg.append(' ');
        }
        if (global) {
            plugin.sendBroadcast(msg.toString());
        } else {
            plugin.sendBroadcast(plugin.getState().getLocalAddress(), msg.toString());
        }
    }

    @Override
    public String getName() {
        return "broadcast";
    }
    
}
