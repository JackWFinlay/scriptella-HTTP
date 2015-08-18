package nz.ac.auckland.scriptella.driver.http;

import org.apache.http.NameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scriptella.configuration.StringResource;
import scriptella.driver.script.ParametersCallbackMap;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
public class HTTPConnectionTest {

    private HTTPConnection httpConnection;
    private ParametersCallback parametersCallback;
    ParametersCallbackMap parametersCallbackMap;

    @Before
    public void before() {
        httpConnection = new HTTPConnection();

        parametersCallback = new ParametersCallback() {
            @Override
            public Object getParameter(String s) {
                return null;
            }
        };

        parametersCallbackMap = new ParametersCallbackMap(parametersCallback);

        parametersCallbackMap.put("One", "123");
        parametersCallbackMap.put("Two", "456");
        parametersCallbackMap.put("Three", "789");
    }

    @After
    public void after() {
        httpConnection = new HTTPConnection();
        parametersCallback = null;
        parametersCallbackMap = null;
    }

    @Test
    public void testGenerateParams() {
        Resource resource = new StringResource(
                "abc=${One}\n" +
                        "def=${Two}\n" +
                        "ghi=${Three}");

        List<NameValuePair> result = httpConnection.generateParams(resource, parametersCallbackMap);

        assertTrue(result.get(0).getName().equals("abc"));
        assertTrue(result.get(0).getValue().equals("123"));

        assertTrue(result.get(1).getName().equals("def"));
        assertTrue(result.get(1).getValue().equals("456"));

        assertTrue(result.get(2).getName().equals("ghi"));
        assertTrue(result.get(2).getValue().equals("789"));

    }


    @Test
    public void testSetRequestType(){
        // Use this the test constructor to avoid null pointer errors.
        httpConnection = new HTTPConnection("http://127.0.0.1:8080/abc", "GET", "String", 500);
        assertTrue(httpConnection.getType().equals("GET"));
        
        httpConnection.setRequestType("Put");
        assertTrue(httpConnection.getHttpRequestBase().getMethod().equals("PUT"));

        httpConnection.setRequestType("Post");
        assertTrue(httpConnection.getHttpRequestBase().getMethod().equals("POST"));
        
    }
    
}
