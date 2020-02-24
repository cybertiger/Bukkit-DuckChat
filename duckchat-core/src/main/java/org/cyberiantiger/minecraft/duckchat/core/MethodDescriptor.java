/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Serializable description of a method.
 * 
 * @author antony
 */
public class MethodDescriptor implements Serializable {
    private static final long serialVersionUID = 0;
    private final String type;
    private final int methodOffset;

    public MethodDescriptor(Method method) {
        this.type = method.getDeclaringClass().getName();
        Method[] declaredMethods = method.getDeclaringClass().getDeclaredMethods();
        int methodOffset = -1;
        for (int i = 0; i < declaredMethods.length; i++) {
            if (declaredMethods[i].equals(method)) {
                methodOffset = i;
                break;
            }
        }
        assert methodOffset != -1;
        this.methodOffset = methodOffset;
    }

    public Method getMethod() throws ClassNotFoundException {
        return Class.forName(type).getDeclaredMethods()[methodOffset];
    }

    public String getType() {
        return type;
    }

    public int getMethodOffset() {
        return methodOffset;
    }
}
