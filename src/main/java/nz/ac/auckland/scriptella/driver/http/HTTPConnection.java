package nz.ac.auckland.scriptella.driver.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scriptella.driver.script.ParametersCallbackMap;
import scriptella.spi.*;
import scriptella.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * @author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
public class HTTPConnection extends AbstractConnection {

    private String host;
    private String type;
    private String format;
    private int timeOut;
    public HttpResponse httpResponse;

    CloseableHttpClient httpClient;

    Logger logger = LoggerFactory.getLogger("HttpConnection");

    public HTTPConnection() {
    } // Override default constructor

    public HTTPConnection(String host, String type, String format, int timeOut) {
        this.host = host;
        this.type = type;
        this.format = format;
        this.timeOut = timeOut;
    }

    public HTTPConnection(ConnectionParameters connectionParameters) {

        super(Driver.DIALECT, connectionParameters);

        Properties properties = CollectionUtils.asProperties(connectionParameters.getProperties());
        properties.putAll(connectionParameters.getUrlQueryMap());

        host = connectionParameters.getUrl();
        type = properties.getProperty("type", "GET");
        format = properties.getProperty("format", "String");
        timeOut = Integer.parseInt((String) properties.getOrDefault("timeout", "500"));
    }

    public String getHost() {
        return host;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }

    public int getTimeOut() {
        return timeOut;
    }


    @Override
    public void executeScript(Resource resource, ParametersCallback parametersCallback) throws ProviderException {
        run(resource, parametersCallback);
    }

    @Override
    public void executeQuery(Resource resource, ParametersCallback parametersCallback, QueryCallback queryCallback) throws ProviderException {
        throw new NotImplementedException();
    }

    private void run(Resource resource, ParametersCallback parametersCallback) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(timeOut)
                .setSocketTimeout(timeOut).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        ParametersCallbackMap parameters = new ParametersCallbackMap(parametersCallback);

        try {
            if (type.toUpperCase().equals("GET")) {

                URIBuilder uriBuilder = new URIBuilder(host);
                uriBuilder.addParameters(generateParams(resource,parameters));

                HttpGet httpGet = new HttpGet(uriBuilder.build());

                httpResponse = httpClient.execute(httpGet);

            } else {
                HttpEntityEnclosingRequestBase httpRequest;

                if (type.toUpperCase().equals("PUT")) {
                    httpRequest = new HttpPut(host);
                } else {
                    httpRequest = new HttpPost(host);
                }

                if (format.toUpperCase().equals("STRING")) {
                    httpRequest.setEntity(new UrlEncodedFormEntity(generateParams(resource, parameters)));
                } else {

                    BufferedReader br = new BufferedReader(resource.open());

                    StringBuilder jsonString = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }

                    StringEntity se = new StringEntity(jsonString.toString(), "UTF-8");

                    se.setContentType("application/json; charset=UTF-8");

                    httpRequest.setEntity(se);
                }

                httpResponse = httpClient.execute(httpRequest);

            }

            logger.info("Response Status: {}", httpResponse.getStatusLine().getStatusCode());

        } catch (IOException | URISyntaxException e) {
            logger.error("IO Error: ", e);
        }
    }


    private List<NameValuePair> generateParams(Resource resource, ParametersCallbackMap parameters) {
        //StringResource stringResource = new StringResource(resource.toString());
        BufferedReader br = null;
        Reader r;

        try {
            br = new BufferedReader(resource.open());
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }

        String line;
        List<NameValuePair> nameValuePairList = new ArrayList<>(1);

        try {
            while ((line = br.readLine()) != null) {
                String[] components = line.trim().split("=");
                if (components.length > 1) {
                    nameValuePairList.add(new BasicNameValuePair(components[0], (String)parameters.getParameter(components[1])));
                }
            }
        } catch (IOException e) {
            logger.error("IO exception: ", e);
        }

        return nameValuePairList;
    }


    @Override
    public void close() throws ProviderException {

    }
}
