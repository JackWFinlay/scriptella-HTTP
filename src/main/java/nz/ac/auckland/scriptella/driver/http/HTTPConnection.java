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

    public HttpResponse httpResponse;
    public final int DEFAULT_TIMEOUT = 0;

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
        logger.debug("Host: {}", host);

        type = properties.getProperty("type", "GET");
        logger.debug("Type: {}", type);

        format = properties.getProperty("format", "String");
        logger.debug("Format: {}", format);

        timeOut = Integer.parseInt(properties.getOrDefault("timeout", DEFAULT_TIMEOUT).toString());
        logger.debug("Timeout: {}", timeOut);

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

        logger.debug("Operation complete for current entry.");
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
        throw new UnsupportedOperationException();
    }

    private void run(Resource resource, ParametersCallback parametersCallback) {

        // Create request configurations and set timeout.
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeOut)
                .setConnectionRequestTimeout(timeOut)
                .setSocketTimeout(timeOut).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        logger.debug("Built HttpClient");

        ParametersCallbackMap parameters = new ParametersCallbackMap(parametersCallback);

        try {
            if (type.toUpperCase().equals("GET")) {
                logger.debug("Http method is GET.");

                URIBuilder uriBuilder = new URIBuilder(host);
                uriBuilder.addParameters(generateParams(resource, parameters));
                logger.debug("Added parameters to uri.");

                HttpGet httpGet = new HttpGet(uriBuilder.build());

                httpResponse = httpClient.execute(httpGet);
                logger.debug("HTTP request executed.");

            } else {
                HttpEntityEnclosingRequestBase httpRequest;

                if (type.toUpperCase().equals("PUT")) {
                    httpRequest = new HttpPut(host);
                    logger.debug("Http method is PUT.");
                } else {
                    httpRequest = new HttpPost(host);
                    logger.debug("Http method is POST.");
                }

                if (format.toUpperCase().equals("STRING")) {

                    httpRequest.setEntity(new UrlEncodedFormEntity(generateParams(resource, parameters)));
                    logger.debug("URLEncodedFormEntity created and set.");
                } else { // JSON

                    StringEntity se = new StringEntity(parseJSON(resource, parameters), "UTF-8");
                    logger.debug("JSON parsed.");

                    se.setContentType("application/json; charset=UTF-8");

                    httpRequest.setEntity(se);
                    logger.debug("JSON format entity set for http request.");
                }

                httpResponse = httpClient.execute(httpRequest);
                logger.debug("Http request executed.");
            }

            logger.info("Response Status: {}", httpResponse.getStatusLine().getStatusCode());

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    private List<NameValuePair> generateParams(Resource resource, ParametersCallbackMap parameters) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(resource.open());
            logger.debug("Resource opened for reading.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String line;
        List<NameValuePair> nameValuePairList = new ArrayList<>(1);

        try {
            while ((line = br.readLine()) != null) {
                String[] components = line.trim().split("=");
                if (components.length > 1) {
                    // Remove "$" and double-quotes used to make driver work as user would expect based on csv driver and others.

                    String key = components[1].replace("$", "").replace("?", "").replace("\"", "");
                    logger.debug("Stripped \'$\' and \'?\' from variables.");
                    if (parameters.containsKey(key)) {
                        nameValuePairList.add(new BasicNameValuePair(components[0], (String) parameters.getParameter(key)));

                        logger.debug("Variable found, added to List.");
                    } else {
                        nameValuePairList.add(new BasicNameValuePair(components[0], components[1]));
                        logger.debug("Line contains no variables, adding to list as literal.");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return nameValuePairList;
    }


    private String parseJSON(Resource resource, ParametersCallbackMap parameters) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(resource.open());
            logger.debug("Resource opened for reading.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Using an array list as "\n" in strings tends to break things.
        List<String> jsonString = new ArrayList<>();
        String line;

        try {
            while ((line = br.readLine()) != null) {
                jsonString.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder parsedJSON = new StringBuilder("");

        for (String jsonLine : jsonString) {
            // Check if line has a comma as it is stripped out if value is replaced.
            String comma = jsonLine.contains(",") ? "," : "";

            String[] split = jsonLine.trim().split(":");
            if (split.length > 1) {
                // Check if value is meant to be a variable and not literal value with "$" in it.
                if (split[1].contains("$") && !split[1].contains("\"")) {
                    String key = split[1].trim().replace("$", "").replace(",", "");
                    split[1] = ("\"" + ((String) parameters.getParameter(key)) + "\"" + comma);

                    logger.debug("JSON string contains variable, replaced with value.");
                }

                parsedJSON.append(split[0] + ":" + split[1]);

            } else {
                parsedJSON.append(jsonLine);
            }
        }

        return parsedJSON.toString();
    }

    /**
     * Required override for inheriting from Abstract connection. Not implemented.
     * @throws ProviderException
     */
    @Override
    public void close() throws ProviderException {
    }
}
