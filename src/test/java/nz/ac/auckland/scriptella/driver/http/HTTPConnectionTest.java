package nz.ac.auckland.scriptella.driver.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scriptella.configuration.StringResource;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;

public class HTTPConnectionTest {

    Resource resource;
    HTTPConnection httpConnection;
    ParametersCallback parametersCallback;

    @Before
    public void setUp() throws Exception {

        parametersCallback = new ParametersCallback() {
            @Override
            public Object getParameter(String s) {
                return null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        httpConnection = null;
        resource = null;
    }

    @Test
    public void testExecuteScript_GET(){

        System.out.println("Test1");
        httpConnection = new HTTPConnection("http://127.0.0.1:8080", "GET", 500);

        resource = new StringResource("abc=123\n" +
                                    "def=456\n" +
                                    "ghi=789");

        httpConnection.executeScript(resource, parametersCallback);

    }

    @Test
    public void testExecuteScript_POST(){

        System.out.println("Test2");
        httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", 500);

        resource = new StringResource("abc=123\n" +
                                    "def=456\n" +
                                    "ghi=789");

        httpConnection.executeScript(resource,parametersCallback);
    }
}