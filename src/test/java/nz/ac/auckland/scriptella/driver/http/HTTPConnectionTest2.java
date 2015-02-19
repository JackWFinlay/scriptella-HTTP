package nz.ac.auckland.scriptella.driver.http;

import nz.ac.auckland.morc.MorcTestBuilder;
import nz.ac.auckland.morc.TestBean;
import org.apache.camel.Exchange;
import scriptella.configuration.StringResource;
import scriptella.spi.ConnectionParameters;
import scriptella.spi.MockConnectionParameters;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
public class HTTPConnectionTest2 extends MorcTestBuilder {

    public void configure() {

        final ParametersCallback parametersCallback;

        parametersCallback = new ParametersCallback() {
            @Override
            public Object getParameter(String s) {
                return null;
            }
        };

        syncTest("script GET test", new TestBean() {
            @Override
            public void run() throws Exception {

                // Set properties
                // The default format is String, default timeout is 500
                Map<String, String> props = new HashMap<>();
                props.put("type", "GET");

                // Set up the connection parameters. MockConnectionParameters allows params to be set outside scriptella etl.xml files.
                ConnectionParameters cp = new MockConnectionParameters(props, "http://127.0.0.1:8080/xyz");

                // The resource used to build the URL with params.
                Resource resource = new StringResource("abc=123\n" +
                        "def=456\n" +
                        "ghi=789");

                // Create the HTTPConnection instance with the set properties.
                HTTPConnection httpConnection = new HTTPConnection(cp);
                // Execute the HTTP request.
                httpConnection.executeScript(resource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080/xyz").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"), header(Exchange.HTTP_URI, "/xyz"))));

        syncTest("script POST test - String", new TestBean() {
            @Override
            public void run() throws Exception {

                Map<String, String> props = new HashMap<>();
                props.put("type", "POST");
                props.put("format", "STRING");
                // "String" is actually the default, evaluation of variable is case insensitive.
                // This uppercase just shows that it can be set.

                ConnectionParameters cp = new MockConnectionParameters(props, "http://127.0.0.1:8080");

                Resource resource = new StringResource("abc=123\n" +
                        "def=456\n" +
                        "ghi=789");

                HTTPConnection httpConnection = new HTTPConnection(cp);
                httpConnection.executeScript(resource, parametersCallback);

                // Tests that the format is in fact "STRING".
                assertFalse(httpConnection.getFormat().equals("String"));
                assertTrue(httpConnection.getFormat().equals("STRING"));
            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"))));

        syncTest("script POST test - JSON", new TestBean() {
            @Override
            public void run() throws Exception {

                Map<String, String> props = new HashMap<>();
                props.put("type", "POST");
                props.put("format", "JSON");

                ConnectionParameters cp = new MockConnectionParameters(props, "http://127.0.0.1:8080");

                Resource resource = new StringResource("{\"item1\": \"one\"}");

                HTTPConnection httpConnection = new HTTPConnection(cp);
                httpConnection.executeScript(resource, parametersCallback);


            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(json("{\"item1\": \"one\"}")));

        syncTest("script Put test - String", new TestBean() {
            @Override
            public void run() throws Exception {

                Map<String, String> props = new HashMap<>();
                props.put("type", "PUT");

                ConnectionParameters cp = new MockConnectionParameters(props, "http://127.0.0.1:8080");

                Resource resource = new StringResource("abc=123\n" +
                        "def=456\n" +
                        "ghi=789");

                HTTPConnection httpConnection = new HTTPConnection(cp);
                httpConnection.executeScript(resource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(text("abc=123&def=456&ghi=789")));

        syncTest("script Put test - JSON", new TestBean() {
            @Override
            public void run() throws Exception {

                Map<String, String> props = new HashMap<>();
                props.put("type", "PUT");
                props.put("format", "JSON");

                ConnectionParameters cp = new MockConnectionParameters(props, "http://127.0.0.1:8080");

                Resource resource = new StringResource("{\"item1\": \"one\"}");

                HTTPConnection httpConnection = new HTTPConnection(cp);
                httpConnection.executeScript(resource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(json("{\"item1\": \"one\"}")));

        syncTest("script timeout test", new TestBean() {
            @Override
            public void run() throws Exception {

                Map<String, String> props = new HashMap<>();
                props.put("type", "POST");
                props.put("format", "JSON");
                props.put("timeout", "1000"); // Test that the timeout can be set.

                ConnectionParameters cp = new MockConnectionParameters(props, "http://127.0.0.1:8080");

                Resource resource = new StringResource("{\"item1\": \"one\"}");

                HTTPConnection httpConnection = new HTTPConnection(cp);
                httpConnection.executeScript(resource, parametersCallback);

                // Test that the default was not used.
                assertFalse(httpConnection.getTimeOut() == 500);
                //Test that the timeout value is as set.
                assertTrue(httpConnection.getTimeOut() == 1000);
            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(json("{\"item1\": \"one\"}")));
    }
}
