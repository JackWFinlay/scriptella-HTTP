/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import scriptella.spi.*;

import java.io.IOException;

public class HTTPConnection extends AbstractConnection {
    private String HOST;
    private String BODY;
    private String TYPE;
    private int    TIME_OUT;
    private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public HTTPConnection () {} // Override default constructor

    public HTTPConnection( ConnectionParameters connectionParameters ){
        super(connectionParameters);
        HOST = connectionParameters.getStringProperty("hostname");
        TYPE = connectionParameters.getStringProperty("type");
        BODY = connectionParameters.getStringProperty("body");
        TIME_OUT = connectionParameters.getIntegerProperty("timeout");



    }

    @Override
    public void executeScript(Resource resource, ParametersCallback parametersCallback) throws ProviderException {
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setConnectTimeout(TIME_OUT);
                    }
                });

        HttpRequest request;
        try {
            request = requestFactory.buildGetRequest(generateGetRequestURL());
            request.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void executeQuery(Resource resource, ParametersCallback parametersCallback, QueryCallback queryCallback) throws ProviderException {
        //return HTTP with body contents
    }

    private GenericUrl generateGetRequestURL() {
        return new GenericUrl(HOST + "?" + BODY);

    }


    @Override
    public void close(){

    }



}
