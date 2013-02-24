package com.piotrnowicki.jgroups;

import org.jboss.as.clustering.jgroups.ChannelFactory;
import org.jboss.as.clustering.jgroups.subsystem.ChannelFactoryService;
import org.jboss.as.clustering.jgroups.subsystem.ChannelService;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ManagedReferenceInjector;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;
import org.jgroups.Channel;

/**
 * Custom ServiceActivator that accesses the underlying JGroups protocol stack configuration, creates a new channel and
 * binds it to the JNDI.
 */
public class JGroupsChannelServiceActivator implements ServiceActivator {

    private final String JNDI_NAME = "java:jboss/channel/myChannel";

    private final String STACK_NAME = "udp";

    private final String CHANNEL_NAME = "myChannel";

    private ServiceName channelServiceName;

    @Override
    public void activate(ServiceActivatorContext context) {
        channelServiceName = ChannelService.getServiceName(CHANNEL_NAME);

        createChannel(context.getServiceTarget());

        bindChannelToJNDI(context.getServiceTarget());
    }

    void createChannel(ServiceTarget target) {
        InjectedValue<ChannelFactory> channelFactory = new InjectedValue<>();
        ServiceName serviceName = ChannelFactoryService.getServiceName(STACK_NAME);
        ChannelService channelService = new ChannelService(CHANNEL_NAME, channelFactory);

        target.addService(channelServiceName, channelService)
                .addDependency(serviceName, ChannelFactory.class, channelFactory).install();
    }

    void bindChannelToJNDI(ServiceTarget target) {
        ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(JNDI_NAME);

        BinderService binder = new BinderService(bindInfo.getBindName());

        ServiceBuilder<ManagedReferenceFactory> service =
                target.addService(bindInfo.getBinderServiceName(), binder);

        service.addAliases(ContextNames.JAVA_CONTEXT_SERVICE_NAME.append(JNDI_NAME));
        service.addDependency(channelServiceName, Channel.class, new ManagedReferenceInjector<Channel>(
                binder.getManagedObjectInjector()));
        service.addDependency(bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class,
                binder.getNamingStoreInjector());

        service.setInitialMode(ServiceController.Mode.PASSIVE).install();
    }
}
