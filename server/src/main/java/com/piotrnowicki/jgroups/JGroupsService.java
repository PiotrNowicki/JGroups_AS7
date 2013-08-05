package com.piotrnowicki.jgroups;

import org.jboss.as.clustering.jgroups.subsystem.ChannelService;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.Value;
import org.jgroups.Channel;

/**
 * JBoss Service that is responsible for connecting to (and disconnecting from) already configured JGroups
 * {@link Channel}.
 * <p>
 * This service is required for JBoss AS > 7.1.1 or for JBoss EAP > 6.1. Starting from these versions a
 * {@link ChannelService} is not automatically connecting to a channel anymore.
 * </p>
 */
public class JGroupsService implements Service<Void> {
    private final Value<Channel> channel;
    private final String clusterName;

    private final Logger logger = Logger.getLogger(JGroupsService.class.getName());

    public JGroupsService(Value<Channel> aChannel, String aClusterName) {
        this.channel = aChannel;
        this.clusterName = aClusterName;
    }

    @Override
    public void start(StartContext aContext) throws StartException {
        logger.infov("Starting JGroups channel: {0}", channel.getValue());

        try {
            channel.getValue().connect(clusterName);
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext aContext) {
        try {
            channel.getValue().close();
        } catch (Exception e) {
            logger.error("Error while stopping service", e);
        }
    }

    @Override
    public Void getValue() {
        return null;
    }
}

