/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat;

/**
 *
 * @author antony
 */
public class PlayerState {

    private String currentChannel;
    private String replyAddress = null;

    public PlayerState(String currentChannel) {
        this.currentChannel = currentChannel;
    }

    public String getCurrentChannel() {
        return currentChannel;
    }

    public void setCurrentChannel(String currentChannel) {
        this.currentChannel = currentChannel;
    }

    public String getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(String replyAddress) {
        this.replyAddress = replyAddress;
    }
}
