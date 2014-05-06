/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat;

/**
 *
 * @author antony
 */
public class CommandSenderState {

    private String currentChannel;
    private String replyAddress = null;

    public CommandSenderState(String currentChannel) {
        this.currentChannel = currentChannel;
    }

    public String getCurrentChannel() {
        return currentChannel;
    }

    public void setCurrentChannel(String currentChannel) {
        this.currentChannel = currentChannel;
    }
}
