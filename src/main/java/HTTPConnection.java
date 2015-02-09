/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scriptella.configuration.StringResource;
import scriptella.spi.*;
import scriptella.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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

    public int getTIME_OUT() {
        return TIME_OUT;
    }

    public void setTIME_OUT(int TIME_OUT) {
        this.TIME_OUT = TIME_OUT;
    }

    public String getHOST() {
        return HOST;
    }

    public void setHOST(String HOST) {
        this.HOST = HOST;
    }

    public String getBODY() {
        return BODY;
    }

    public void setBODY(String BODY) {
        this.BODY = BODY;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
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
            if (TYPE.toUpperCase().equals("GET")) {

                BufferedReader br = new BufferedReader(new StringReader(((StringResource)resource).getString()));
                String line;

                while ((line = br.readLine()) != null) {
                    String [] components = line.split("=");
                    url.put(components[0],components[1]);
                }

                logger.info("Query URL: {}", url );
                request = requestFactory.buildGetRequest(url);
            } else { //if (TYPE.equals("POST")) {

                HttpContent httpContent = ByteArrayContent.fromString(null, ((StringResource)resource).getString());

                request = requestFactory.buildPostRequest(url, httpContent);
                logger.info("Query URL: {}", url );
                logger.info("Request contents: {}", request.getContent().toString());
            }

            httpResponse = request.execute();
            logger.info("Status: {}, {}", httpResponse.getStatusCode(), httpResponse.getStatusMessage());
            logger.info("Response: {}", httpResponse.parseAsString());

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
