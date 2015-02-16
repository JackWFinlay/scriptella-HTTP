package nz.ac.auckland.scriptella.driver.http;

/**
 * @Author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */

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
    public void testExecuteScript_GET() {

        System.out.println("Test1");
        httpConnection = new HTTPConnection("http://127.0.0.1:8080", "GET","String", 500);

        resource = new StringResource("abc=123\n" +
                "def=456\n" +
                "ghi=789");

        httpConnection.executeScript(resource, parametersCallback);

    }

    @Test
    public void testExecuteScript_POST() {

        System.out.println("Test2");
        httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", "String", 500);

        resource = new StringResource("abc=123\n" +
                "def=456\n" +
                "ghi=789");

        httpConnection.executeScript(resource, parametersCallback);
    }

    @Test
    public void testExecuteScript_POST_JSON() {

        System.out.println("Test3");
        httpConnection = new HTTPConnection("http://127.0.0.1:8080", "POST", "JSON", 500);

        resource = new StringResource("{\n" +
                "  \"item1\": \"one\",\n" +
                "  \"item2\": \"two\",\n" +
                "  \"item3\": \"three\"\n" +
                "}");

        httpConnection.executeScript(resource, parametersCallback);
    }

    @Test
    public void testExecuteScript_PUT() {

        System.out.println("Test4");
        httpConnection = new HTTPConnection("http://127.0.0.1:8080", "PUT", "String", 500);

        resource = new StringResource("abc=123\n" +
                "def=456\n" +
                "ghi=789");

        httpConnection.executeScript(resource, parametersCallback);
    }

    @Test
    public void testExecuteScript_PUT_JSON() {

        System.out.println("Test4");
        httpConnection = new HTTPConnection("http://127.0.0.1:8080", "PUT", "JSON", 500);

        resource = new StringResource("{\n" +
                "  \"item1\": \"one\",\n" +
                "  \"item2\": \"two\",\n" +
                "  \"item3\": \"three\"\n" +
                "}");

        httpConnection.executeScript(resource, parametersCallback);
    }
}