package nz.ac.auckland.scriptella.driver.http;

import nz.ac.auckland.morc.MorcTestBuilder;
import nz.ac.auckland.morc.TestBean;
import scriptella.configuration.ConfigurationEl;
import scriptella.configuration.ConfigurationFactory;
import scriptella.execution.EtlExecutor;

import java.net.URL;


/**
 * @author Jack W Finlay - jfin404@aucklanduni.ac.nz
 */
public class IntegrationJSONTest extends MorcTestBuilder {

    public void configure() {

        syncTest("Integration test", new TestBean() {
            @Override
            public void run() throws Exception {
                final EtlExecutor se = newEtlExecutor();
                se.execute();
            }
        }).addExpectation(syncExpectation("jetty:http://localhost:8080")
                .expectedBody(json("{" +
                        "\"item1\": \"123\"," +
                        "\"item2\": \"456\"," +
                        "\"item3\": \"789\"" +
                        "}")))
                .addExpectation(syncExpectation("jetty:http://localhost:8080")
                        .expectedBody(json("{" +
                                "\"item1\": \"jkl\"," +
                                "\"item2\": \"mno\"," +
                                "\"item3\": \"pqr\"" +
                                "}")))
                .addExpectation(syncExpectation("jetty:http://localhost:8080")
                        .expectedBody(json("{" +
                                "\"item1\": \"stu\"," +
                                "\"item2\": \"vwx\"," +
                                "\"item3\": \"yz\"" +
                                "}")));

    }

    // Adapted from AbstractTestCase due to multiple-inheritance issues
    protected EtlExecutor newEtlExecutor() {
        return new EtlExecutor(loadConfiguration("IntegrationJSONTest.xml"));
    }

    // Adapted from AbstractTestCase due to multiple-inheritance issues
    protected ConfigurationEl loadConfiguration(final String path) {
        ConfigurationFactory cf = new ConfigurationFactory();
        final URL resource = this.getClass().getClassLoader().getResource(path);

        if (resource == null) {
            throw new IllegalStateException("Resource " + path + " not found");

        }

        cf.setResourceURL(resource);

        return cf.createConfiguration();


    }

}
