/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

/**
 *
 * @author antony
 */
public interface ControlCodeTranslator {

    String translate(String message, ControlCodes target, boolean reverse);

    public static final ControlCodeTranslator IRC = new ControlCodeTranslator() {

        private void appendColor(StringBuilder ret, int color, ControlCodes target, boolean reverse) {
            switch (color & 0xf) {
                case 0:
                    ret.append(reverse ? target.black() : target.white());
                    break;
                case 1:
                    ret.append(reverse ? target.white() : target.black());
                    break;
                case 2:
                    ret.append(target.blue());
                    break;
                case 3:
                    ret.append(target.green());
                    break;
                case 4:
                    ret.append(target.lightRed());
                    break;
                case 5:
                    ret.append(target.red());
                    break;
                case 6:
                    ret.append(target.magenta());
                    break;
                case 7:
                    ret.append(target.yellow());
                    break;
                case 8:
                    ret.append(target.lightYellow());
                    break;
                case 9:
                    ret.append(target.lightGreen());
                    break;
                case 10:
                    ret.append(target.cyan());
                    break;
                case 11:
                    ret.append(target.lightCyan());
                    break;
                case 12:
                    ret.append(target.lightBlue());
                    break;
                case 13:
                    ret.append(target.lightMagenta());
                    break;
                case 14:
                    ret.append(target.gray());
                    break;
                case 15:
                    ret.append(target.lightGray());
                    break;
            }
        }

        @Override
        public String translate(String message, ControlCodes target, boolean reverse) {
            StringBuilder foreground = new StringBuilder();
            StringBuilder background = new StringBuilder();
            int state = 0;
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < message.length(); i++) {
                char ch = message.charAt(i);
                switch (state) {
                    case 0:
                        switch (ch) {
                            case ControlCodes.IRC_BOLD:
                                ret.append(target.bold());
                                break;
                            case ControlCodes.IRC_ITALIC:
                                ret.append(target.italic());
                                break;
                            case ControlCodes.IRC_UNDERLINE:
                                ret.append(target.underline());
                                break;
                            case ControlCodes.IRC_RESET:
                                ret.append(target.reset());
                                break;
                            case ControlCodes.IRC_COLOR_CODE:
                                state = 1;
                                break;
                            default:
                                ret.append(ch);
                                break;
                        }
                        break;
                    case 1:
                        if (foreground.length() < 2 && Character.isDigit(ch)) {
                            foreground.append(ch);
                            break;
                        } else if (',' == ch) {
                            state = 2;
                        } else {
                            if (foreground.length() > 0) {
                                appendColor(ret, Integer.parseInt(foreground.toString()), target, reverse);
                                foreground.setLength(0);
                            } else {
                                ret.append(target.reset());
                            }
                            i--;
                            state = 0;
                        }
                break;
                    case 2:
                        if (background.length() < 2 && Character.isDigit(ch)) {
                            background.append(ch);
                        } else {
                            if (foreground.length() > 0) {
                                appendColor(ret, Integer.parseInt(foreground.toString()), target, reverse);
                                foreground.setLength(0);
                            } else if (background.length() == 0) {
                                ret.append(',');
                            }
                            background.setLength(0);
                            i--;
                            state = 0;
                        }
                }
            }
            // Ignore trailing color codes unless we're going to eat a ','
            if (state == 2 && background.length() == 0) {
                if (foreground.length() == 0) {
                    ret.append(target.reset());
                } else {
                    appendColor(ret, Integer.parseInt(foreground.toString()), target, reverse);
                }
                ret.append(',');
            }
            // Whoever invented irc color codes should be shot.
            return ret.toString();
        }
    };

    public static final ControlCodeTranslator MINECRAFT = new ControlCodeTranslator() {
        @Override
        public String translate(String message, ControlCodes target, boolean reverse) {
            int state = 0;
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < message.length(); i++) {
                char ch = message.charAt(i);
                switch (state) {
                    case 0:
                        if (ch == ControlCodes.MINECRAFT_CONTROL_CODE) {
                            state = 1;
                        } else {
                            ret.append(ch);
                        }
                        break;
                    case 1:
                        switch (ch) {
                            case ControlCodes.MINECRAFT_CONTROL_CODE:
                                break;
                            case '0':
                                ret.append(reverse ? target.white() : target.black());
                                state = 0;
                                break;
                            case '1':
                                ret.append(target.blue());
                                state = 0;
                                break;
                            case '2':
                                ret.append(target.green());
                                state = 0;
                                break;
                            case '3':
                                ret.append(target.cyan());
                                state = 0;
                                break;
                            case '4':
                                ret.append(target.red());
                                state = 0;
                                break;
                            case '5':
                                ret.append(target.magenta());
                                state = 0;
                                break;
                            case '6':
                                ret.append(target.yellow());
                                state = 0;
                                break;
                            case '7':
                                ret.append(target.lightGray());
                                state = 0;
                                break;
                            case '8':
                                ret.append(target.gray());
                                state = 0;
                                break;
                            case '9':
                                ret.append(target.lightBlue());
                                state = 0;
                                break;
                            case 'a':
                            case 'A':
                                ret.append(target.lightGreen());
                                state = 0;
                                break;
                            case 'b':
                            case 'B':
                                ret.append(target.lightCyan());
                                state = 0;
                                break;
                            case 'c':
                            case 'C':
                                ret.append(target.lightRed());
                                state = 0;
                                break;
                            case 'd':
                            case 'D':
                                ret.append(target.lightMagenta());
                                state = 0;
                                break;
                            case 'e':
                            case 'E':
                                ret.append(target.lightYellow());
                                state = 0;
                                break;
                            case 'f':
                            case 'F':
                                ret.append(reverse ? target.black() : target.white());
                                state = 0;
                                break;
                            case 'k':
                            case 'K':
                                ret.append(target.obfuscated());
                                state = 0;
                                break;
                            case 'l':
                            case 'L':
                                ret.append(target.bold());
                                state = 0;
                                break;
                            case 'm':
                            case 'M':
                                ret.append(target.strike());
                                state = 0;
                                break;
                            case 'n':
                            case 'N':
                                ret.append(target.underline());
                                state = 0;
                                break;
                            case 'o':
                            case 'O':
                                ret.append(target.italic());
                                state = 0;
                                break;
                            case 'r':
                            case 'R':
                                ret.append(target.reset());
                                state = 0;
                                break;
                            default:
                                state = 1;
                                break;
                        }
                        break;
                }
            }
            return ret.toString();
        }
    };
    
}
