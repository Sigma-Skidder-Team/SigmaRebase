package org.newdawn.slick.util;

import java.io.InputStream;
import java.net.URL;

public interface Identifier {
    InputStream getResourceAsStream(String ref);

    URL getResource(String ref);
}
