package com.piotrnowicki.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Exemplary usage of the injected JGroups channel. This way it can be used from the application on the same app
 * server.
 */
@Singleton
@Startup
public class JGroupsSampleDataProducer {
    private static final Logger LOGGER = Logger.getLogger(JGroupsSampleDataProducer.class.getName());

    @Resource(lookup = "java:jboss/channel/myChannel")
    private JChannel channel;

    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {

        // This is a good moment to register some receivers.
        channel.setReceiver(new ReceiverAdapter() {
            @Override
            public void viewAccepted(View aView) {
                LOGGER.info("Cluster view changed. Members: " + aView.getMembers());
            }
        });

        // Invoke @Timeout method every 2 seconds starting from now. Do not persist the timer between server restarts.
        timerService.createIntervalTimer(new Date(), TimeUnit.SECONDS.toMillis(2), new TimerConfig(null,
                false));
    }

    @Timeout
    public void timeout() {
        String value = UUID.randomUUID().toString();

        LOGGER.info("Sending value: " + value);

        try {
            channel.send(new Message(null, null, value));
        } catch (Exception e) {
            // do something with it.
            e.printStackTrace();
        }
    }
}
