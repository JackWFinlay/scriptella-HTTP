package scriptella.driver.http;

/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */

import scriptella.spi.AbstractScriptellaDriver;
import scriptella.spi.Connection;
import scriptella.spi.ConnectionParameters;
import scriptella.spi.DialectIdentifier;

public class Driver extends AbstractScriptellaDriver {

    static final DialectIdentifier DIALECT = new DialectIdentifier("HTTP", "1.1");

    public Connection connect(ConnectionParameters connectionParameters) {
        return new HTTPConnection(connectionParameters);
    }

}
