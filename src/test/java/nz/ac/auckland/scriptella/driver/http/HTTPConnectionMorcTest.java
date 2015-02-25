package nz.ac.auckland.scriptella.driver.http;

import nz.ac.auckland.morc.MorcTestBuilder;
import nz.ac.auckland.morc.TestBean;
import org.apache.camel.Exchange;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import scriptella.configuration.StringResource;
import scriptella.driver.script.ParametersCallbackMap;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;


/**
 * @author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
public class HTTPConnectionMorcTest extends MorcTestBuilder {

    public void configure() {

        ParametersCallback parametersCallback = new ParametersCallback() {
            @Override
            public Object getParameter(String s) {
                return null;
            }
        };

        ParametersCallbackMap parametersCallbackMap = new ParametersCallbackMap(parametersCallback);
        
        Resource stringResource = new StringResource("abc=123\n" +
                "def=456\n" +
                "ghi=789");

        Resource jsonResource = new StringResource("{\"item1\": \"one\"}");
        
        

        syncTest("executeGetRequestTest", new TestBean() {
            @Override
            public void run() throws Exception {

                
                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080/abc", "GET", "String", 500);


                httpConnection.executeGetRequest(stringResource, parametersCallbackMap);
            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080/abc").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"), header(Exchange.HTTP_URI, "/abc"))));

        syncTest("executeStringRequestPOSTTest", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", "String", 500);

                httpConnection.setRequestType("POST");
                httpConnection.executeStringRequest(stringResource, parametersCallbackMap);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"))));

        syncTest("executeStringRequestPUTTest", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "PUT", "String", 500);

                httpConnection.setRequestType("PUT");
                httpConnection.executeStringRequest(stringResource, parametersCallbackMap);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(text("abc=123&def=456&ghi=789")));

        syncTest("executeJsonPostTest", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", "JSON", 500);
                httpConnection.setRequestType("POST");
                httpConnection.executeJsonRequest(jsonResource, parametersCallbackMap);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(json("{\"item1\": \"one\"}")));

        syncTest("executeJsonPutTest", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "PUT", "JSON", 500);
                httpConnection.setRequestType("PUT");
                httpConnection.executeJsonRequest(jsonResource, parametersCallbackMap);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(json("{\"item1\": \"one\"}")));

        syncTest("script GET test", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080/abc", "GET", "String", 500);

                httpConnection.executeScript(stringResource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080/abc").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"), header(Exchange.HTTP_URI, "/abc"))));

        syncTest("script POST test", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", "String", 500);

                httpConnection.executeScript(stringResource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"))));

        syncTest("script POST test - JSON", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", "JSON", 500);

                httpConnection.executeScript(jsonResource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(json("{\"item1\": \"one\"}")));

        syncTest("script Put test", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "PUT", "String", 500);

                httpConnection.executeScript(stringResource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(text("abc=123&def=456&ghi=789")));

        syncTest("script Put test - JSON", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "PUT", "JSON", 500);


                httpConnection.executeScript(jsonResource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedBody(json("{\"item1\": \"one\"}")));
    }

    

}
