/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.state;

import java.io.InputStream;
import java.io.OutputStream;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelCreateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelDeleteData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelMessageData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.Data;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelJoinData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MemberCreateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MemberDeleteData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MemberUpdateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelPartData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ChannelUpdateData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.MessageData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.PluginMessageData;
import org.cyberiantiger.minecraft.duckchat.bukkit.message.ServerCreateData;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 *
 * @author antony
 */
public class DuckReceiver extends ReceiverAdapter {

    private final StateManager state;

    public DuckReceiver(StateManager state) {
        this.state = state;
    }

    @Override
    public void receive(Message msg) {
        Address src = msg.getSrc();
        Data data = (Data) msg.getObject();
        if (data != null) {
            switch (data.getType()) {
                case SERVER_CREATE:
                    ServerCreateData serverCreateData = (ServerCreateData) data;
                    state.onServerCreate(src, serverCreateData.getName());
                    break;
                case MEMBER_CREATE:
                    MemberCreateData memberCreateData = (MemberCreateData) data;
                    state.onMemberCreate(src, memberCreateData.getIdentifier(), memberCreateData.getName(), memberCreateData.getFlags());
                    break;
                case MEMBER_UPDATE:
                    MemberUpdateData memberUpdateData = (MemberUpdateData) data;
                    state.onMemberUpdate(memberUpdateData.getIdentifier(), memberUpdateData.getFlags());
                    break;
                case MEMBER_DELETE:
                    MemberDeleteData memberDeleteData = (MemberDeleteData) data;
                    state.onMemberDelete(src, memberDeleteData.getIdentifier());
                    break;
                case CHANNEL_CREATE:
                    ChannelCreateData channelCreateData = (ChannelCreateData) data;
                    state.onChannelCreate(channelCreateData.getOwner(), channelCreateData.getName(), channelCreateData.getMessageFormat(), channelCreateData.getActionFormat(), channelCreateData.getFlags(), channelCreateData.getPermission());
                    break;
                case CHANNEL_UPDATE:
                    ChannelUpdateData channelModifyData = (ChannelUpdateData) data;
                    state.onChannelUpdate(channelModifyData.getName(), channelModifyData.getMessageFormat(), channelModifyData.getActionFormat(), channelModifyData.getFlags(), channelModifyData.getPermission());
                    break;
                case CHANNEL_DELETE:
                    ChannelDeleteData channelDeleteData = (ChannelDeleteData) data;
                    state.onChannelDelete(channelDeleteData.getName());
                    break;
                case CHANNEL_JOIN:
                    ChannelJoinData channelJoinData = (ChannelJoinData) data;
                    state.onChannelJoin(channelJoinData.getChannel(), channelJoinData.getIdentifier());
                    break;
                case CHANNEL_PART:
                    ChannelPartData channelPartData = (ChannelPartData) data;
                    state.onChannelPart(channelPartData.getChannel(), channelPartData.getIdentifier());
                    break;
                case CHANNEL_MESSAGE:
                    ChannelMessageData channelMessageData = (ChannelMessageData) data;
                    state.onChannelMessage(channelMessageData.getChannel(), channelMessageData.getIdentifier(), channelMessageData.getMessage());
                    break;
                case MESSAGE:
                    MessageData messageData = (MessageData) data;
                    state.onMessage(messageData.getFrom(), messageData.getTo(), messageData.getMessage());
                    break;
                case PLUGIN_MESSAGE:
                    PluginMessageData pluginMessageData = (PluginMessageData) data;
                    state.onPluginMessageData(pluginMessageData.getChannel(), pluginMessageData.getData());
                    break;
            }
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        state.set(input);
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        state.get(output);
    }

    @Override
    public void viewAccepted(View view) {
        state.onViewUpdated(view.getMembers());
    }

    @Override
    public void suspect(Address addr) {
        state.onSuspect(addr);
    }

    
}