/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.message;

/**
 *
 * @author antony
 */
public enum DataType {
    SERVER_CREATE,
    MEMBER_CREATE,
    MEMBER_UPDATE,
    MEMBER_DELETE,
    CHANNEL_CREATE,
    CHANNEL_UPDATE,
    CHANNEL_DELETE,
    CHANNEL_MESSAGE,
    CHANNEL_JOIN,
    CHANNEL_PART,
    MESSAGE,
    PLUGIN_MESSAGE;
}
