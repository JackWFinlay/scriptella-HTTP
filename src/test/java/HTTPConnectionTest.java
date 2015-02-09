import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scriptella.configuration.StringResource;
import scriptella.driver.script.ParametersCallbackMap;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;

import static org.junit.Assert.*;

public class HTTPConnectionTest {

    Resource resource;
    HTTPConnection httpConnection;
    ParametersCallback parametersCallback;

    @Before
    public void setUp() throws Exception {
        httpConnection = new HTTPConnection();
        httpConnection.setHOST("http://127.0.0.1:8080");
        httpConnection.setTIME_OUT(9999);
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
        httpConnection.setTYPE("GET");

        resource = new StringResource("abc=123\n" +
                                    "def=456\n" +
                                    "ghi=789");

        httpConnection.executeScript(resource, parametersCallback);
    }

    @Test
    public void testExecuteScript_POST(){

        System.out.println("Test2");
        httpConnection.setTYPE("POST");

        resource = new StringResource("test", "testtest");

        httpConnection.executeScript(resource,parametersCallback);
    }
}