package nz.ac.auckland.scriptella.driver.http;

/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */

import nz.ac.auckland.morc.MorcTestBuilder;
import nz.ac.auckland.morc.TestBean;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import scriptella.configuration.StringResource;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;

public class HTTPConnectionTestMorc extends MorcTestBuilder {

    public void configure(){

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

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "GET", 500);
                System.out.println("Test2");
                Resource resource = new StringResource("abc=123\n" +
                        "def=456\n" +
                        "ghi=789");

                httpConnection.executeScript(resource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"), header(Exchange.HTTP_QUERY, "abc=123&def=456&ghi=789"))).mockFeedPreprocessor(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                if (exchange.getIn().getBody() == null) exchange.getIn().setBody("");
            }
        }));

        syncTest("script POST test", new TestBean() {
            @Override
            public void run() throws Exception {

                HTTPConnection httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", 500);

                System.out.println("Test2");
                Resource resource = new StringResource("abc=123\n" +
                        "def=456\n" +
                        "ghi=789");

                httpConnection.executeScript(resource, parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"))).mockFeedPreprocessor(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                if (exchange.getIn().getBody() == null) exchange.getIn().setBody("");
            }
        }));

    }
}
