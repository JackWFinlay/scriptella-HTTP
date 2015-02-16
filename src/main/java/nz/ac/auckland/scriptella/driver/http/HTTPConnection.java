package nz.ac.auckland.scriptella.driver.http;

/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scriptella.configuration.StringResource;
import scriptella.spi.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class HTTPConnection extends AbstractConnection {

    private String host;
    private String type;
    private String format;
    private int timeOut;

    CloseableHttpClient httpClient;

    Logger logger = LoggerFactory.getLogger("HttpConnection");

    public HTTPConnection() {
    } // Override default constructor

    public HTTPConnection(String host, String type, String format, int timeOut ) {
        this.host = host;
        this.type = type;
        this.format = format;
        this.timeOut = timeOut;
    }

    public HTTPConnection(ConnectionParameters connectionParameters) {
        host = connectionParameters.getStringProperty("url");
        type = connectionParameters.getStringProperty("type");
        format = connectionParameters.getStringProperty("format");
        timeOut = connectionParameters.getIntegerProperty("timeout");
    }


    @Override
    public void executeScript(Resource resource, ParametersCallback parametersCallback) throws ProviderException {
        run(resource);
    }

    @Override
    public void executeQuery(Resource resource, ParametersCallback parametersCallback, QueryCallback queryCallback) throws ProviderException {
        throw new NotImplementedException();
    }

    private void run(Resource resource) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(timeOut)
                .setSocketTimeout(timeOut).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpResponse httpResponse = null;
        HttpEntityEnclosingRequestBase httpRequest = null;
        HttpGet httpGet = null;

        try {
            if (type.toUpperCase().equals("GET")) {

                URIBuilder uriBuilder = new URIBuilder(host);
                uriBuilder.addParameters(generateParams(resource));

                httpGet = new HttpGet(uriBuilder.build());

                httpResponse = httpClient.execute(httpGet);

            } else {

                if (type.toUpperCase().equals("PUT")) {
                    httpRequest = new HttpPut(host);
                } else {
                    httpRequest = new HttpPost(host);
                }

                if (format.toUpperCase().equals("STRING")) {
                    httpRequest.setEntity(new UrlEncodedFormEntity(generateParams(resource)));
                } else {
                    StringEntity se = new StringEntity(((StringResource) resource).getString(), "UTF-8");
                    se.setContentType("application/json; charset=UTF-8");

                    httpRequest.setEntity(se);
                }

                httpResponse = httpClient.execute(httpRequest);

            }

            logger.info("Response Status: {}", httpResponse.getStatusLine().getStatusCode());

        } catch ( IOException | URISyntaxException e) {
            logger.error("IO Error: ", e);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            } else {
                httpRequest.releaseConnection();
            }
        }

    }


    private List<NameValuePair> generateParams(Resource resource) {
        BufferedReader br = new BufferedReader(new StringReader(((StringResource) resource).getString()));
        String line;

        List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>(1);

        try {
            while ((line = br.readLine()) != null) {
                String[] components = line.split("=");
                nameValuePairList.add(new BasicNameValuePair(components[0], components[1]));
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
