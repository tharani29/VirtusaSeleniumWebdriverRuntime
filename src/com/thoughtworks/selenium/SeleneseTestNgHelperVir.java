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

import java.awt.AWTException;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import com.opera.core.systems.OperaDriver;
import com.virtusa.VTAF.reporter.reader.ReportBase;
import com.virtusa.isq.vtaf.report.reporter.Reporter;

public class SeleneseTestNgHelperVir extends SeleneseTestBaseVir {
    public static ReportBase reporter;
    private boolean runFromAnt = false;
    public static Robot robot;
    public static Properties prop;
    public static String propertiesLocation;

    public WebElement element;
    public String errorMessages = "Verification failures : \n";
    public String testPackageName = "";
    protected String currentMethod = "";
    protected String callingClassName = "";
    protected int lineNumber = 0;
    protected static int RETRY = 12;
    private static String TIMEOUT = "30000";
    public static Properties execProps;
    protected static String identifire = "";
    public List<String> openWindowHandleIndex;

    private static Reporter resultReporter;
    private Date commandStartTime;
    private Date testcaseStartTime;
    private long totalExecutionTimeTaken;

    @BeforeTest
    @Parameters({"selenium.url", "selenium.browser"})
    public void setUp(@Optional("http://www.google.com") final String url,
            @Optional("*iexplore") String browserString,
            final ITestContext context) throws Exception {
        try {

            if (super.browserString == null) {
                if (browserString == null || browserString.isEmpty()) {
                    browserString = runtimeBrowserString();
                }
            } else {
                browserString = super.browserString;
            }

            log.info("Browser : " + browserString);
            System.out.println("child setUp ....");
            log.info("Going in to the child setup ");

            super.setUp(url, browserString);

            System.out.println("going out attachScreenshotListener");

        } catch (Exception e) {
            log.error("Exception occured ", e);
        }
        super.setCaptureScreenShotOnFailure(true);
        cleanDriverServerSessions();
    };

    @BeforeSuite
    public void readRunProp() throws FileNotFoundException, IOException {

        initLogger();
        resultReporter = new Reporter();
        resultReporter.addNewTestExecution();
        getLogger(SeleneseTestNgHelperVir.class);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        prop = new Properties();
        String rootFile = new File("").getAbsolutePath();
        String file = "";
        if (rootFile.indexOf("grid") > -1) {
            file =
                    rootFile + File.separator + "grid" + File.separator
                            + "selenium-grid-1.0.6" + File.separator
                            + "project.properties";

        } else {
            file = rootFile + File.separator + "project.properties";
        }
        propertiesLocation = file;
        prop.load(new FileInputStream(new File(file)));
        runFromAnt = Boolean.parseBoolean(prop.getProperty("runFromAnt"));
        log.info("Propery file location : " + rootFile);
        log.info("Run from ANT : " + runFromAnt);
        this.readUserProp();
    }

    private void readUserProp() throws FileNotFoundException, IOException {

        execProps = new Properties();
        File file = new File("runtime.properties");

        boolean exists = file.exists();
        execProps.load(new FileInputStream("runtime.properties"));
        try {
            String timeOut = execProps.getProperty("TIMEOUT");
            String retry = execProps.getProperty("RETRY");
            String browser = execProps.getProperty("BROWSER");

            if (!browser.isEmpty()) {
                super.browserString = browser;
                System.setProperty("selenium.defaultBrowser", browserString);
            }
            if (!retry.isEmpty()) {
                RETRY = Integer.parseInt(retry);
            }
            if (!timeOut.isEmpty()) {
                TIMEOUT = timeOut;
            }
        } catch (Exception e) {
        }
        log.info("Retry Count : " + RETRY);
        log.info("Timeout Period : " + TIMEOUT);

    }

    @BeforeClass
    @Parameters({"selenium.restartSession"})
    public void getSelenium(@Optional("false") final boolean restartSession) {

        reporter = new ReportBase();
        resultReporter.addNewTestSuite(this.getClass().getSimpleName());

        System.out.println("Get Selenium " + selenium);

        if (restartSession) {
            System.out.println("in Selenium " + restartSession);

            driver.quit();
        }

    }

    @SuppressWarnings("static-access")
    @BeforeMethod
    public void setTestContext(final Method method) {

        String testComment = "";
        try {
            prop.setProperty("tcComment", testComment);
            prop.store(new FileOutputStream(propertiesLocation), null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        totalExecutionTimeTaken = 0;
        testcaseStartTime = getCurrentTime();

        errorMessages = "Verification failures : \n";
        identifire = "";
        openWindowHandleIndex = new ArrayList<String>();
        browserString = System.getProperty("selenium.defaultBrowser");
        this.cleanBrowserSessions();
        log.info("******************Starting the Selenium session******************");
        System.out
                .println("***********************************************starting the session ...");

        this.startBrowserSession(browserString);

        log.info("Started the selenium session.");

        reporter.startReporter(method.getDeclaringClass().getCanonicalName(),
                method.getName());
        log.info("Executing the test case : "
                + method.getDeclaringClass().getSimpleName() + "."
                + method.getName());

        getLogger(this.getClass());

        this.getClass().getPackage().toString();
        System.out.println(this.getClass().getPackage().toString());
        testPackageName =
                this.getClass().getPackage().toString().split("package ")[1];

        resultReporter.addNewTestCase(method.getName());

        startOfTestCase();

        log.info("############################# START OF TEST CASE #############################");
    }

    public void startBrowserSession(final String browserString) {

        if (browserString.contains("chrome")
                || browserString.contains("Chrome")) {
            driver = new ChromeDriver();

        } else if (browserString.contains("safari")) {

            WebDriverCapabilities.safari();
            driver = new SafariDriver(WebDriverCapabilities);

        } else if (browserString.contains("iexplore")) {

            WebDriverCapabilities.internetExplorer();
            WebDriverCapabilities.setJavascriptEnabled(true);
            driver = new InternetExplorerDriver(WebDriverCapabilities);

        } else if (browserString.contains("firefox")) {

            defaultProfile.setEnableNativeEvents(true);
            driver = new FirefoxDriver(defaultProfile);

        } else if (browserString.contains("opera")) {

            WebDriverCapabilities.opera();
            driver = new OperaDriver(WebDriverCapabilities);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new AssertionError("Unsupported Browser");
        }

        if (seleniumInstances.isEmpty()) {
            seleniumInstances.put("default", driver);
        } else {
            seleniumInstances.put(seleniumInstanceName, driver);
        }
    }

    private void cleanDriverServerSessions() {
        this.killBrowserProcess("chromedriver");
        if (isx64bit()) {
            this.killBrowserProcess("IEDriverServer(x64)");
        } else {
            this.killBrowserProcess("IEDriverServer(x86)");
        }
    }

    private void cleanBrowserSessions() {

        if (browserString.contains("iexplore")) {
            this.killBrowserProcess("iexplore");

        } else if (browserString.contains("chrome")
                || browserString.contains("Chrome")) {
            this.killBrowserProcess("chrome");

        } else if (browserString.contains("firefox")) {
            this.killBrowserProcess("firefox");

        } else if (browserString.contains("opera")) {
            this.killBrowserProcess("opera");

        } else if (browserString.contains("safari")) {
            this.killBrowserProcess("safari");
        }
    }

    @AfterMethod(alwaysRun = true)
    public void checkForVerificationError() {

        getLogger(SeleneseTestNgHelperVir.class);
        log.info("############################# END OF TEST CASE #############################");

        log.info("Total Time taken to execute the commands : "
                + totalExecutionTimeTaken + " ms");
        logTime("Total Time taken to execute the test case : ",
                testcaseStartTime, getCurrentTime());

        System.out
                .println("***********************************************Closing the session ...");

        for (String key : seleniumInstances.keySet()) {
            try {
                seleniumInstances.get(key).quit();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        for (String key : databaseInstances.keySet()) {
            try {
                databaseInstances.get(key).close();
            } catch (Exception e) {
                System.out
                        .println("\n\n\n Unable to close the connection.....\n\n\n");
                e.printStackTrace();

            }
        }

    }

    @AfterMethod(alwaysRun = true)
    public void cleanupSessions() {
        seleniumInstances = new HashMap<String, WebDriver>();
        databaseInstances = new HashMap<String, Connection>();
        endTestReporting(false);
        super.checkForVerificationErrors();
        this.cleanBrowserSessions();
    }

    @AfterSuite
    @Override
    public void tearDown() throws Exception {
        resultReporter.endTestReporting();
        super.tearDown();
        cleanDriverServerSessions();
    }

    /**
     * Kill the browser process.<br>
     * Specify the browser process to be killed.
     * 
     * @Parameters<br> process name which should be killed. <br>
     *                 Ex:<br>
     *                 If the process is firefox.exe parameter should be firefox
     */
    public void killBrowserProcess(final String process) {
        String processName = process + ".exe";

        try {
            if (isProcessRunning(processName)) {
                this.killProcess(processName);
                System.out.println("INFO : " + process
                        + " browser session cleaned successfully");
                log.info("INFO : " + process
                        + " browser session cleaned successfully");
                Thread.sleep(3000);
            }
        } catch (Exception ex) {
            System.out.println("INFO : " + process
                    + " browser session clean failed");
            log.error("INFO : " + process + " browser session clean failed", ex);
        }
    }

    private final String TASKLIST = "tasklist";
    private final String KILL = "taskkill /F /IM ";

    private boolean isProcessRunning(final String serviceName) throws Exception {
        Process p = Runtime.getRuntime().exec(TASKLIST);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {

            if (line.contains(serviceName)) {
                return true;
            }
        }
        return false;
    }

    private void killProcess(final String serviceName) throws Exception {
        Runtime.getRuntime().exec(KILL + serviceName);
    }

    // @Override static method of super class (which assumes JUnit conventions)
    public static void assertEquals(final Object actual, final Object expected) {
        SeleneseTestBase.assertEquals(expected, actual);
    }

    // @Override static method of super class (which assumes JUnit conventions)
    public static void assertEquals(final String actual, final String expected) {
        SeleneseTestBase.assertEquals(expected, actual);
    }

    // @Override static method of super class (which assumes JUnit conventions)
    public static void assertEquals(final String actual, final String[] expected) {
        SeleneseTestBase.assertEquals(expected, actual);
    }

    // @Override static method of super class (which assumes JUnit conventions)
    public static void assertEquals(final String[] actual,
            final String[] expected) {
        SeleneseTestBase.assertEquals(expected, actual);
    }

    // @Override static method of super class (which assumes JUnit conventions)
    public static boolean seleniumEquals(final Object actual,
            final Object expected) {
        return SeleneseTestBase.seleniumEquals(expected, actual);
    }

    // @Override static method of super class (which assumes JUnit conventions)
    public static boolean seleniumEquals(final String actual,
            final String expected) {
        return SeleneseTestBase.seleniumEquals(expected, actual);
    }

    @Override
    public void verifyEquals(final Object actual, final Object expected) {
        super.verifyEquals(expected, actual);
    }

    @Override
    public void verifyEquals(final String[] actual, final String[] expected) {
        super.verifyEquals(expected, actual);
    }

    public void startOfTestCase() {

        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            driver.manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reportresult(final boolean isAssert, final String step,
            final String result, String message) {

        StackTraceElement[] stackTraceElements =
                Thread.currentThread().getStackTrace();
        callingClassName =
                stackTraceElements[0].getClassName().toString().toString();
        String callingMethod = "";
        for (int i = 0; i < stackTraceElements.length; i++) {
            callingClassName = stackTraceElements[i].getClassName().toString();
            if (callingClassName.startsWith(testPackageName)) {
                callingMethod = stackTraceElements[i].getMethodName();
                lineNumber = stackTraceElements[i].getLineNumber();
                break;
            }
        }
        System.out.println(callingClassName);
        Class callingClass = null;
        try {
            callingClass = Class.forName(callingClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        getLogger(callingClass);
        if (!currentMethod.equals(callingMethod)) {
            log.info("Executing : " + callingClass.getName() + " : "
                    + callingMethod);
            currentMethod = callingMethod;
        }

        log.info("Step : " + step + "\t|\tResult : " + result
                + "\t|\tMessage : " + message);

        logTime(step, getCommandStartTime(), getCurrentTime());

        reporter.reportResult(step, result, message);

        try {

            String testStep = step.substring(0, step.indexOf(":"));
            message = replaceXMLSpecialCharacters(message);
            if (result.equals("PASSED")) {
                String testMessage = message;
                if (message.equals("") || message == null) {
                    testMessage = "Passed";
                }
                resultReporter.reportStepResults(true, testStep, testMessage,
                        "Success", "");
            } else {
                resultReporter.reportStepResults(false, testStep, message,
                        "Error",
                        getSourceLines(new Throwable(message).getStackTrace()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String replaceXMLSpecialCharacters(final String text) {
        String replaced = text;

        replaced = replaced.replaceAll("<", "&lt;");
        replaced = replaced.replaceAll(">", "&gt;");
        replaced = replaced.replaceAll("&", "&amp;");
        replaced = replaced.replaceAll("'", "&apos;");
        replaced = replaced.replaceAll("\"", "&quot;");
        replaced = replaced.replaceAll("»", "");
        return replaced;
    }

    private static String getSourceLines(final StackTraceElement[] StackTrace) {
        String lines = "";
        for (int elementid = 0; elementid < StackTrace.length; elementid++) {
            if (StackTrace[elementid].toString().indexOf("invoke0") != -1) {
                lines =
                        lines + StackTrace[elementid - 1] + "|"
                                + StackTrace[elementid - 2] + "|"
                                + StackTrace[elementid - 3];

            }

        }
        return lines;

    }

    public void endTestReporting(final boolean testFailed) {

        reporter.endResultReporting(testFailed);

    }

    public Date getCurrentTime() {
        Date time = Calendar.getInstance().getTime();
        return time;
    }

    public void setCommandStartTime(final Date startTime) {
        this.commandStartTime = startTime;
    }

    public Date getCommandStartTime() {
        return this.commandStartTime;
    }

    private void logTime(final String desc, final Date start, final Date end) {

        if (!desc.startsWith("PAUSE")) {
            Long timeDiff = Math.abs(end.getTime() - start.getTime());
            totalExecutionTimeTaken += timeDiff;
            log.info("Time taken to execute " + desc + " is : " + timeDiff
                    + " ms");
        }
    }

}
