package org.mini_lab.file_upload_service.support.failure_simulator;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import lombok.extern.slf4j.Slf4j;

import eu.rekawek.toxiproxy.Proxy;

import java.io.IOException;

@Slf4j
public class ExternalServiceConnectionResetSimulator implements AutoCloseable {

    public static final String RESET_CONNECTION = "RESET_CONNECTION";
    private final Proxy proxy;

    private ExternalServiceConnectionResetSimulator(Proxy proxy) throws IOException {
        this.proxy = proxy;
        proxy.toxics().resetPeer(RESET_CONNECTION, ToxicDirection.DOWNSTREAM, 0L);
    }

    public static ExternalServiceConnectionResetSimulator applyTo(Proxy proxy) throws IOException {
        return new ExternalServiceConnectionResetSimulator(proxy);
    }

    @Override
    public void close() throws IOException {
        removeToxic();
    }

    private void removeToxic() throws IOException {
        proxy.toxics().get(RESET_CONNECTION).remove();
    }
}
