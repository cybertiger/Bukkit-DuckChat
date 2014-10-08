/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.duckchat.core;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author antony
 */
public class NetworkTest {
    private static final Logger LOG = Logger.getLogger(NetworkTest.class.getName());
    
    public NetworkTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        LOG.setLevel(Level.ALL);
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.ALL);
        LOG.addHandler(console);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void basicTest() throws Exception {
        Network netA = new Network(LOG,"junit","a",false);
        Network netB = new Network(LOG,"junit","b",false);
        netA.connect();
        netB.connect();
        netA.disconnect();
        netB.disconnect();
        netA.connect();
        netB.connect();
        netA.disconnect();
        netB.disconnect();
        netA.close();
        netB.close();
    }
}