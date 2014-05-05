/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.cyberiantiger.minecraft.duckchat.message.ChannelCreateData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelDeleteData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelMessageData;
import org.cyberiantiger.minecraft.duckchat.message.Data;
import org.cyberiantiger.minecraft.duckchat.message.ChannelJoinData;
import org.cyberiantiger.minecraft.duckchat.message.MemberCreateData;
import org.cyberiantiger.minecraft.duckchat.message.MemberDeleteData;
import org.cyberiantiger.minecraft.duckchat.message.MemberUpdateData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelPartData;
import org.cyberiantiger.minecraft.duckchat.message.ChannelUpdateData;
import org.cyberiantiger.minecraft.duckchat.message.MessageData;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 *
 * @author antony
 */
public class DuckReceiver extends ReceiverAdapter {

    private final Main plugin;

    public DuckReceiver(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void receive(Message msg) {
        Address src = msg.getSrc();
        Data data = (Data) msg.getObject();
        switch (data.getType()) {
            case MEMBER_CREATE:
                MemberCreateData memberCreateData = (MemberCreateData) data;
                plugin.createMember(src, memberCreateData.getIdentifier(), memberCreateData.getName(), memberCreateData.getFlags());
                break;
            case MEMBER_UPDATE:
                MemberUpdateData memberUpdateData = (MemberUpdateData) data;
                plugin.updateMember(memberUpdateData.getIdentifier(), memberUpdateData.getFlags());
                break;
            case MEMBER_DELETE:
                MemberDeleteData memberDeleteData = (MemberDeleteData) data;
                plugin.deleteMember(src, memberDeleteData.getIdentifier());
                break;
            case CHANNEL_CREATE:
                ChannelCreateData channelCreateData = (ChannelCreateData) data;
                plugin.createChannel(channelCreateData.getOwner(), channelCreateData.getName(), channelCreateData.getMessageFormat(), channelCreateData.getActionFormat(), channelCreateData.getFlags(), channelCreateData.getPermission());
                break;
            case CHANNEL_UPDATE:
                ChannelUpdateData channelModifyData = (ChannelUpdateData) data;
                plugin.updateChannel(channelModifyData.getName(), channelModifyData.getMessageFormat(), channelModifyData.getActionFormat(), channelModifyData.getFlags(), channelModifyData.getPermission());
                break;
            case CHANNEL_DELETE:
                ChannelDeleteData channelDeleteData = (ChannelDeleteData) data;
                plugin.deleteChannel(channelDeleteData.getName());
                break;
            case CHANNEL_JOIN:
                ChannelJoinData channelJoinData = (ChannelJoinData) data;
                plugin.joinChannel(channelJoinData.getChannel(), channelJoinData.getIdentifier());
                break;
            case CHANNEL_PART:
                ChannelPartData channelPartData = (ChannelPartData) data;
                plugin.partChannel(channelPartData.getChannel(), channelPartData.getIdentifier());
                break;
            case CHANNEL_MESSAGE:
                ChannelMessageData channelMessageData = (ChannelMessageData) data;
                plugin.messageChannel(channelMessageData.getChannel(), channelMessageData.getIdentifier(), channelMessageData.getMessage());
                break;
            case MESSAGE:
                MessageData messageData = (MessageData) data;
                plugin.message(messageData.getFrom(), messageData.getTo(), messageData.getMessage());
                break;
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        plugin.setState(input);
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        plugin.getState(output);
    }

    @Override
    public void viewAccepted(View view) {
        // TODO: Netsplit - this only deals with the naive case of a single node joining.
        plugin.viewUpdated(view.getMembers());
    }
}