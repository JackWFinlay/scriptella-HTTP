/**
 * Created by Jack on 10/02/2015.
 */
import nz.ac.auckland.morc.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import scriptella.configuration.StringResource;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;

public class HTTPConnectionTestMorc extends MorcTestBuilder {

    public void configure(){

        final HTTPConnection httpConnection;
        final ParametersCallback parametersCallback;


        httpConnection = new HTTPConnection();
        httpConnection.setHOST("http://127.0.0.1:8080");
        httpConnection.setTIME_OUT(9999);
        parametersCallback = new ParametersCallback() {
            @Override
            public Object getParameter(String s) {
                return null;
            }
        };


        syncTest("script POST test",new TestBean() {
            @Override
            public void run() throws Exception {

                System.out.println("Test2");
                httpConnection.setTYPE("POST");

                Resource resource = new StringResource("abc=123\n" +
                                                        "def=456\n" +
                                                        "ghi=789");

                httpConnection.executeScript(resource,parametersCallback);

            }

        }).addExpectation(syncExpectation("jetty:http://localhost:8080").expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"))).mockFeedPreprocessor(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                if (exchange.getIn().getBody() == null) exchange.getIn().setBody("");
            }
        }));

    }
}
