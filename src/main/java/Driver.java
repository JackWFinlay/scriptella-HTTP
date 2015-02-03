/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */

import scriptella.spi.AbstractScriptellaDriver;
import scriptella.spi.Connection;
import scriptella.spi.ConnectionParameters;

public class Driver extends AbstractScriptellaDriver {

    public Connection connect(ConnectionParameters connectionParameters) {
        return new HTTPConnection(connectionParameters);
    }

}
