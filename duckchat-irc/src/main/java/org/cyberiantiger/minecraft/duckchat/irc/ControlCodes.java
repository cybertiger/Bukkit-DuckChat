/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

/**
 *
 * @author antony
 */
public interface ControlCodes {
    static final char MINECRAFT_CONTROL_CODE = '\u00a7';
    static final char MINECRAFT_OBFUSCATED = 'k';
    static final char MINECRAFT_BOLD = 'l';
    static final char MINECRAFT_STRIKETHROUGH = 'm';
    static final char MINECRAFT_UNDERLINE = 'n';
    static final char MINECRAFT_ITALIC = 'o';
    static final char MINECRAFT_RESET = 'r';
    
    static final char IRC_COLOR_CODE = '\u0003';
    static final char IRC_BOLD = '\u0002';
    static final char IRC_ITALIC = '\u0016';
    static final char IRC_UNDERLINE = '\u001f';
    static final char IRC_RESET = '\u000f';


    public String white();
    public String black();
    public String blue();
    public String green();
    public String cyan();
    public String red();
    public String magenta();
    public String yellow();
    public String gray();
    public String lightGray();
    public String lightBlue();
    public String lightGreen();
    public String lightCyan();
    public String lightRed();
    public String lightMagenta();
    public String lightYellow();

    public String reset();
    public String bold();
    public String strike();
    public String underline();
    public String italic();
    public String obfuscated();

    public static final ControlCodes IRC = new ControlCodes() {

        @Override
        public String white() {
            return "" + IRC_COLOR_CODE + "00";
        }

        @Override
        public String black() {
            return "" + IRC_COLOR_CODE + "01";
        }

        @Override
        public String blue() {
            return "" + IRC_COLOR_CODE + "02";
        }

        @Override
        public String green() {
            return "" + IRC_COLOR_CODE + "03";
        }

        @Override
        public String cyan() {
            return "" + IRC_COLOR_CODE + "10";
        }

        @Override
        public String red() {
            return "" + IRC_COLOR_CODE + "05";
        }

        @Override
        public String magenta() {
            return "" + IRC_COLOR_CODE + "06";
        }

        @Override
        public String yellow() {
            return "" + IRC_COLOR_CODE + "07";
        }

        @Override
        public String gray() {
            return "" + IRC_COLOR_CODE + "14";
        }

        @Override
        public String lightGray() {
            return "" + IRC_COLOR_CODE + "15";
        }

        @Override
        public String lightBlue() {
            return "" + IRC_COLOR_CODE + "12";
        }

        @Override
        public String lightGreen() {
            return "" + IRC_COLOR_CODE + "09";
        }

        @Override
        public String lightCyan() {
            return "" + IRC_COLOR_CODE + "11";
        }

        @Override
        public String lightRed() {
            return "" + IRC_COLOR_CODE + "04";
        }

        @Override
        public String lightMagenta() {
            return "" + IRC_COLOR_CODE + "13";
        }

        @Override
        public String lightYellow() {
            return "" + IRC_COLOR_CODE + "08";
        }

        @Override
        public String reset() {
            return "" + IRC_RESET;
        }

        @Override
        public String bold() {
            return "" + IRC_BOLD;
        }

        @Override
        public String strike() {
            return "";
        }

        @Override
        public String underline() {
            return "" + IRC_UNDERLINE;
        }

        @Override
        public String italic() {
            return "" + IRC_ITALIC;
        }

        @Override
        public String obfuscated() {
            return "";
        }
    };

    public static final ControlCodes MINECRAFT = new ControlCodes() {

        @Override
        public String white() {
            return "" + MINECRAFT_CONTROL_CODE + 'f';
        }

        @Override
        public String black() {
            return "" + MINECRAFT_CONTROL_CODE + '0';
        }

        @Override
        public String blue() {
            return "" + MINECRAFT_CONTROL_CODE + '1';
        }

        @Override
        public String green() {
            return "" + MINECRAFT_CONTROL_CODE + '2';
        }

        @Override
        public String cyan() {
            return "" + MINECRAFT_CONTROL_CODE + '3';
        }

        @Override
        public String red() {
            return "" + MINECRAFT_CONTROL_CODE + '4';
        }

        @Override
        public String magenta() {
            return "" + MINECRAFT_CONTROL_CODE + '5';
        }

        @Override
        public String yellow() {
            return "" + MINECRAFT_CONTROL_CODE + '6';
        }

        @Override
        public String gray() {
            return "" + MINECRAFT_CONTROL_CODE + '8';
        }

        @Override
        public String lightGray() {
            return "" + MINECRAFT_CONTROL_CODE + '7';
        }

        @Override
        public String lightBlue() {
            return "" + MINECRAFT_CONTROL_CODE + '9';
        }

        @Override
        public String lightGreen() {
            return "" + MINECRAFT_CONTROL_CODE + 'a';
        }

        @Override
        public String lightCyan() {
            return "" + MINECRAFT_CONTROL_CODE + 'b';
        }

        @Override
        public String lightRed() {
            return "" + MINECRAFT_CONTROL_CODE + 'c';
        }

        @Override
        public String lightMagenta() {
            return "" + MINECRAFT_CONTROL_CODE + 'd';
        }

        @Override
        public String lightYellow() {
            return "" + MINECRAFT_CONTROL_CODE + 'e';
        }

        @Override
        public String reset() {
            return "" + MINECRAFT_CONTROL_CODE + MINECRAFT_RESET;
        }

        @Override
        public String bold() {
            return "" + MINECRAFT_CONTROL_CODE + MINECRAFT_BOLD;
        }

        @Override
        public String strike() {
            return "" + MINECRAFT_CONTROL_CODE + MINECRAFT_STRIKETHROUGH;
        }

        @Override
        public String underline() {
            return "" + MINECRAFT_CONTROL_CODE + MINECRAFT_UNDERLINE;
        }

        @Override
        public String italic() {
            return "" + MINECRAFT_CONTROL_CODE + MINECRAFT_ITALIC;
        }

        @Override
        public String obfuscated() {
            return "" + MINECRAFT_CONTROL_CODE + MINECRAFT_OBFUSCATED;
        }
    };
    
    public static final ControlCodes NO_COLORS = new ControlCodes() {

        @Override
        public String white() {
            return "";
        }

        @Override
        public String black() {
            return "";
        }

        @Override
        public String blue() {
            return "";
        }

        @Override
        public String green() {
            return "";
        }

        @Override
        public String cyan() {
            return "";
        }

        @Override
        public String red() {
            return "";
        }

        @Override
        public String magenta() {
            return "";
        }

        @Override
        public String yellow() {
            return "";
        }

        @Override
        public String gray() {
            return "";
        }

        @Override
        public String lightGray() {
            return "";
        }

        @Override
        public String lightBlue() {
            return "";
        }

        @Override
        public String lightGreen() {
            return "";
        }

        @Override
        public String lightCyan() {
            return "";
        }

        @Override
        public String lightRed() {
            return "";
        }

        @Override
        public String lightMagenta() {
            return "";
        }

        @Override
        public String lightYellow() {
            return "";
        }

        @Override
        public String reset() {
            return "";
        }

        @Override
        public String bold() {
            return "";
        }

        @Override
        public String strike() {
            return "";
        }

        @Override
        public String underline() {
            return "";
        }

        @Override
        public String italic() {
            return "";
        }

        @Override
        public String obfuscated() {
            return "";
        }
    };
}
