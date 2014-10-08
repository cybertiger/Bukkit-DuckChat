/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core.service.tag;

/**
 * @author antony
 */
public enum InvocationType {
    /**
     * For service calls which are always broadcast.
     */
    MULTICAST_MESSAGE,
    /**
     * For service calls which are always unicast to available service providers.
     */
    UNICAST_MESSAGE,
    /**
     * For services calls which are unicast based upon the parameters passed.
     * See TargetAddress, TargetNode and TargetActor
     */
    TARGETTED_MESSAGE;
}
