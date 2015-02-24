package nz.ac.auckland.scriptella.driver.http;

import org.apache.http.NameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scriptella.configuration.StringResource;
import scriptella.driver.script.ParametersCallbackMap;
import scriptella.spi.ParametersCallback;
import scriptella.spi.Resource;

import java.util.ArrayList;
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
        Resource resource = new StringResource("abc=$One\n" +
                "def=$Two\n" +
                "ghi=$Three");

        List<NameValuePair> result = httpConnection.generateParams(resource, parametersCallbackMap);

        assertTrue(result.get(0).getName().equals("abc"));
        assertTrue(result.get(0).getValue().equals("123"));

        assertTrue(result.get(1).getName().equals("def"));
        assertTrue(result.get(1).getValue().equals("456"));

        assertTrue(result.get(2).getName().equals("ghi"));
        assertTrue(result.get(2).getValue().equals("789"));

    }

    @Test
    public void testGetJSONString() {
        Resource jsonResource = new StringResource("{\n" +
                "    \"item1\": $One,\n" +
                "    \"item2\": $Two,\n" +
                "    \"item3\": $Three\n" +
                "}");

        List<String> list = httpConnection.getJsonStrings(jsonResource);

        assertTrue(list.contains("{"));
        assertTrue(list.contains("\"item1\": $One,"));
        assertTrue(list.contains("\"item2\": $Two,"));
        assertTrue(list.contains("\"item3\": $Three"));
        assertTrue(list.contains("}"));
        
    }

    @Test
    public void testParseJSON() {
        List<String> jsonStrings = new ArrayList<>();
        jsonStrings.add("{");
        jsonStrings.add("\"item1\": $One,");
        jsonStrings.add("\"item2\": $Two,");
        jsonStrings.add("\"item3\": $Three");
        jsonStrings.add("}");

        String result = httpConnection.parseJSON(jsonStrings, parametersCallbackMap);

        assertTrue(result.equals("{" +
                "\"item1\":\"123\"," +
                "\"item2\":\"456\"," +
                "\"item3\":\"789\"" +
                "}"));
        
    }

}
