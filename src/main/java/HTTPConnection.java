/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scriptella.spi.*;

import java.io.IOException;

public class HTTPConnection extends AbstractConnection {
    private String HOST;
    private String BODY;
    private String TYPE;
    private int    TIME_OUT;
    private final  HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private HttpResponse httpResponse;

    Logger logger = LoggerFactory.getLogger("HttpConnection");

    public HTTPConnection () {} // Override default constructor

    public HTTPConnection( ConnectionParameters connectionParameters ){
        HOST = connectionParameters.getStringProperty("url");
        TYPE = connectionParameters.getStringProperty("type");
        TIME_OUT = connectionParameters.getIntegerProperty("timeout");

    }

    @Override
    public void executeScript(Resource scriptContent, ParametersCallback parametersCallback) throws ProviderException {
        run(scriptContent);
    }

    @Override
    public void executeQuery(Resource queryContent, ParametersCallback parametersCallback, QueryCallback queryCallback) throws ProviderException {
        //return HTTP with body contents
    }

    private void run(Resource resource){

        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setConnectTimeout(TIME_OUT);
                    }
                });

        HttpRequest request;

        HTTPUrl url = new HTTPUrl(HOST);

        try {
            if (TYPE.equals("GET")) {
                url.fields = resource.toString();

                logger.info("Query URL: {}", url );
                request = requestFactory.buildGetRequest(url);
            } else { //if (TYPE.equals("POST")) {

                HttpContent httpContent = (HttpContent) resource;

                request = requestFactory.buildPostRequest(url, httpContent);
                logger.info("Request response: {}", request.toString());
            }

            httpResponse = request.execute();

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
