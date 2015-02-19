package nz.ac.auckland.scriptella.driver.http;

import scriptella.spi.Connection;
import scriptella.spi.ConnectionParameters;
import scriptella.spi.DialectIdentifier;
import scriptella.spi.ScriptellaDriver;

/**
 * @author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */
public class Driver implements ScriptellaDriver {

    static final DialectIdentifier DIALECT = new DialectIdentifier("HTTP", "1.1");

    public Connection connect(ConnectionParameters connectionParameters) {
        return new HTTPConnection(connectionParameters);
    }

    @Override
    public String toString() {
        return "Http driver";
    }


}
