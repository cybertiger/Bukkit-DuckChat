/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.irc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author antony
 */
public class ChannelModeParser {
    // For InspircD, excluding +qaohv
    private static final String FLAGS_WITH_ARGS = "bkldeFfgHJjLwX";
    Map<String, ModeChange> modeChanges = new HashMap<String,ModeChange>();

    public ChannelModeParser(String line) {
        String[] parts = line.split(" ");
        List<String> modes = new ArrayList<String>();
        List<String> args = new ArrayList<String>();
        for (String s : parts) {
            if (s.startsWith("+") || s.startsWith("-")) {
                modes.add(s);
            } else {
                args.add(s);
            }
        }
        int arg = 0;
        for (String s : modes) {
            boolean setFlag = false;
            boolean clearFlag = false;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                switch (ch) {
                    case '+':
                        setFlag = true;
                        clearFlag = false;
                        break;
                    case '-':
                        setFlag = false;
                        clearFlag = true;
                        break;
                    default:
                        ChannelMode mode = ChannelMode.getModeByFlag(ch);
                        if (mode != null) {
                            String user = args.get(arg++);
                            ModeChange change = modeChanges.get(user);
                            if (change == null) {
                                change = new ModeChange();
                                modeChanges.put(user, change);
                            }
                            if (setFlag) {
                                change.getSetModes().set(mode.ordinal());
                                change.getClearedModes().clear(mode.ordinal());
                            }
                            if (clearFlag) {
                                change.getSetModes().clear(mode.ordinal());
                                change.getClearedModes().set(mode.ordinal());
                            }
                        } else if (FLAGS_WITH_ARGS.indexOf(ch) != -1) {
                            arg++;
                        }
                        break;
                }
                // We only care about mode changes with arguments so if we run out of
                // arguments, don't bother parsing the rest.
                if (arg >= args.size()) 
                    break;
            }
        }
    }

    public Map<String, ModeChange> getModeChanges() {
        return modeChanges;
    }

    public static class ModeChange {
        private BitSet setModes = new BitSet();
        private BitSet clearedModes = new BitSet();

        public BitSet getSetModes() {
            return setModes;
        }

        public BitSet getClearedModes() {
            return clearedModes;
        }
    }
}
