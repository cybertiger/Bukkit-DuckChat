/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.depend;

import org.bukkit.entity.Player;

/**
 *
 * @author antony
 */
public interface PlayerTitles {

    public static final PlayerTitles DEFAULT = new PlayerTitles() {

        @Override
        public String getPrefix(Player player) {
            return "";
        }

        @Override
        public String getSuffix(Player player) {
            return "";
        }

    };

    public String getPrefix(Player player);

    public String getSuffix(Player player);
    
}
