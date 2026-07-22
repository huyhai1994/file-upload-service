package org.mini_lab.file_upload_service.support;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import lombok.extern.slf4j.Slf4j;

import eu.rekawek.toxiproxy.Proxy;

import java.io.IOException;

@Slf4j
public class DatabaseConnectionResetSimulator implements AutoCloseable {

    public static final String RESET_CONNECTION = "RESET_CONNECTION";
    private final Proxy proxy;

    public DatabaseConnectionResetSimulator(Proxy proxy) throws IOException {
        this.proxy = proxy;
        proxy.toxics().resetPeer(RESET_CONNECTION, ToxicDirection.DOWNSTREAM, 0L);
    }

    @Override
    public void close() throws IOException {
        removeToxic();
    }

    private void removeToxic() throws IOException {
        proxy.toxics().get(RESET_CONNECTION).remove();
    }
}
