package io.redsift;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class SandboxTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SandboxTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(SandboxTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws Exception {
        assertTrue(true);
    }
}
