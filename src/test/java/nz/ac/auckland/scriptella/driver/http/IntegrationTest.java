package nz.ac.auckland.scriptella.driver.http;

import nz.ac.auckland.morc.TestBean;
import scriptella.AbstractTestCase;
import scriptella.execution.EtlExecutor;
import scriptella.execution.EtlExecutorException;

import nz.ac.auckland.morc.MorcTestBuilder;

import static nz.ac.auckland.morc.MorcTestBuilder.syncTest;


/**
 * @author Jack W Finlay - jfin404@aucklanduni.ac.nz
 *
 */
public class IntegrationTest extends AbstractTestCase{
    public void test() throws EtlExecutorException {

        new MorcTestBuilder() {
            public void configure() {
                syncTest("Integration test", new TestBean() {
                    @Override
                    public void run() throws Exception {
                        final EtlExecutor se = newEtlExecutor();
                        se.execute();
                    }
                }).addExpectation(syncExpectation("jetty:http://localhost:8080")
                        .expectedHeaders(headers(header("abc", "123"), header("def", "456"), header("ghi", "789"))))
                    .addExpectation(syncExpectation("jetty:http://localhost:8080")
                            .expectedHeaders(headers(header("abc", "jkl"), header("def", "mno"), header("ghi", "pqr"))))
                    .addExpectation(syncExpectation("jetty:http://localhost:8080")
                            .expectedHeaders(headers(header("abc", "stu"), header("def", "vwx"), header("ghi", "yz"))));}
        }.run();
    }
}
