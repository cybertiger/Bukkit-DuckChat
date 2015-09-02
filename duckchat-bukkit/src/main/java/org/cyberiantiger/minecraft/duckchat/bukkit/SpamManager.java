/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.duckchat.bukkit.state.ChatChannel;
import org.cyberiantiger.minecraft.duckchat.bukkit.state.ChatChannelMetadata;

/**
 *
 * @author antony
 */
public class SpamManager {
    public enum SpamResult {
        ALLOW,
        SPAM,
        REPEAT;
    }

    private final Map<CommandSender, PlayerLimiter> limiters = new WeakHashMap<CommandSender, PlayerLimiter>();

    public synchronized  SpamResult allowMessage(CommandSender player, boolean action, String channelName, ChatChannelMetadata channel, String message) {
        PlayerLimiter limiter = limiters.get(player);
        if (limiter == null) {
            limiter = new PlayerLimiter();
            limiters.put(player, limiter);
        }
        return limiter.allowMessage(channelName, channel, new ChatMessage(System.currentTimeMillis(), action, message));
    }

    private class PlayerLimiter {
        private final Map<String, ChannelLimiter> limiters = new HashMap<String, ChannelLimiter>();

        private SpamResult allowMessage(String channelName, ChatChannelMetadata channel, ChatMessage chatMessage) {
            ChannelLimiter limiter = limiters.get(channelName);
            if (limiter == null) {
                limiter = new ChannelLimiter();
                limiters.put(channelName, limiter);
            }

            return limiter.allowMessage(channel, chatMessage);
        }
    }

    private class ChannelLimiter {
        private final ArrayDeque<ChatMessage> spamHistory = new ArrayDeque<ChatMessage>();
        private final ArrayDeque<ChatMessage> repeatHistory = new ArrayDeque<ChatMessage>();

        private SpamResult allowMessage(ChatChannelMetadata channel, ChatMessage chatMessage) {
            long now = chatMessage.getWhen();
            long spamWindow = channel.getSpamWindow();
            int spamThreshold = channel.getSpamThreshold();
            long repeatWindow = channel.getRepeatWindow();
            int repeatThreshold = channel.getRepeatThreshold();

            if (spamWindow > 0L && spamThreshold > 0) {
                long timeoutTime = now - spamWindow;
                while (!spamHistory.isEmpty() && spamHistory.getLast().getWhen() < timeoutTime)
                    spamHistory.removeLast();
                if (spamHistory.size() >= spamThreshold) {
                    return SpamResult.SPAM;
                }
            }
            if (repeatWindow > 0L && repeatThreshold > 0) {
                long timeoutTime = now - repeatWindow;
                while (!repeatHistory.isEmpty() && repeatHistory.getLast().getWhen() < timeoutTime)
                    repeatHistory.removeLast();
                String message = chatMessage.getMessage();
                int count = 0;
                for (ChatMessage msg : repeatHistory) {
                    if (message.equalsIgnoreCase(chatMessage.getMessage())) 
                        count++;
                }
                if (count >= repeatThreshold) {
                    return SpamResult.REPEAT;
                }
            }
            spamHistory.addFirst(chatMessage);
            repeatHistory.addFirst(chatMessage);
            return SpamResult.ALLOW;
        }
    }

    private class ChatMessage {
        private final String message;
        private final boolean action;
        private final long when;

        public String getMessage() {
            return message;
        }

        public boolean isAction() {
            return action;
        }

        public long getWhen() {
            return when;
        }
        public ChatMessage(long when, boolean action, String message) {
            this.when = when;
            this.action = action;
            this.message = message;
        }
    }
}
