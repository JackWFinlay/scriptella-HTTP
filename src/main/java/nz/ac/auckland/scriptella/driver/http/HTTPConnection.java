package nz.ac.auckland.scriptella.driver.http; /**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scriptella.configuration.StringResource;
import scriptella.spi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class HTTPConnection extends AbstractConnection {

    private String host;
    private String type;
    private int timeOut;
    private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private HttpResponse httpResponse;

    Logger logger = LoggerFactory.getLogger("HttpConnection");

    public HTTPConnection () {} // Override default constructor

    public HTTPConnection (String host, String type, int timeOut) {
        this.host = host;
        this.type = type;
        this.timeOut = timeOut;
    }

    public HTTPConnection( ConnectionParameters connectionParameters ){
        host = connectionParameters.getStringProperty("url");
        type = connectionParameters.getStringProperty("type");
        timeOut = connectionParameters.getIntegerProperty("timeout");

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
                        request.setConnectTimeout(timeOut);
                    }
                });

        HttpRequest request;

        GenericUrl url = new GenericUrl(host);

        try {
            if (type.toUpperCase().equals("GET")) {

                BufferedReader br = new BufferedReader(new StringReader(((StringResource)resource).getString()));
                String line;

                while ((line = br.readLine()) != null) {
                    String [] components = line.split("=");
                    url.put(components[0],components[1]);
                }

                logger.info("Query URL: {}", url );
                request = requestFactory.buildGetRequest(url);
            } else { //if (TYPE.equals("POST")) {

                String contents = ((StringResource)resource).getString().replace("\n","&");
                // Cast to StringResource as Resource's toString() implementation is rubbish.

                logger.info("body = {}", contents);

                HttpContent httpContent = ByteArrayContent.fromString("application/x-www-form-urlencoded boundary=&", contents);

                request = requestFactory.buildPostRequest(url, httpContent);
                logger.info("Query URL: {}", url );
            }

            httpResponse = request.execute();
            logger.info("Status: {}, {}", httpResponse.getStatusCode(), httpResponse.getStatusMessage());
            logger.info("Response: {}", httpResponse.parseAsString());

        } catch (IOException e) {
            logger.error("Error: ", e);
        }

    }

    @Override
    public void close(){}

}
