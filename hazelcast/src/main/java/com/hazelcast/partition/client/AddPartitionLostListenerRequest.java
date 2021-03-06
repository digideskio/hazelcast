package com.hazelcast.partition.client;

import com.hazelcast.client.ClientEndpoint;
import com.hazelcast.client.impl.client.CallableClientRequest;
import com.hazelcast.client.impl.client.ClientPortableHook;
import com.hazelcast.client.impl.client.RetryableRequest;
import com.hazelcast.partition.InternalPartitionService;
import com.hazelcast.partition.PartitionLostEvent;
import com.hazelcast.partition.PartitionLostListener;
import com.hazelcast.spi.impl.PortablePartitionLostEvent;

import java.security.Permission;

import static com.hazelcast.partition.InternalPartitionService.PARTITION_LOST_EVENT_TOPIC;
import static com.hazelcast.partition.InternalPartitionService.SERVICE_NAME;

public class AddPartitionLostListenerRequest
        extends CallableClientRequest
        implements RetryableRequest {

    public AddPartitionLostListenerRequest() {
    }

    @Override
    public Object call() {
        final ClientEndpoint endpoint = getEndpoint();
        final InternalPartitionService partitionService = getService();

        final PartitionLostListener listener = new PartitionLostListener() {
            @Override
            public void partitionLost(PartitionLostEvent event) {
                if (endpoint.isAlive()) {
                    final PortablePartitionLostEvent portableEvent = new PortablePartitionLostEvent(event.getPartitionId(),
                            event.getLostBackupCount(), event.getEventSource());
                    endpoint.sendEvent(null, portableEvent, getCallId());
                }
            }
        };

        final String registrationId = partitionService.addPartitionLostListener(listener);
        endpoint.setListenerRegistration(SERVICE_NAME, PARTITION_LOST_EVENT_TOPIC, registrationId);
        return registrationId;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public String getMethodName() {
        return "addPartitionLostListener";
    }

    @Override
    public int getFactoryId() {
        return ClientPortableHook.ID;
    }

    @Override
    public int getClassId() {
        return ClientPortableHook.ADD_PARTITION_LOST_LISTENER;
    }

    @Override
    public Permission getRequiredPermission() {
        return null;
    }

    @Override
    public String getDistributedObjectName() {
        return null;
    }
}
