/*
* Copyright (c) 2005-2010, Virtusa Inc. (http://www.virtusa.com/) All Rights Reserved.
* 
* This file is part of the Virtusa Test Automation Framework project
* Virtusa Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* 
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
* 
*/


package com.thoughtworks.selenium;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.TestResult;
import java.sql.Connection;

/**
 * Provides a base class that implements some handy functionality for Selenium
 * testing (you are <i>not</i> required to extend this class).
 * 
 * <p>
 * This class adds a number of "verify" commands, which are like "assert"
 * commands, but they don't stop the test when they fail. Instead, verification
 * errors are all thrown at once during tearDown.
 * </p>
 * 
 * @author Nelson Sproul (nsproul@bea.com) Mar 13-06
 */
public class SeleneseTestBaseVir {

    private static final boolean THIS_IS_WINDOWS = File.pathSeparator
            .equals(";");

    private boolean captureScreenShotOnFailure = false;

    /** Use this object to run all of your selenium tests */
    public Selenium selenium;

    public WebDriver driver;
    public static String browserString;
    public static DesiredCapabilities WebDriverCapabilities;
    public Map<String, WebDriver> seleniumInstances =
            new HashMap<String, WebDriver>();
    public String seleniumInstanceName = "";
    public static FirefoxProfile defaultProfile;

    public static Logger log;
    public Map<String, Connection> databaseInstances =
            new HashMap<String, Connection>();

    protected StringBuffer verificationErrors = new StringBuffer();

    public SeleneseTestBaseVir() {
        super();
    }

    /**
     * Initialize the logger
     * */
    public void initLogger() {
        PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * Get the logger for each corresponding class
     * */
    public void getLogger(final Class clz) {
        log = Logger.getLogger(clz);
    }

    /**
     * Calls this.setUp(null)
     * 
     * @see #setUp(String)
     */
    public void setUp() throws Exception {
        this.setUp(null);
    }

    /**
     * Calls this.setUp with the specified url and a default browser. On
     * Windows, the default browser is *iexplore; otherwise, the default browser
     * is *firefox.
     * 
     * @see #setUp(String, String)
     * @param url
     *            the baseUrl to use for your Selenium tests
     * @throws Exception
     * 
     */
    public void setUp(final String browserString) throws Exception {

    }

    protected String runtimeBrowserString() {
        String defaultBrowser = System.getProperty("selenium.defaultBrowser");
        if (null != defaultBrowser && defaultBrowser.startsWith("${")) {
            defaultBrowser = null;
        }
        if (defaultBrowser == null) {
            if (THIS_IS_WINDOWS) {
                defaultBrowser = "*iexplore";
            } else {
                defaultBrowser = "*firefox";
            }
        }
        return defaultBrowser;
    }

    /**
     * Creates a new DefaultSelenium object and starts it using the specified
     * baseUrl and browser string. The port is selected as follows: if the
     * server package's RemoteControlConfiguration class is on the classpath,
     * that class' default port is used. Otherwise, if the "server.port" system
     * property is specified, that is used - failing that, the default of 4444
     * is used.
     * 
     * @see #setUp(String, String, int)
     * @param url
     *            the baseUrl for your tests
     * @param browserString
     *            the browser to use, e.g. *firefox
     * @throws Exception
     */
    public void setUp(final String url, final String browserString)
            throws Exception {
        System.out.println("child 2 setUp ....");

        setUp(url, browserString, getDefaultPort());
    }

    private int getDefaultPort() {
        try {
            Class c =
                    Class.forName("org.openqa.selenium.server.RemoteControlConfiguration");
            Method getDefaultPort = c.getMethod("getDefaultPort", new Class[0]);
            Integer portNumber =
                    (Integer) getDefaultPort.invoke(null, new Object[0]);
            return portNumber.intValue();
        } catch (Exception e) {
            return Integer.getInteger("selenium.port", 4444).intValue();
        }
    }

    public void setUp(String url, final String browserString, final int port) {
        if (url == null) {
            url = "http://localhost:" + port;
        }
        System.out.println("inside setUp");
        this.configWebDriver(browserString);
    }

    public void setUp(final String instanceName, final String browserString,
            final String serverConfig) {

        System.out.println("inside setUp");
        this.configWebDriver(browserString);
        seleniumInstanceName = instanceName;

        if (!serverConfig.isEmpty()) {
            String commandSet[] = serverConfig.split(",");

            for (String fullCommand : commandSet) {
                try {
                    String command = fullCommand.split("=")[0].toLowerCase();
                    String input = fullCommand.split("=")[1];

                    switch (command) {
                    case "firefoxprofile":
                        try {
                            defaultProfile =
                                    new FirefoxProfile(new File(input));
                        } catch (Exception e) {
                            System.err
                                    .println("Cannot find the specific firefox profile. Switching to the default profile.");
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void configWebDriver(final String browserString) {
        if (browserString.contains("chrome")
                || browserString.contains("Chrome")) {

            SeleneseTestBaseVir.browserString = browserString;
            File chromedriver =
                    new File("lib" + File.separator + "chromedriver.exe");
            System.setProperty("webdriver.chrome.driver",
                    chromedriver.getAbsolutePath());
            WebDriverCapabilities = new DesiredCapabilities();

        } else if (browserString.contains("safari")) {

            SeleneseTestBaseVir.browserString = browserString;
            WebDriverCapabilities = new DesiredCapabilities();

        } else if (browserString.contains("iexplore")) {

            SeleneseTestBaseVir.browserString = browserString;
            WebDriverCapabilities = new DesiredCapabilities();
            File iedriver;
            if (isx64bit()) {
                iedriver =
                        new File("lib" + File.separator
                                + "IEDriverServer(x64).exe");
            } else {
                iedriver =
                        new File("lib" + File.separator
                                + "IEDriverServer(x86).exe");
            }
            System.setProperty("webdriver.ie.driver",
                    iedriver.getAbsolutePath());

        } else if (browserString.contains("firefox")) {

            SeleneseTestBaseVir.browserString = browserString;
            WebDriverCapabilities = new DesiredCapabilities();
            defaultProfile = new FirefoxProfile();
        } else if (browserString.contains("opera")) {

            SeleneseTestBaseVir.browserString = browserString;
            WebDriverCapabilities = new DesiredCapabilities();
        } else {
            throw new AssertionError("Unsupported Browser");
        }
    }

    protected boolean isx64bit() {
        String architecture = System.getProperty("os.arch");
        if (architecture.contains("64")) {
            return true;
        } else {
            return false;
        }
    }

    /** Like assertTrue, but fails at the end of the test (during tearDown) */
    public void verifyTrue(final boolean b) {
        try {
            assertTrue(b);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Overloaded to handle custom error messages */
    public void verifyTrue(final String message, final boolean b) {
        try {
            ITestResult reult = new TestResult();
            reult = Reporter.getCurrentTestResult();
            reult.setStatus(ITestResult.SUCCESS);

            System.out.println(reult.getMethod() + "   " + reult.getClass());
            System.out.println(new Throwable(message).getStackTrace());
            reult.setThrowable(new Throwable(message));

            Reporter.setCurrentTestResult(reult);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like assertFalse, but fails at the end of the test (during tearDown) */
    public void verifyFalse(final boolean b) {
        try {
            assertFalse(b);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Returns the body text of the current page */
    public String getText() {
        return selenium.getEval("this.page().bodyText()");
    }

    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public void verifyEquals(final Object s1, final Object s2) {
        try {
            assertEquals(s1, s2);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public void verifyEquals(final boolean s1, final boolean s2) {
        try {
            assertEquals(new Boolean(s1), new Boolean(s2));
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like JUnit's Assert.assertEquals, but knows how to compare string arrays */
    public static void assertEquals(final Object s1, final Object s2) {
        if (s1 instanceof String && s2 instanceof String) {
            assertEquals((String) s1, (String) s2);
        } else if (s1 instanceof String && s2 instanceof String[]) {
            assertEquals((String) s1, (String[]) s2);
        } else if (s1 instanceof String && s2 instanceof Number) {
            assertEquals((String) s1, ((Number) s2).toString());
        } else {
            if (s1 instanceof String[] && s2 instanceof String[]) {

                String[] sa1 = (String[]) s1;
                String[] sa2 = (String[]) s2;
                if (sa1.length != sa2.length) {
                    throw new Error("Expected " + sa1 + " but saw " + sa2);
                }
                for (int j = 0; j < sa1.length; j++) {
                    assertEquals(sa1[j], sa2[j]);
                }
            }
        }
    }

    /**
     * Like JUnit's Assert.assertEquals, but handles "regexp:" strings like HTML
     * Selenese
     */
    public static void assertEquals(final String s1, final String s2) {
        assertTrue("Expected \"" + s1 + "\" but saw \"" + s2 + "\" instead",
                seleniumEquals(s1, s2));
    }

    /**
     * Like JUnit's Assert.assertEquals, but joins the string array with commas,
     * and handles "regexp:" strings like HTML Selenese
     */
    public static void assertEquals(final String s1, final String[] s2) {
        assertEquals(s1, join(s2, ','));
    }

    /**
     * Compares two strings, but handles "regexp:" strings like HTML Selenese
     * 
     * @param expectedPattern
     * @param actual
     * @return true if actual matches the expectedPattern, or false otherwise
     */
    public static boolean seleniumEquals(String expectedPattern, String actual) {
        if (actual.startsWith("regexp:") || actual.startsWith("regex:")
                || actual.startsWith("regexpi:")
                || actual.startsWith("regexi:")) {
            // swap 'em
            String tmp = actual;
            actual = expectedPattern;
            expectedPattern = tmp;
        }
        Boolean b;
        b = handleRegex("regexp:", expectedPattern, actual, 0);
        if (b != null) {
            return b.booleanValue();
        }
        b = handleRegex("regex:", expectedPattern, actual, 0);
        if (b != null) {
            return b.booleanValue();
        }
        b =
                handleRegex("regexpi:", expectedPattern, actual,
                        Pattern.CASE_INSENSITIVE);
        if (b != null) {
            return b.booleanValue();
        }
        b =
                handleRegex("regexi:", expectedPattern, actual,
                        Pattern.CASE_INSENSITIVE);
        if (b != null) {
            return b.booleanValue();
        }

        if (expectedPattern.startsWith("exact:")) {
            String expectedExact = expectedPattern.replaceFirst("exact:", "");
            if (!expectedExact.equals(actual)) {
                System.out.println("expected " + actual + " to match "
                        + expectedPattern);
                return false;
            }
            return true;
        }

        String expectedGlob = expectedPattern.replaceFirst("glob:", "");
        expectedGlob =
                expectedGlob.replaceAll("([\\]\\[\\\\{\\}$\\(\\)\\|\\^\\+.])",
                        "\\\\$1");

        expectedGlob = expectedGlob.replaceAll("\\*", ".*");
        expectedGlob = expectedGlob.replaceAll("\\?", ".");
        if (!Pattern.compile(expectedGlob, Pattern.DOTALL).matcher(actual)
                .matches()) {
            System.out.println("expected \"" + actual + "\" to match glob \""
                    + expectedPattern
                    + "\" (had transformed the glob into regexp \""
                    + expectedGlob + "\"");
            return false;
        }
        return true;
    }

    private static Boolean handleRegex(final String prefix,
            final String expectedPattern, final String actual, final int flags) {
        if (expectedPattern.startsWith(prefix)) {
            String expectedRegEx =
                    expectedPattern.replaceFirst(prefix, ".*") + ".*";
            Pattern p = Pattern.compile(expectedRegEx, flags);
            if (!p.matcher(actual).matches()) {
                System.out.println("expected " + actual + " to match regexp "
                        + expectedPattern);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Compares two objects, but handles "regexp:" strings like HTML Selenese
     * 
     * @see #seleniumEquals(String, String)
     * @return true if actual matches the expectedPattern, or false otherwise
     */
    public static boolean seleniumEquals(final Object expected,
            final Object actual) {
        if (expected instanceof String && actual instanceof String) {
            return seleniumEquals((String) expected, (String) actual);
        }
        return expected.equals(actual);
    }

    /** Asserts that two string arrays have identical string contents */
    public static void assertEquals(final String[] s1, final String[] s2) {
        String comparisonDumpIfNotEqual =
                verifyEqualsAndReturnComparisonDumpIfNot(s1, s2);
        if (comparisonDumpIfNotEqual != null) {
            throw new AssertionError(comparisonDumpIfNotEqual);
        }
    }

    /**
     * Asserts that two string arrays have identical string contents (fails at
     * the end of the test, during tearDown)
     */
    public void verifyEquals(final String[] s1, final String[] s2) {
        String comparisonDumpIfNotEqual =
                verifyEqualsAndReturnComparisonDumpIfNot(s1, s2);
        if (comparisonDumpIfNotEqual != null) {
            verificationErrors.append(comparisonDumpIfNotEqual);
        }
    }

    private static String verifyEqualsAndReturnComparisonDumpIfNot(
            final String[] s1, final String[] s2) {
        boolean misMatch = false;
        if (s1.length != s2.length) {
            misMatch = true;
        }
        for (int j = 0; j < s1.length; j++) {
            if (!seleniumEquals(s1[j], s2[j])) {
                misMatch = true;
                break;
            }
        }
        if (misMatch) {
            return "Expected " + stringArrayToString(s1) + " but saw "
                    + stringArrayToString(s2);
        }
        return null;
    }

    private static String stringArrayToString(final String[] sa) {
        StringBuffer sb = new StringBuffer("{");
        for (int j = 0; j < sa.length; j++) {
            sb.append(" ").append("\"").append(sa[j]).append("\"");
        }
        sb.append(" }");
        return sb.toString();
    }

    private static String throwableToString(final Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static String join(final String[] sa, final char c) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < sa.length; j++) {

            sb.append(sa[j]);
            if (j < sa.length - 1) {
                sb.append(c);
            }

        }
        return sb.toString();
    }

    /** Like assertNotEquals, but fails at the end of the test (during tearDown) */
    public void verifyNotEquals(final Object s1, final Object s2) {
        try {
            assertNotEquals(s1, s2);
        } catch (AssertionError e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like assertNotEquals, but fails at the end of the test (during tearDown) */
    public void verifyNotEquals(final boolean s1, final boolean s2) {
        try {
            assertNotEquals(new Boolean(s1), new Boolean(s2));
        } catch (AssertionError e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Asserts that two objects are not the same (compares using .equals()) */
    public static void assertNotEquals(final Object obj1, final Object obj2) {
        if (obj1.equals(obj2)) {
            failure("did not expect values to be equal (" + obj1.toString()
                    + ")");
        }
    }

    public static void failure(final Object message) {
        throw new AssertionError(message.toString());
    }

    static public void assertTrue(final String message, final boolean condition) {
        if (!condition) {
            failure(message);

            /*
             * ITestResult reult = new TestResult(); reult =
             * Reporter.getCurrentTestResult();
             * reult.setStatus(ITestResult.FAILURE);
             * 
             * System.out.println(reult.getMethod()+"   "+reult.getClass());
             * //System.out.println(new Throwable(message).getStackTrace());
             * //reult.setThrowable(new Throwable(message));
             * 
             * 
             * Reporter.setCurrentTestResult(reult);
             */

        }
    }

    static public void assertTrue(final boolean condition) {
        assertTrue(null, condition);
    }

    static public void assertFalse(final String message, final boolean condition) {
        assertTrue(message, !condition);
    }

    static public void assertFalse(final boolean condition) {
        assertTrue(null, !condition);
    }

    /** Asserts that two booleans are not the same */
    public static void assertNotEquals(final boolean b1, final boolean b2) {
        assertNotEquals(new Boolean(b1), new Boolean(b2));
    }

    /** Sleeps for the specified number of milliseconds */
    public void pause(final int millisecs) {
        try {
            Thread.sleep(millisecs);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Asserts that there were no verification errors during the current test,
     * failing immediately if any are found
     */
    public void checkForVerificationErrors() {
        String verificationErrorString = verificationErrors.toString();
        clearVerificationErrors();
        if (!"".equals(verificationErrorString)) {
            failure(verificationErrorString);
        }
    }

    /** Clears out the list of verification errors */
    public void clearVerificationErrors() {
        verificationErrors = new StringBuffer();
    }

    /** checks for verification errors and stops the browser */
    public void tearDown() throws Exception {
        try {
            checkForVerificationErrors();
        } finally {

        }
    }

    protected boolean isCaptureScreenShotOnFailure() {
        return captureScreenShotOnFailure;
    }

    protected void setCaptureScreenShotOnFailure(
            final boolean captureScreetShotOnFailure) {
        this.captureScreenShotOnFailure = captureScreetShotOnFailure;
    }
}
