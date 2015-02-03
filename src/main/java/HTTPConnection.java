/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Key;
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
        HOST = connectionParameters.getStringProperty("url");
        TYPE = connectionParameters.getStringProperty("type");
        TIME_OUT = connectionParameters.getIntegerProperty("timeout");

    }

    @Override
    public void executeScript(Resource resource, ParametersCallback parametersCallback) throws ProviderException {


    }

    @Override
    public void executeQuery(Resource resource, ParametersCallback parametersCallback, QueryCallback queryCallback) throws ProviderException {
        //return HTTP with body contents
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setConnectTimeout(TIME_OUT);
                    }
                });

        HttpRequest request;

        HTTPUrl url = new HTTPUrl(HOST);
        //url.fields =

        try {
            if (TYPE.equals("GET")) {

                request = requestFactory.buildGetRequest(url);
            } else if (TYPE.equals("POST")) {

                //request = requestFactory.buildPostRequest(HOST, new HttpCo);
            }

            //HttpResponse httpResponse = request.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void close(){

    }

    public static class HTTPUrl extends GenericUrl {

        public HTTPUrl(String encodedUrl) {
            super(encodedUrl);
        }

        @Key
        public String fields;
    }


}
