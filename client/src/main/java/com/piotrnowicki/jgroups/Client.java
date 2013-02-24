package com.piotrnowicki.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import java.io.InputStream;

/**
 * Standalone client for the JGroups.
 */
public class Client {
    public static void main(String[] args) throws Exception {

        InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream("udp.xml");

        JChannel channel = new JChannel(config);

        channel.connect("myChannel");

        channel.setReceiver(new ReceiverAdapter() {
            @Override
            public void receive(Message msg) {
                System.out.println("msg = " + msg.getObject());
            }
        });
    }
}
