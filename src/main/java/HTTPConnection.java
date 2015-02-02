import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import scriptella.spi.*;

/**
 * Created by Jack on 3/02/2015.
 */
public class HTTPConnection extends AbstractConnection {
    private final String HOST = "host";
    private final String PARAMS = "parameters";
    private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public HTTPConnection () {} // Override default constructor

    public HTTPConnection( ConnectionParameters connectionParameters ){
        super(connectionParameters);


    }

    @Override
    public void executeScript(Resource resource, ParametersCallback parametersCallback) throws ProviderException {
        // call HTTP with parameters
    }

    @Override
    public void executeQuery(Resource resource, ParametersCallback parametersCallback, QueryCallback queryCallback) throws ProviderException {
        //return HTTP with body contents
    }

    @Override
    public void close(){

    }
}
