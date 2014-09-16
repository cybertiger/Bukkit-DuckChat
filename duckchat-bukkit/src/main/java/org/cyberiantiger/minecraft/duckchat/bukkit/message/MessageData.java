/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.bukkit.message;

/**
 *
 * @author antony
 */
public class MessageData extends Data {

    private String from;
    private String to;
    private String message;
    
    public MessageData(String from, String to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "MessageData{" + "from=" + from + ", to=" + to + ", message=" + message + '}';
    }

    @Override
    public DataType getType() {
        return DataType.MESSAGE;
    }
}
