
package org.mini_lab.file_upload_service.component;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface InputStreamSupplier {
    InputStream open() throws IOException;
}
