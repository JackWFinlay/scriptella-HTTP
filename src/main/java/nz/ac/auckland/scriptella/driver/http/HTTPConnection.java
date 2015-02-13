package nz.ac.auckland.scriptella.driver.http;

/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */

import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
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
    private int timeOut;
    CloseableHttpClient httpClient;

    Logger logger = LoggerFactory.getLogger("HttpConnection");

    public HTTPConnection() {
    } // Override default constructor

    public HTTPConnection(String host, String type, int timeOut) {
        this.host = host;
        this.type = type;
        this.timeOut = timeOut;
    }

    public HTTPConnection(ConnectionParameters connectionParameters) {
        host = connectionParameters.getStringProperty("url");
        type = connectionParameters.getStringProperty("type");
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

        HttpResponse httpResponse;
        HttpGet httpGet = null;
        HttpPost httpPost = null;

        try {
            if (type.toUpperCase().equals("GET")) {

                URIBuilder uriBuilder = new URIBuilder(host);
                uriBuilder.addParameters(generateParams(resource));

                httpGet = new HttpGet(uriBuilder.build());

                httpResponse = httpClient.execute(httpGet);
            } else { // Post
                httpPost = new HttpPost(host);

                httpPost.setEntity(new UrlEncodedFormEntity(generateParams(resource)));

                httpResponse = httpClient.execute(httpPost);
            }

            logger.info("Response Status: {}", httpResponse.getStatusLine().getStatusCode());

        } catch (HttpException e) {
            logger.error("HTTP Error: ", e);
        } catch (IOException e) {
            logger.error("IO Error: ", e);
        } catch (URISyntaxException e) {
            logger.error("URI Error: ", e);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (httpPost != null) {
                httpPost.releaseConnection();
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
