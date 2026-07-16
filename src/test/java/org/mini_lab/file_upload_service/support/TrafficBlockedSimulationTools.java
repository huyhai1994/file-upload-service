package org.mini_lab.file_upload_service.support;


import eu.rekawek.toxiproxy.model.ToxicDirection;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.ToxiproxyContainer;

import java.io.IOException;

@Slf4j
public class TrafficBlockedSimulationTools implements AutoCloseable {
    public static final String CUT_UPSTREAM = "CUT_UPSTREAM";
    public static final String CUT_DOWNSTREAM = "CUT_DOWNSTREAM";

    private final ToxiproxyContainer.ContainerProxy proxy;

    private TrafficBlockedSimulationTools(ToxiproxyContainer.ContainerProxy proxy) throws IOException {
        this.proxy = proxy;

        proxy.toxics().bandwidth(
                CUT_UPSTREAM,
                ToxicDirection.UPSTREAM,
                0
        );

        proxy.toxics().bandwidth(
                CUT_DOWNSTREAM,
                ToxicDirection.DOWNSTREAM,
                0
        );
    }

    public static TrafficBlockedSimulationTools applyTo(ToxiproxyContainer.ContainerProxy proxy) throws IOException {
        return new TrafficBlockedSimulationTools(proxy);
    }

    @Override
    public void close() throws IOException {
        log.info("toxic remove");
        removeToxic(CUT_UPSTREAM);
        removeToxic(CUT_DOWNSTREAM);
    }

    private void removeToxic(String toxicName) throws IOException {
        proxy.toxics()
                .get(toxicName)
                .remove();
    }

    public boolean isRemoved(String toxicName) throws IOException {
        return proxy.toxics()
                .getAll()
                .stream()
                .noneMatch(toxic -> toxicName.equals(toxic.getName()));
    }
}
