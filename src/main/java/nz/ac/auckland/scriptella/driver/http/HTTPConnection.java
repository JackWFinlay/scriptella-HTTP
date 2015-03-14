package nz.ac.auckland.scriptella.driver.http;

import org.apache.commons.lang3.text.StrSubstitutor;
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

import java.io.BufferedReader;
import java.io.IOException;
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
    private HttpGet httpGet;
    private HttpResponse httpResponse;
    private HttpEntityEnclosingRequestBase httpRequestBase;

    private final int DEFAULT_TIMEOUT = 0;
    private final String DEFAULT_FORMAT = "Text";
    private final String GET = "GET";
    private final String POST = "POST";
    private final String PUT = "PUT";

    CloseableHttpClient httpClient;

    Logger logger = LoggerFactory.getLogger(HTTPConnection.class);

    public HTTPConnection() {
    } // Overrides default constructor

    /**
     * Constructor used for unit testing, allows setting of all the parameters directly.
     *
     * @param host    The URL of the endpoint.
     * @param type    The method to use for the request i.e. GET, POST, or PUT
     * @param format  The format of the content. "String" or "JSON"
     * @param timeOut The request time-out.
     */
    public HTTPConnection(String host, String type, String format, int timeOut) {
        this.host = host;
        this.type = type;
        this.format = format;
        this.timeOut = timeOut;

        setupHttpClient();
    }

    /**
     * Constructor used by Scriptella to initialise the HTTPConnection object.
     *
     * @param connectionParameters The parameters specified in the connection tag in the ETL file.
     */
    public HTTPConnection(ConnectionParameters connectionParameters) {

        super(Driver.DIALECT, connectionParameters);

        // Convert connection parameters to a map so that fields can be set.
        Properties properties = CollectionUtils.asProperties(connectionParameters.getProperties());
        properties.putAll(connectionParameters.getUrlQueryMap());

        host = connectionParameters.getUrl();
        logger.trace("Host: {}", host);

        type = properties.getProperty("type", GET);
        logger.trace("Type: {}", type);

        format = properties.getProperty("format", DEFAULT_FORMAT);
        logger.trace("Format: {}", format);

        timeOut = Integer.parseInt(properties.getOrDefault("timeout", DEFAULT_TIMEOUT).toString());
        logger.trace("Timeout: {}", timeOut);

        setupHttpClient();

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

    public HttpEntityEnclosingRequestBase getHttpRequestBase() {
        return httpRequestBase;
    }

    /**
     * Executes the request with the provided resource and passed in variables.
     *
     * @param resource           The body of the script.
     * @param parametersCallback The map of variables passed in from query that script in nested in.
     * @throws ProviderException
     */
    @Override
    public void executeScript(Resource resource, ParametersCallback parametersCallback) throws ProviderException {
        run(resource, parametersCallback);

        logger.trace("Operation complete for current entry.");
    }

    /**
     * Executes the request as a query, returning the response. Not yet implemented.
     *
     * @param resource           The body of the query
     * @param parametersCallback the map of variables.
     * @param queryCallback      The response.
     * @throws ProviderException
     */
    @Override
    public void executeQuery(Resource resource, ParametersCallback parametersCallback, QueryCallback queryCallback) throws ProviderException {
        throw new UnsupportedOperationException("Queries are not implemented in this version.");
    }

    private void run(Resource resource, ParametersCallback parametersCallback) {

        logger.trace("Built HttpClient");

        ParametersCallbackMap parameters = new ParametersCallbackMap(parametersCallback);

        try {
            if (type.toUpperCase().equals(GET)) {
                executeGetRequest(resource, parameters);

            } else {

                setRequestType(type);

                if (format.toUpperCase().equals("JSON")) { // JSON

                    executeFormattedTextRequest(resource, parameters, "JSON");
                } else if (format.toUpperCase().equals("XML")) { // JSON

                    executeFormattedTextRequest(resource, parameters, "XML");
                } else if (format.toUpperCase().equals("FORM")) {

                    executeFormRequest(resource, parameters);
                } else {

                    executePlainTextRequest(resource);

                }
            }

            logger.info("Response Status: {}", httpResponse.getStatusLine().getStatusCode());

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }



    private void setupHttpClient() {
        // Create request configurations and set timeout.
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(timeOut)
                .setSocketTimeout(timeOut).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    public void setRequestType(String type) {
        if (type.toUpperCase().equals(PUT)) {
            httpRequestBase = new HttpPut(host);
            logger.trace("Http method is PUT.");
        } else if (type.toUpperCase().equals(POST)) {
            httpRequestBase = new HttpPost(host);
            logger.trace("Http method is POST.");
        } else {
            throw new RuntimeException("Invalid http method type");
        }
    }

    public String template(Resource resource, ParametersCallbackMap parameters) {

        StringBuilder plainText = new StringBuilder("");
        String line;

        try (BufferedReader br = new BufferedReader(resource.open());) {
            while ((line = br.readLine()) != null) {
                plainText.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        StrSubstitutor strSubstitutor = new StrSubstitutor(parameters);
        String resolvedString = strSubstitutor.replace(plainText.toString());

        return resolvedString;
    }

    public void executeFormRequest(Resource resource, ParametersCallbackMap parameters) throws IOException {
        httpRequestBase.setEntity(new UrlEncodedFormEntity(generateParams(resource, parameters)));
        logger.trace("URLEncodedFormEntity created and set.");

        try {
            httpResponse = httpClient.execute(httpRequestBase);
        } catch (IOException e) {
            logger.error("Error occurred during execution of http request.");
            throw new RuntimeException(e);
        } finally {
            httpRequestBase.releaseConnection();
        }
        logger.trace("Http request executed.");
    }


    public void executeJsonRequest(Resource resource, ParametersCallbackMap parameters) {
        StringEntity se = new StringEntity(template(resource, parameters), "UTF-8");
        logger.trace("JSON parsed.");

        se.setContentType("application/json; charset=UTF-8");

        httpRequestBase.setEntity(se);
        logger.trace("JSON format entity set for http request.");

        try {
            httpResponse = httpClient.execute(httpRequestBase);
        } catch (IOException e) {
            logger.error("Error occurred during execution of http request.");
            throw new RuntimeException(e);
        } finally {
            httpRequestBase.releaseConnection();
        }

        logger.debug("Http request executed.");

    }

    public void executeFormattedTextRequest(Resource resource, ParametersCallbackMap parameters, String contentType) {
        StringEntity se = new StringEntity(template(resource, parameters), "UTF-8");
        logger.trace("Formatted text parsed.");

        if (contentType.equals("XML")) {
            se.setContentType("application/xml; charset=UTF-8");
        } else if (contentType.equals("JSON")) {
            se.setContentType("application/json; charset=UTF-8");
        }

        httpRequestBase.setEntity(se);
        logger.trace("Format entity set for http request.");

        try {
            httpResponse = httpClient.execute(httpRequestBase);
        } catch (IOException e) {
            logger.error("Error occurred during execution of http request.");
            throw new RuntimeException(e);
        } finally {
            httpRequestBase.releaseConnection();
        }

        logger.debug("Http request executed.");

    }

    public void executeGetRequest(Resource resource, ParametersCallbackMap parameters) throws URISyntaxException {
        logger.trace("Http method is GET.");

        URIBuilder uriBuilder = new URIBuilder(host);
        uriBuilder.addParameters(generateParams(resource, parameters));
        logger.trace("Added parameters to uri.");

        httpGet = new HttpGet(uriBuilder.build());

        try {
            httpResponse = httpClient.execute(httpGet);
        } catch (IOException e) {
            logger.error("Error occurred during execution of http request.");
            throw new RuntimeException(e);
        } finally {
            httpGet.releaseConnection();
        }

        logger.debug("HTTP request executed.");
    }

    private void executePlainTextRequest(Resource resource) throws IOException {
        StringBuilder plainText = new StringBuilder("");
        String line;

        try (BufferedReader br = new BufferedReader(resource.open());) {
            while ((line = br.readLine()) != null) {
                plainText.append(line);
            }

            StringEntity se = new StringEntity(plainText.toString(), "UTF-8");
            se.setContentType("text/plain; charset=UTF-8");

            httpRequestBase.setEntity(se);

            try {
                httpResponse = httpClient.execute(httpRequestBase);
            } catch (IOException e) {
                logger.error("Error occurred during execution of http request.");
                throw new RuntimeException(e);
            } finally {
                httpRequestBase.releaseConnection();
            }
            logger.trace("Http request executed.");

        }
    }


    public List<NameValuePair> generateParams(Resource resource, ParametersCallbackMap parameters) {

        String line;
        List<NameValuePair> nameValuePairList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(resource.open());) {
            while ((line = br.readLine()) != null) {
                StrSubstitutor sub = new StrSubstitutor(parameters);
                String resolvedString = sub.replace(line.toString());
                String[] components = resolvedString.trim().split("=");

                if (components.length > 1) {

                    String key = components[1];
                    if (parameters.containsKey(key)) {
                        nameValuePairList.add(new BasicNameValuePair(components[0], (String) parameters.getParameter(key)));

                        logger.trace("Variable found, added to List.");
                    } else {
                        nameValuePairList.add(new BasicNameValuePair(components[0], components[1]));
                        logger.trace("Line contains no variables, adding to list as literal.");
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error occurred while generating request parameters: ", e);
            throw new RuntimeException(e);
        }

        return nameValuePairList;
    }


    /**
     * Releases HTTP connections.
     *
     * @throws ProviderException
     */
    @Override
    public void close() throws ProviderException {

    }
}
