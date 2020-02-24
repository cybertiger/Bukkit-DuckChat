/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cyberiantiger.minecraft.duckchat.core.state.State;
import org.cyberiantiger.minecraft.duckchat.core.state.State.StateProvider;
import org.cyberiantiger.minecraft.duckchat.core.state.State.StateUpdater;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 *
 * @author antony
 */
public final class Network {

    private final Logger log;
    private final String channel;
    private final String node;
    private final File config;
    private final boolean debug;
    private final BiMap<String,Address> addresses = HashBiMap.create();
    private final JChannel channelImpl;

    private final Map<Class<? extends State>, State.StateProvider> stateProviders =
            new HashMap<Class<? extends State>, State.StateProvider>();
    private final Map<Key<? extends State>, State> state =
            new HashMap<Key<? extends State>, State>();
    private final Map<Key<? extends State>, State.StateUpdater> stateUpdater =
            new HashMap<Key<? extends State>, State.StateUpdater>();

    private final ReceiverAdapter receiver = new ReceiverAdapter() {
        @Override
        public void viewAccepted(View view) {
            BiMap<String,Address> added = HashBiMap.create();
            BiMap<String,Address> removed = HashBiMap.create();

            BiMap<Address,String> current = addresses.inverse();
            Set<Address> members = new HashSet<Address>(view.getMembers());
            
            Iterator<Map.Entry<Address,String>> itr = current.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Address,String> e = itr.next();
                if (!members.contains(e.getKey())) {
                    itr.remove();
                    removed.put(e.getValue(), e.getKey());
                }
            }
            
            for (Address addr : members) {
                if (!current.containsKey(addr)) {
                    String name = channelImpl.getName(addr);
                    added.put(name, addr);
                    current.put(addr,name);
                }
            }

            log.log(Level.FINE, "View from {2} changed, added={0}, removed={1}", new Object[]{added, removed, node});


        }

        @Override
        public void receive(Message msg) {
            Object[] data = (Object[]) msg.getObject(Network.class.getClassLoader());
            String msgType = (String) data[0];
            if ("state".equals(msgType)) {
                updateState(msg.getSrc(), (State) data[1]);
            } else if("staterpc".equals(msgType)) {
                try {
                    Address src = msg.getSrc();
                    Class<? extends State> stateClass = (Class<? extends State>) Class.forName((String)data[1]);
                    MethodDescriptor descriptor = (MethodDescriptor) data[2];
                    Method method = descriptor.getMethod();
                    Object[] parameters = (Object[]) data[3];
                    updateStateRPC(src, stateClass, method, parameters);
                } catch (ClassNotFoundException ex) {
                }
                
            }
        }
        
    };

    protected <T extends State> void updateStateRPC(Address owner, Class<T> type, Method method, Object[] parameters) {
        
    }

    protected void updateState(Address master, State data) {
        Class<? extends State> type = data.getClass();
        Key<? extends State> key = new Key(master, type);
        state.put(key, data);
        stateUpdater.put(key, stateProviders.get(data.getClass()).createStateUpdater(data));
        // TODO update listeners.
    }

    public Network(Logger log, String channel, String node) throws Exception {
        this(log, channel, node, false);
    }

    public Network(Logger log, String channel, String node, boolean debug) throws Exception {
        this(log, channel, node, debug, null);
    }

    public Network(Logger log, String channel, String node, boolean debug, File config) throws Exception {
        this.log = log;
        this.channel = channel;
        this.node = node;
        this.debug = debug;
        this.config = config;
        for (StateProvider<?,?> p : ServiceLoader.load(StateProvider.class)) {
            stateProviders.put(p.getStateClass(), p);
        }
        channelImpl = config == null ? new JChannel() : new JChannel(config);
        channelImpl.setName(node);
        channelImpl.setDiscardOwnMessages(true);
        if (debug) {
            channelImpl.getProtocolStack().getTransport().enableDiagnostics();
        } else {
            channelImpl.getProtocolStack().getTransport().disableDiagnostics();
        }
        channelImpl.setReceiver(receiver);
    }

    public Logger getLog() {
        return log;
    }

    public String getChannel() {
        return channel;
    }

    public String getNode() {
        return node;
    }

    public File getConfig() {
        return config;
    }

    public boolean isDebug() {
        return debug;
    }

    public Address getAddress() {
        return channelImpl.getAddress();
    }

    public void connect() throws Exception {
        addresses.clear();
        state.clear();
        stateUpdater.clear();
        channelImpl.connect(channel);
    }

    public void disconnect() {
        channelImpl.disconnect();
    }

    public void close() {
        channelImpl.close();
    }

    private final class Key<T> {
        private final Address address;
        private final Class<? extends T> type;

        public Key(Address address, Class<? extends T> type) {
            this.address = address;
            this.type = type;
        }

        public Address getAddress() {
            return address;
        }

        public Class<? extends T> getType() {
            return type;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + (this.address != null ? this.address.hashCode() : 0);
            hash = 83 * hash + (this.type != null ? this.type.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key<T> other = (Key<T>) obj;
            if (this.address != other.address && (this.address == null || !this.address.equals(other.address))) {
                return false;
            }
            if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
                return false;
            }
            return true;
        }
    }
}