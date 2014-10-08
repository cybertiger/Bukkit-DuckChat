/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core.service.tag;

/**
 *
 * @author antony
 */
public @interface ParamFlag {
    ParamFlagType value() default ParamFlagType.SERIALIZE;
}
