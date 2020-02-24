/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.PermissionException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SenderTypeException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommand;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.SubCommandException;
import org.cyberiantiger.minecraft.duckchat.bukkit.command.UsageException;
import org.cyberiantiger.minecraft.duckchat.irc.command.ReloadSubCommand;

/**
 *
 * @author antony
 */
public class IRCCommandExecutor implements CommandExecutor, TabCompleter {
    private final Main main;
    private Map<String, SubCommand<?>> subcommands = new LinkedHashMap<String, SubCommand<?>>();

    public IRCCommandExecutor(Main main) {
        this.main = main;
        subcommands.put("reload", new ReloadSubCommand(main));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check for label matches.
        for (Map.Entry<String, SubCommand<?>> e : subcommands.entrySet()) {
            if (label.equalsIgnoreCase(e.getKey())) {
                executeCommand(sender, e.getValue(), label, args);
                return true;
            }
        }
        // Check for second argument matches.
        if (args.length >= 1) {
            for (Map.Entry<String, SubCommand<?>> e : subcommands.entrySet()) {
                if (e.getKey().equalsIgnoreCase(args[0])) {
                    label += " " + args[0];
                    String[] newArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                    executeCommand(sender, e.getValue(), label, newArgs);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        for (Map.Entry<String, SubCommand<?>> e : subcommands.entrySet()) {
            if (label.equalsIgnoreCase(e.getKey())) {
                return e.getValue().onTabComplete(sender, args);
            } else if (args.length >= 1 && e.getKey().equalsIgnoreCase(args[0])) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                return e.getValue().onTabComplete(sender, newArgs);
            }
        }
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            String start = args[0].toLowerCase();
            for (String s : subcommands.keySet()) {
                if (s.toLowerCase().startsWith(start)) {
                    result.add(s);
                }
            }
            return result;
        }
        return null;
    }

    private void executeCommand(CommandSender sender, SubCommand<?> cmd, String label, String[] args) {
        try {
            cmd.onCommand(sender, args);
        } catch (SenderTypeException ex) {
            sender.sendMessage(main.translate("error.wrongsender"));
        } catch (PermissionException ex) {
            sender.sendMessage(main.translate("error.permission", ex.getPermission()));
        } catch (UsageException ex) {
            sender.sendMessage(main.translate(cmd.getName() + ".usage", label));
        } catch (SubCommandException ex) {
            sender.sendMessage(main.translate("error.generic", ex.getMessage()));
        }
    }
}
