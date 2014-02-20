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

package com.virtusa.isq.vtaf.runtime;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.ImageTarget;
import org.sikuli.api.ScreenRegion;
import org.sikuli.api.Target;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.TestResult;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;
import com.thoughtworks.selenium.SeleneseTestBaseVir;
import com.thoughtworks.selenium.SeleneseTestNgHelperVir;

public class SeleniumTestBase extends SeleneseTestNgHelperVir {

    Clipboard clipboard;

    public static enum TableValidationType {
        COLCOUNT, ROWCOUNT, TABLEDATA, RELATIVE, TABLECELL
    };

    public static enum WindowValidationType {
        WINDOWPRESENT, CHECKTITLE
    }

    private static final int WAITTIME = 1000;
    private static final int RETRY_INTERVAL = 1000;
    private HashMap<String, DataTable> tables = null;
    private static String JS;
    private static String identifire = "";
    int failureCount = 0;
    private String notFoundOptions = "";

    String objectLocator = "";

    public JavascriptExecutor jsExecutor;

    // SeleniumWD
    // DB Connection
    private static String url1 = "";
    // Image recognition quality and rotation degree
    private static double maxRecQuality = 0.7;
    private static double minRecQuality = 0.5;
    private static int rotationDegree = 90;

    private String checkNullObject(final Object obj) {
        String value = null;
        try {
            value = obj.toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Opens an URL in the test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param objectName
     *            : Logical name of the object
     * @param waitTime
     *            : Time to wait for open command to complete
     * @param identifire
     *            : identifire is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifire</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * 
     * */
    public void open(final String objectName, final String identifire,
            final String waitTime) throws Exception {
        SeleniumTestBase.identifire = identifire;
        open(objectName, waitTime);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Opens an URL in the test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param objectName
     *            : url of the openning page
     * @param waitTime
     *            : time to wait till the page is loaded.
     * 
     * */
    public void open(final String objectName, final String waitTime) {
        String url = "";
        try {

            if (identifire != "") {
                url = ObjectMap.getResolvedSearchPath(objectName, identifire);
            } else {
                url = objectName;
            }
            setCommandStartTime(getCurrentTime());
            driver.get(url);

            try {
                driver.manage()
                        .timeouts()
                        .implicitlyWait(Integer.parseInt(waitTime),
                                TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }

            reportresult(true, "OPEN :" + url + "", "PASSED", url);
        } catch (Exception e) {
            e.printStackTrace();
            reportresult(true, "OPEN :" + url + "", "FAILED",
                    "OPEN command  :URL (" + url + ")  cannot access");
            checkTrue(false, true, "OPEN command  :URL (" + url
                    + ")  cannot access");
        }

    }

    /**
     * Opens an URL in a new test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param url
     *            : Logical name of the object
     * 
     * @param identifire
     *            : identifire is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifire</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * 
     * */

    public void navigateToURL(final String url, final String identifire,
            final String waitTime) throws Exception {

        SeleniumTestBase.identifire = identifire;
        navigateToURL(url, waitTime);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Opens an URL in a new test frame. This accepts both relative and absolute
     * URLs. The "open" command <br>
     * waits for the page to load before proceeding, ie. the "AndWait" suffix is
     * implicit. Note: The URL <br>
     * must be on the same domain as the runner HTML due to security
     * restrictions in the browser <br>
     * (Same Origin Policy). If you need to open an URL on another domain, use
     * the Selenium Server <br>
     * to start a new browser session on that domain.
     * 
     * @param url
     *            : url of the openning page
     * 
     **/
    public void navigateToURL(final String objectName, final String waitTime) {
        String url = "";
        try {

            if (identifire != "") {
                url = ObjectMap.getResolvedSearchPath(objectName, identifire);
            } else {
                url = objectName;
            }

            setCommandStartTime(getCurrentTime());
            if (url.toLowerCase().startsWith("openwindow=")) {

                Set<String> oldWindowHandles = driver.getWindowHandles();
                String URL = url.substring(url.indexOf('=') + 1, url.length());

                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.open('" + URL + "', '_newWindow');");
                super.pause(Integer.parseInt(waitTime));

                Set<String> newWindowHandles = driver.getWindowHandles();
                newWindowHandles.removeAll(oldWindowHandles);
                Object[] newWindowArr = newWindowHandles.toArray();
                driver.switchTo().window(newWindowArr[0].toString());

            } else {
                driver.get(url);
                super.pause(Integer.parseInt(waitTime));
            }
            // Commenting due to unwanted command since the waitFoPageLoad is
            // implicitly called

            reportresult(true, "NAVIGATETOURL Command:" + url + "", "PASSED",
                    url);
        } catch (Exception e) {
            reportresult(true, "NAVIGATETOURL :" + url + "", "FAILED",
                    "NAVIGATETOURL command  :URL (" + url + ")  cannot access");
            checkTrue(false, true, "NAVIGATETOURL command  :URL (" + url
                    + ")  cannot access");
        }

    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * ClickAt is capable of perform clicking on a relative location to the
     * specified element. use locator to specify the respective X,Y coordinates
     * to click
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param coordinates
     *            : X,Y coordinates of the position to be clicked with respect
     *            to the element (i.e. - 10,20)
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void clickAt(final String objectName, final String identifire,
            final String coordinateString) {

        SeleniumTestBase.identifire = identifire;
        clickAt(objectName, coordinateString);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * ClickAt is capable of perform clicking on a relative location to the
     * specified element. use locator to specify the respective X,Y coordinates
     * to click
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param coordinates
     *            : X,Y coordinates of the position to be clicked with respect
     *            to the element (i.e. - 10,20)
     * 
     * 
     * */

    public void clickAt(final String objectName, final String coordinateString) {
        String objectID = "";
        int counter = RETRY;
        int xOffset = 0;
        int yOffset = 0;
        try {
            // Retrieve the correct object locator from the object map
            objectID = ObjectMap.getObjectSearchPath(objectName, identifire);

            // first verify whether the element is present in the current web
            // page
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            try {
                xOffset =
                        Integer.parseInt((coordinateString.split(",")[0])
                                .trim());
                yOffset =
                        Integer.parseInt((coordinateString.split(",")[1])
                                .trim());
            } catch (Exception e) {

                e.printStackTrace();

                reportresult(true, "CLICKAT :" + objectName + "", "FAILED",
                        "CLICKAT coordinate string (" + coordinateString
                                + ") for :Element (" + objectName + ") ["
                                + objectID + "] is invalid");

                checkTrue(false, true, "CLICKAT coordinate string ("
                        + coordinateString + ") " + "for :Element ("
                        + objectName + ") [" + objectID + "] is invalid");
            }
            /*
             * START DESCRIPTION following while loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    // call for real selenium command
                    Actions clickAt = new Actions(driver);
                    clickAt.moveToElement(element, xOffset, yOffset).click();
                    clickAt.build().perform();
                    // if not exception is called consider and report the result
                    // as passed
                    reportresult(true, "CLICKAT :" + objectName + "", "PASSED",
                            "");
                    // if the testcase passed move out from the loop
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "CLICKAT :" + objectName + "",
                                "FAILED",
                                "CLICKAT command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "CLICKAT command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {
            e.printStackTrace();
            /*
             * VTAF result reporter call
             */
            reportresult(true, "CLICKAT :" + objectName + "", "FAILED",
                    "CLICKAT command  :Element (" + objectName + ") ["
                            + objectID + "] not present");

            /*
             * VTAF specific validation framework reporting
             */
            checkTrue(false, true, "CLICKAT command  :Element (" + objectName
                    + ") [" + objectID + "] not present");
        }

    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * 
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void click(final String objectName, final String identifire) {

        SeleniumTestBase.identifire = identifire;
        click(objectName);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Clicks on a link, button, checkbox or radio button. If the click action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad. <br>
     * 
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * 
     * 
     * 
     **/

    public void click(final String objectName) {
        String objectID = "";
        int counter = RETRY;
        try {
            // Retrieve the correct object locator from the object map
            objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
            // first verify whether the element is present in the current web
            // page
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following while loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exitted from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    // call for real selenium command
                    element.click();
                    // if not exception is called consider and report the result
                    // as passed
                    reportresult(true, "CLICK :" + objectName + "", "PASSED",
                            "");
                    // if the test case passed move out from the loop
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (ElementNotVisibleException ex) {
                    try {
                        jsExecutor = (JavascriptExecutor) driver;
                        jsExecutor.executeScript("arguments[0].click();",
                                element);
                        reportresult(true, "CLICK :" + objectName + "",
                                "PASSED", "");
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "CLICK :" + objectName + "",
                                "FAILED",
                                "CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {
            e.printStackTrace();
            /*
             * VTAF result reporter call
             */
            reportresult(true, "CLICK :" + objectName + "", "FAILED",
                    "CLICK command  :Element (" + objectName + ") [" + objectID
                            + "] not present");

            /*
             * VTAF specific validation framework reporting
             */
            checkTrue(false, true, "CLICK command  :Element (" + objectName
                    + ") [" + objectID + "] not present");
        }

    }

    /**
     * Stores a value in a given element property and return it as a string
     * value
     * 
     * @param objectName
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute> *
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public String getStringProperty(final String objectName,
            final String identifire, final String component) {

        SeleniumTestBase.identifire = identifire;
        String value = getStringProperty(objectName, component);
        SeleniumTestBase.identifire = "";
        return value;
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value
     * 
     * @param objectName
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute> *
     * 
     * 
     * */

    public String getStringProperty(final String objectName,
            final String component) {
        int counter = RETRY;
        String returnValue = "";
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            if (component.startsWith("TEXT:")) {

                if (component.split(":").length == 3) {
                    returnValue = element.getText();
                    if (component.split(":")[1].contains("-")) {
                        returnValue =
                                returnValue.substring(Integer
                                        .parseInt(component.split(":")[1]
                                                .split("-")[0]), Integer
                                        .parseInt(component.split(":")[1]
                                                .split("-")[1]));
                        reportresult(true, "SET VARIABLE PROPERTY :"
                                + objectName + "", "PASSED", "Object value = "
                                + returnValue);
                    } else {

                        returnValue =
                                returnValue.substring(Integer
                                        .parseInt(component.split(":")[1]));
                        reportresult(true, "SET VARIABLE PROPERTY :"
                                + objectName + "", "PASSED", "Object value = "
                                + returnValue);
                    }
                } else {
                    returnValue = element.getText();

                    reportresult(true, "SET VARIABLE PROPERTY :" + objectName
                            + "", "PASSED", "Object value = " + returnValue);
                }

            } else if (component.startsWith("VALUE:")) {

                returnValue = element.getAttribute("value");

                reportresult(true, "SET VARIABLE PROPERTY :" + objectName + "",
                        "PASSED", "Object value = " + returnValue);
            } else if (component.startsWith("ATTR:")) {

                /*
                 * START DESCRIPTION following for loop was added to make the
                 * command more consistent try the command for give amount of
                 * time (can be configured through class variable RETRY) command
                 * will be tried for "RETRY" amount of times or until command
                 * works. any exception thrown within the tries will be handled
                 * internally.
                 * 
                 * can be exited from the loop under 2 conditions 1. if the
                 * command succeeded 2. if the RETRY count is exceeded
                 */
                while (counter > 0) {
                    try {
                        counter--;

                        returnValue =
                                validateObjectProperty(objectID,
                                        component.substring(5), false);
                        reportresult(true, "SET VARIABLE PROPERTY :"
                                + objectName + "." + component.substring(5),
                                "PASSED", "Object value = " + returnValue);
                        break;
                    } catch (StaleElementReferenceException staleElementException) {

                        element = checkElementPresence(objectID);
                    } catch (Exception e) {
                        Thread.sleep(RETRY_INTERVAL);
                        /*
                         * after the retry amout, if still the object is not
                         * found, report the failure error will be based on the
                         * exception message, if e contains attribute report
                         * attribute failure else if e contains element, report
                         * object not found
                         */
                        if (!(counter > 0)) {
                            if (e.getMessage().equals("Attribute")) {
                                reportresult(true,
                                        "SET VARIABLE PROPERTY :" + objectName
                                                + "." + component.substring(5),
                                        "FAILED",
                                        " command setvarProperty()  :Atrribute ("
                                                + component.substring(5)
                                                + ")of  [" + objectName
                                                + "] not present");
                                checkTrue(false, true,
                                        " command setvarProperty()  :Atrribute ("
                                                + component.substring(5)
                                                + ")of  [" + objectName
                                                + "] not present");
                            }

                        }
                        if (e.getMessage().equals("Element")) {
                            reportresult(true,
                                    "SET VARIABLE PROPERTY :" + objectName
                                            + "." + component.substring(5),
                                    "FAILED",
                                    " command setvarProperty()  :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                            checkTrue(false, true,
                                    " command setvarProperty()  :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                        }
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amount, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            if (!(counter > 0)) {
                if (e.getMessage().equals("Attribute")) {
                    reportresult(true, "SET VARIABLE PROPERTY :" + objectName
                            + "." + component.substring(5), "FAILED",
                            " command setvarProperty()  :Atrribute ("
                                    + component.substring(5) + ")of  ["
                                    + objectName + "] not present");
                    checkTrue(false, true,
                            " command setvarProperty()  :Atrribute ("
                                    + component.substring(5) + ")of  ["
                                    + objectName + "] not present");
                }
            }
            if (e.getMessage().equals("Element")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + objectName + "."
                        + component.substring(5), "FAILED",
                        " command setvarProperty()  :Element (" + objectName
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, " command setvarProperty()  :Element ("
                        + objectName + ") [" + objectID + "] not present");
            }
        }
        return returnValue;
    }

    public void fail(final Object message) {
        reportresult(true, "Fail Command : ", "FAILED", " command Fail("
                + message + ")");
        SeleneseTestBaseVir.failure(message);
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value
     * 
     * @param objectName
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an element TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute> *
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusability of the locator.
     *            The usage can be defined using the following example <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public int getIntegerProperty(final String objectName,
            final String identifire, final String component) {

        SeleniumTestBase.identifire = identifire;
        int value = getIntegerProperty(objectName, component);
        SeleniumTestBase.identifire = "";
        return value;
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value
     * 
     * @param objectName
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute> *
     * 
     * 
     * */

    public int getIntegerProperty(final String objectName,
            final String component) {
        int counter = RETRY;
        String returnValue = "";
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            if (component.startsWith("TEXT:")) {

                if (component.split(":").length == 3) {
                    returnValue = element.getText();
                    if (component.split(":")[1].contains("-")) {
                        returnValue =
                                returnValue.substring(Integer
                                        .parseInt(component.split(":")[1]
                                                .split("-")[0]), Integer
                                        .parseInt(component.split(":")[1]
                                                .split("-")[1]));
                        reportresult(true, "SET VARIABLE PROPERTY :"
                                + objectName + "", "PASSED", "Object value = "
                                + returnValue);
                    } else {

                        returnValue =
                                returnValue.substring(Integer
                                        .parseInt(component.split(":")[1]));
                        reportresult(true, "SET VARIABLE PROPERTY :"
                                + objectName + "", "PASSED", "Object value = "
                                + returnValue);
                    }
                } else {
                    returnValue = selenium.getText(objectID);

                }

            } else if (component.startsWith("VALUE:")) {

                returnValue = element.getAttribute("value");
                reportresult(true, "SET VARIABLE PROPERTY :" + objectName + "",
                        "PASSED", "Object value = " + returnValue);
            } else if (component.startsWith("ATTR:")) {

                /*
                 * START DESCRIPTION following for loop was added to make the
                 * command more consistent try the command for give amount of
                 * time (can be configured through class variable RETRY) command
                 * will be tried for "RETRY" amount of times or until command
                 * works. any exception thrown within the tries will be handled
                 * internally.
                 * 
                 * can be exited from the loop under 2 conditions 1. if the
                 * command succeeded 2. if the RETRY count is exceeded
                 */
                while (counter > 0) {
                    try {
                        counter--;

                        returnValue =
                                validateObjectProperty(objectID,
                                        component.substring(5), false);
                        reportresult(true, "SET VARIABLE PROPERTY :"
                                + objectName + "." + component.substring(5),
                                "PASSED", "Object value = " + returnValue);
                        break;
                    } catch (StaleElementReferenceException staleElementException) {

                        element = checkElementPresence(objectID);
                    } catch (Exception e) {
                        Thread.sleep(RETRY_INTERVAL);
                        /*
                         * after the retry amout, if still the object is not
                         * found, report the failure error will be based on the
                         * exception message, if e contains attribute report
                         * attribute failure else if e contains element, report
                         * object not found
                         */
                        if (!(counter > 0)) {
                            if (e.getMessage().equals("Attribute")) {
                                reportresult(true,
                                        "SET VARIABLE PROPERTY :" + objectName
                                                + "." + component.substring(5),
                                        "FAILED",
                                        " command setVarProperty() :Atrribute ("
                                                + component.substring(5)
                                                + ")of  [" + objectName
                                                + "] not present");
                                checkTrue(false, true,
                                        " command setVarProperty() :Atrribute ("
                                                + component.substring(5)
                                                + ")of  [" + objectName
                                                + "] not present");
                            }
                        }

                        if (e.getMessage().equals("Element")) {
                            reportresult(true,
                                    "SET VARIABLE PROPERTY :" + objectName
                                            + "." + component.substring(5),
                                    "FAILED",
                                    " command setVarProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                            checkTrue(false, true,
                                    " command setVarProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                        }
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            if (!(counter > 0)) {
                if (e.getMessage().equals("Attribute")) {
                    reportresult(true, "SET VARIABLE PROPERTY :" + objectName
                            + "." + component.substring(5), "FAILED",
                            " command setVarProperty() :Atrribute ("
                                    + component.substring(5) + ")of  ["
                                    + objectName + "] not present");
                    checkTrue(false, true,
                            " command setVarProperty() :Atrribute ("
                                    + component.substring(5) + ")of  ["
                                    + objectName + "] not present");
                }
            }
            if (e.getMessage().equals("Element")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + objectName + "."
                        + component.substring(5), "FAILED",
                        " command setVarProperty()  :Element (" + objectName
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, " command setVarProperty()  :Element ("
                        + objectName + ") [" + objectID + "] not present");
            }
        }
        int returnval = 0;
        try {
            returnval = Integer.parseInt(returnValue);
        } catch (Exception e) {
            reportresult(true, "SET VARIABLE PROPERTY :" + objectName,
                    "FAILED",
                    " command setVarProperty() :input value mismatch with int, "
                            + "user input:" + returnValue);

            checkTrue(false, true,
                    " command setVarProperty() :input value mismatch with int ("
                            + "[" + objectID + "user input:" + returnValue);
        }

        return returnval;
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value
     * 
     * @param objectName
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute> *
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public boolean getBooleanProperty(final String objectName,
            final String identifire, final String component) {

        SeleniumTestBase.identifire = identifire;
        boolean value = getBooleanProperty(objectName, component);
        SeleniumTestBase.identifire = "";
        return value;
    }

    /**
     * Stores a value in a given element property and return it as a string
     * value
     * 
     * @param objectName
     *            logical name of the object
     * @param component
     *            Component specification string <br>
     *            Following is the way user needs to use this parameter<br>
     *            to store a text of an elemt TEXT: to verify a value of an
     *            element VALUE: to get a value of an attribute ATTR:<name of
     *            the attribute> *
     * 
     * 
     * */

    public boolean getBooleanProperty(String objectName, final String component) {
        int counter = RETRY;
        String returnValue = "";
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            if (component.startsWith("TEXT:")) {

                if (component.split(":").length == 3) {
                    returnValue = element.getText();
                    if (component.split(":")[1].contains("-")) {
                        returnValue =
                                returnValue.substring(Integer
                                        .parseInt(component.split(":")[1]
                                                .split("-")[0]), Integer
                                        .parseInt(component.split(":")[1]
                                                .split("-")[1]));
                        reportresult(true, "SET VARIABLE PROPERTY :"
                                + objectName + "", "PASSED", "Object value = "
                                + returnValue);
                    } else {

                        returnValue =
                                returnValue.substring(Integer
                                        .parseInt(component.split(":")[1]));
                    }
                } else {
                    returnValue = element.getText();
                }

            } else if (component.startsWith("VALUE:")) {
                returnValue = element.getAttribute("value");

            } else if (component.startsWith("ATTR:")) {

                /*
                 * START DESCRIPTION following for loop was added to make the
                 * command more consistent try the command for give amount of
                 * time (can be configured through class variable RETRY) command
                 * will be tried for "RETRY" amount of times or until command
                 * works. any exception thrown within the tries will be handled
                 * internally.
                 * 
                 * can be exited from the loop under 2 conditions 1. if the
                 * command succeeded 2. if the RETRY count is exceeded
                 */
                while (counter > 0) {
                    try {
                        counter--;

                        returnValue =
                                validateObjectProperty(objectID,
                                        component.substring(5), false);
                        objectName += objectName + "." + component.substring(5);

                        break;
                    } catch (StaleElementReferenceException staleElementException) {

                        element = checkElementPresence(objectID);
                    } catch (Exception e) {
                        Thread.sleep(RETRY_INTERVAL);
                        /*
                         * after the retry amout, if still the object is not
                         * found, report the failure error will be based on the
                         * exception message, if e contains attribute report
                         * attribute failure else if e contains element, report
                         * object not found
                         */
                        if (!(counter > 0)) {
                            if (e.getMessage().equals("Attribute")) {
                                reportresult(true,
                                        "SET VARIABLE PROPERTY :" + objectName
                                                + "." + component.substring(5),
                                        "FAILED",
                                        " command setVarProperty()  :Atrribute ("
                                                + component.substring(5)
                                                + ")of  [" + objectName
                                                + "] not present");
                                checkTrue(false, true,
                                        " command setVarProperty()  :Atrribute ("
                                                + component.substring(5)
                                                + ")of  [" + objectName
                                                + "] not present");
                            }
                        }
                        if (e.getMessage().equals("Element")) {
                            reportresult(true,
                                    "SET VARIABLE PROPERTY :" + objectName
                                            + "." + component.substring(5),
                                    "FAILED",
                                    " command setVarProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                            checkTrue(false, true,
                                    " command setVarProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                        }
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            if (!(counter > 0)) {
                if (e.getMessage().equals("Attribute")) {
                    reportresult(true, "SET VARIABLE PROPERTY :" + objectName
                            + "." + component.substring(5), "FAILED",
                            " command setVarProperty() :Atrribute ("
                                    + component.substring(5) + ")of  ["
                                    + objectName + "] not present");
                    checkTrue(false, true,
                            " command setVarProperty() :Atrribute ("
                                    + component.substring(5) + ")of  ["
                                    + objectName + "] not present");
                }
            }
            if (e.getMessage().equals("Element")) {
                reportresult(true, "SET VARIABLE PROPERTY :" + objectName + "."
                        + component.substring(5), "FAILED",
                        " command setVarProperty() :Element (" + objectName
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, " command setVarProperty() :Element ("
                        + objectName + ") [" + objectID + "] not present");
            }
        }

        if (returnValue.equalsIgnoreCase("true")
                || returnValue.equalsIgnoreCase("false")) {
            reportresult(true, "SET VARIABLE PROPERTY :" + objectName + "",
                    "PASSED", "Object value = " + returnValue);

        } else {
            reportresult(true, "CHECK VARIABLE PROPERTY :" + objectName,
                    "FAILED",
                    " command setVarProperty() :input value mismatch with boolean, "
                            + "user input:" + returnValue);

            checkTrue(false, true,
                    " command setVarProperty() :input value mismatch with boolean ("
                            + "[" + objectID + "user input:" + returnValue);
        }
        return Boolean.parseBoolean(returnValue);
    }

    /**
     * Arguments: <br>
     * <br>
     * 
     * windowID - the JavaScript window ID of the window to select<br>
     * 
     * Selects a popup window using a window locator; once a popup window has
     * been selected, all commands go to that window. To select the main window
     * again, use null as the target.<br>
     * <br>
     * 
     * Window locators provide different ways of specifying the window object:
     * by title, by internal JavaScript "name," or by JavaScript variable.<br>
     * <br>
     * 
     * title=My Special Window: Finds the window using the text that appears in
     * the title bar. Be careful; two windows can share the same title. If that
     * happens, this locator will just pick one.<br>
     * name=myWindow: Finds the window using its internal JavaScript "name"
     * property. This is the second parameter "windowName" passed to the
     * JavaScript method window.open(url, windowName, windowFeatures,
     * replaceFlag) (which Selenium intercepts).<br>
     * var=variableName: Some pop-up windows are unnamed (anonymous), but are
     * associated with a JavaScript variable name in the current application
     * window, e.g. "window.foo = window.open(url);". In those cases, you can
     * open the window using "var=foo".<br>
     * <br>
     * 
     * If no window locator prefix is provided, we'll try to guess what you mean
     * like this:<br>
     * <br>
     * 
     * 1.) if windowID is null, (or the string "null") then it is assumed the
     * user is referring to the original window instantiated by the browser).<br>
     * <br>
     * 
     * 2.) if the value of the "windowID" parameter is a JavaScript variable
     * name in the current application window, then it is assumed that this
     * variable contains the return value from a call to the JavaScript
     * window.open() method.<br>
     * <br>
     * 
     * 3.) Otherwise, selenium looks in a hash it maintains that maps string
     * names to window "names".<br>
     * <br>
     * 
     * 4.) If that fails, we'll try looping over all of the known windows to try
     * to find the appropriate "title". Since "title" is not necessarily unique,
     * this may have unexpected behavior.<br>
     * <br>
     * 
     * If you're having trouble figuring out the name of a window that you want
     * to manipulate, look at the Selenium log messages which identify the names
     * of windows created via window.open (and therefore intercepted by
     * Selenium). You will see messages like the following for each window as it
     * is opened:<br>
     * <br>
     * 
     * debug: window.open call intercepted; window ID (which you can use with
     * selectWindow()) is "myNewWindow"<br>
     * <br>
     * 
     * In some cases, Selenium will be unable to intercept a call to window.open
     * (if the call occurs during or before the "onLoad" event, for example).
     * (This is bug SEL-339.) In those cases, you can force Selenium to notice
     * the open window's name by using the Selenium openWindow command, using an
     * empty (blank) url, like this: openWindow("", "myFunnyWindow").<br>
     * <br>
     * 
     * @param windowName
     *            : Logical name of the window assigned by the test scriptor
     * 
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */

    public void selectWindow(final String windowName, final String identifire) {

        SeleniumTestBase.identifire = identifire;
        selectWindow(windowName);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Arguments: <br>
     * <br>
     * 
     * windowID - the JavaScript window ID of the window to select<br>
     * 
     * Selects a popup window using a window locator; once a popup window has
     * been selected, all commands go to that window. To select the main window
     * again, use null as the target.<br>
     * <br>
     * 
     * Window locators provide different ways of specifying the window object:
     * by title, by internal JavaScript "name," or by JavaScript variable.<br>
     * <br>
     * 
     * title=My Special Window: Finds the window using the text that appears in
     * the title bar. Be careful; two windows can share the same title. If that
     * happens, this locator will just pick one.<br>
     * name=myWindow: Finds the window using its internal JavaScript "name"
     * property. This is the second parameter "windowName" passed to the
     * JavaScript method window.open(url, windowName, windowFeatures,
     * replaceFlag) (which Selenium intercepts).<br>
     * var=variableName: Some pop-up windows are unnamed (anonymous), but are
     * associated with a JavaScript variable name in the current application
     * window, e.g. "window.foo = window.open(url);". In those cases, you can
     * open the window using "var=foo".<br>
     * <br>
     * 
     * If no window locator prefix is provided, we'll try to guess what you mean
     * like this:<br>
     * <br>
     * 
     * 1.) if windowID is null, (or the string "null") then it is assumed the
     * user is referring to the original window instantiated by the browser).<br>
     * <br>
     * 
     * 2.) if the value of the "windowID" parameter is a JavaScript variable
     * name in the current application window, then it is assumed that this
     * variable contains the return value from a call to the JavaScript
     * window.open() method.<br>
     * <br>
     * 
     * 3.) Otherwise, selenium looks in a hash it maintains that maps string
     * names to window "names".<br>
     * <br>
     * 
     * 4.) If that fails, we'll try looping over all of the known windows to try
     * to find the appropriate "title". Since "title" is not necessarily unique,
     * this may have unexpected behavior.<br>
     * <br>
     * 
     * If you're having trouble figuring out the name of a window that you want
     * to manipulate, look at the Selenium log messages which identify the names
     * of windows created via window.open (and therefore intercepted by
     * Selenium). You will see messages like the following for each window as it
     * is opened:<br>
     * <br>
     * 
     * debug: window.open call intercepted; window ID (which you can use with
     * selectWindow()) is "myNewWindow"<br>
     * <br>
     * 
     * In some cases, Selenium will be unable to intercept a call to window.open
     * (if the call occurs during or before the "onLoad" event, for example).
     * (This is bug SEL-339.) In those cases, you can force Selenium to notice
     * the open window's name by using the Selenium openWindow command, using an
     * empty (blank) url, like this: openWindow("", "myFunnyWindow").<br>
     * <br>
     * 
     * @param windowName
     *            : Logical name of the window assigned by the test scriptor
     * 
     * 
     * */
    public void selectWindow(final String windowName) {
        int counter = RETRY;
        boolean objectFound = false;
        String targetWindow = null;
        // String windowiden = "";

        // Getting the actual object identification from the object map
        String window = ObjectMap.getObjectSearchPath(windowName, identifire);
        try {
            checkForNewWindowPopups();
            Set<String> windowarr = getAllWindows();

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    if (window.startsWith("index=")) {
                        int winIndex =
                                Integer.parseInt(window.substring(
                                        window.indexOf("=") + 1,
                                        window.length()));
                        targetWindow = openWindowHandleIndex.get(winIndex);
                        objectFound = true;
                    } else {
                        for (String windowname : windowarr) {

                            if (window.startsWith("regexp:")
                                    || window.startsWith("glob:")) {

                                Pattern pattern =
                                        Pattern.compile(window.substring(
                                                window.indexOf(":") + 1,
                                                window.length()));
                                Matcher matcher =
                                        pattern.matcher(driver.switchTo()
                                                .window(windowname).getTitle());
                                if (matcher.matches()) {
                                    objectFound = true;
                                    targetWindow = windowname;
                                    break;
                                }
                            } else {
                                if (driver.switchTo().window(windowname)
                                        .getTitle().equals(window)) {
                                    objectFound = true;
                                    targetWindow = windowname;
                                    break;
                                }
                            }
                        }
                    }

                    if (objectFound) {

                        driver.switchTo().window(targetWindow);
                        try {
                            driver.manage().window().maximize();
                            jsExecutor = (JavascriptExecutor) driver;
                            jsExecutor.executeScript("window.focus();");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        reportresult(true, "SELECT WINDOW :" + windowName + "",
                                "PASSED", "");
                        break;
                    } else {
                        throw new Exception("Window Not Found");
                    }
                } catch (Exception ex) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        reportresult(true, "SELECT WINDOW :" + windowName + "",
                                "FAILED", "selectWindow command  :Element ("
                                        + windowName + ") [" + window
                                        + "] is not accessible");
                        checkTrue(false, true,
                                "selectWindow command  :Element (" + windowName
                                        + ") [" + window
                                        + "] is not accessible");
                    }
                }
            }
            if (!objectFound) {
                throw new Exception("Window Not Found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // if any exception is raised, report failure
            reportresult(true, "SELECT WINDOW :" + windowName + "", "FAILED",
                    "selectWindow command  :Element (" + windowName + ") ["
                            + window + "] not present");
            checkTrue(false, true, "selectWindow command  :Element ("
                    + windowName + ") [" + window + "] not present");

        }

    }

    /**
     * Sets the value of an input field, as though you typed it in.<br>
     * Can also be used to set the value of comboboxes, check boxes, etc. In
     * these cases, value should be the value of the option selected, not the
     * visible text.<br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param value
     *            : value to be typed in the object
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void type(final String objectName, final String identifire,
            final Object value) {
        SeleniumTestBase.identifire = identifire;
        type(objectName, value);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Sets the value of an input field, as though you typed it in.<br>
     * Can also be used to set the value of comboboxes, check boxes, etc. In
     * these cases, value should be the value of the option selected, not the
     * visible text.<br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param value
     *            : value to be typed in the object
     * 
     * */
    public void type(final String objectName, final Object objValue) {
        String value = checkNullObject(objValue);
        if (value == null) {
            reportresult(true, "TYPE :" + objectName + "", "FAILED",
                    "TYPE command: Invalid input. cannot use null as input");
            checkTrue(false, true,
                    "TYPE command: Invalid input. cannot use null as input");
            return;
        }
        int counter = RETRY;

        // Getting the actual object identification from the object map

        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            try {
                element.clear();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            while (counter > 0) {
                try {
                    counter--;
                    // Calling the actual command

                    element.sendKeys(value);

                    reportresult(true, "TYPE :" + objectName + "", "PASSED",
                            " [Input value = " + value + "]");
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "TYPE :" + objectName + "",
                                "FAILED",
                                "TYPE command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]"
                                        + " [Input value = " + value + "]");
                        checkTrue(false, true,
                                "TYPE command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]"
                                        + " [Input value = " + value + "]");
                    }
                }

            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // if any exception was raised, report a test failure
            e.printStackTrace();
            reportresult(true, "TYPE :" + objectName + "", "FAILED",
                    "TYPE command  :Element (" + objectName + ") [" + objectID
                            + "] [Input value = " + value + "] not present");
            checkTrue(false, true, "TYPE command  :Element (" + objectName
                    + ") [" + objectID + "] [Input value = " + value
                    + "] not present");

        }

    }

    /**
     * Drag and drop a given object to a specific loaction or on to another
     * object
     * 
     * @param sourceObject
     *            : Source object to be dragged, Specify the logical name
     * @param targetObject
     *            : Target object or the target location, to drag the object. <br>
     *            to specify the object : enter the logical name of the object.
     *            to specify the location : enter the relative x:y coordinates
     *            E.g. "+70,-300"
     * 
     * @param identifire
     *            : Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void dragAndDrop(final String sourceObject, final String identifire,
            final String targetObject) {
        SeleniumTestBase.identifire = identifire;
        dragAndDrop(sourceObject, targetObject);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Drag and drop a given object to a specific loaction or on to another
     * object
     * 
     * @param sourceObject
     *            : Source object to be dragged, Specify the logical name
     * @param targetObject
     *            : Target object or the target location, to drag the object. <br>
     *            to specify the object : enter the logical name of the object.
     *            to specify the location : enter the relative x:y coordinates
     *            E.g. "+70,-300"
     * 
     **/
    public void dragAndDrop(final String sourceObject, final String target) {
        int counter = RETRY;

        // Getting the actual object identification from the object map
        String sourceObjectID =
                ObjectMap.getObjectSearchPath(sourceObject, identifire);
        String targetObjectID = "";

        // Checks whether the target is a web element or a coordinate
        String initialvalue = target.split("\\,")[0].substring(1);
        boolean isComponent = false;
        try {
            int xcoord = Integer.parseInt(initialvalue);
        } catch (Exception ex) {
            isComponent = true;
        }

        // If target is an element, check the presence of the target, in the
        // page
        if (isComponent) {

            try {
                targetObjectID =
                        ObjectMap.getObjectSearchPath(target, identifire);
                checkElementPresence(targetObjectID);
            } catch (Exception ex) {
                reportresult(true, "DragandDrp :" + target + "", "FAILED",
                        "DRAGANDDROP command  : Target element (" + target
                                + ") [" + targetObjectID + "] not present");
                checkTrue(false, true, "DRAGANDDROP command  :Element ("
                        + target + ") [" + targetObjectID + "] not present");
                return;
            }
        }
        try {
            // Check whether the element present
            checkElementPresence(sourceObjectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    // if coordinates were supplied as target
                    if (isComponent) {
                        selenium.dragAndDropToObject(sourceObjectID,
                                targetObjectID);
                    } else {
                        selenium.dragAndDrop(sourceObjectID, target);

                    }
                    reportresult(true, "DRAGANDDROP :" + sourceObjectID + "",
                            "PASSED", "");
                    break;
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "DRAGANDDROP :" + sourceObject + "",
                                "FAILED",
                                "DRAGANDDROP command cannot access :Element ("
                                        + sourceObject + ") [" + sourceObjectID
                                        + "]");
                        checkTrue(false, true,
                                "DRAGANDDROP command cannot access :Element ("
                                        + sourceObject + ") [" + sourceObjectID
                                        + "]");
                    }
                }

            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // if any exception was raised, report a test failure
            e.printStackTrace();
            reportresult(true, "DRAGANDDROP :" + sourceObject + "", "FAILED",
                    "DRAGANDDROP command  :Element (" + sourceObject + ") ["
                            + sourceObjectID + "] not present");
            checkTrue(false, true, "DRAGANDDROP command  :Element ("
                    + sourceObject + ") [" + sourceObjectID + "] not present");

        }

    }

    /**
     * Select an option\options from a drop-down using an option locator. <br>
     * 
     * Option locators provide different ways of specifying options of an HTML
     * Select element (e.g. for selecting a specific option, or for asserting
     * that the selected option satisfies a specification). There are several
     * forms of Select Option Locator.<br>
     * <br>
     * 
     * <b>label=labelPattern:</b> matches options based on their labels, i.e.
     * the visible text. (This is the default.) label=regexp:^[Oo]ther<br>
     * <b>value=valuePattern:</b> matches options based on their values.
     * value=other<br>
     * <b>id=id:</b> matches options based on their ids. id=option1<br>
     * <b>index=index:</b> matches an option based on its index (offset from
     * zero). index=2<br>
     * <br>
     * 
     * If no option locator prefix is provided, the default behaviour is to
     * match on label. <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter <br>
     * <br>
     * @param value
     *            : value to be selected from the drpodown <br>
     *            If working with multi-select list box, to select multiple
     *            items <br>
     *            give the multiple values seperated by <b> # </b> symbol e.g. <br>
     * <br>
     * 
     *            for selecting single option : apple<br>
     *            for selecting multiple options : apple#orange#mango <br>
     * <br>
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is used to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://<p1>/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void select(final String objectName, final String identifire,
            final Object value) {
        SeleniumTestBase.identifire = identifire;
        select(objectName, value);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Select an option\options from a drop-down using an option locator. <br>
     * 
     * Option locators provide different ways of specifying options of an HTML
     * Select element (e.g. for selecting a specific option, or for asserting
     * that the selected option satisfies a specification). There are several
     * forms of Select Option Locator.<br>
     * <br>
     * 
     * <b>label=labelPattern:</b> matches options based on their labels, i.e.
     * the visible text. (This is the default.) label=regexp:^[Oo]ther<br>
     * <b>value=valuePattern:</b> matches options based on their values.
     * value=other<br>
     * <b>id=id:</b> matches options based on their ids. id=option1<br>
     * <b>index=index:</b> matches an option based on its index (offset from
     * zero). index=2<br>
     * <br>
     * 
     * If no option locator prefix is provided, the default behaviour is to
     * match on label. <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter <br>
     * <br>
     * @param value
     *            : value to be selected from the drpodown <br>
     *            If working with multi-select list box, to select multiple
     *            items <br>
     *            give the multiple values seperated by <b> # </b> symbol e.g. <br>
     * <br>
     * 
     *            for selecting single option : apple <br>
     *            for selecting multiple options : apple#orange#mango <br>
     * <br>
     * 
     * */
    public void select(final String objectName, final Object objValue) {

        String value = checkNullObject(objValue);
        if (value == null) {
            reportresult(true, "SELECT :" + objectName + "", "FAILED",
                    "SELECT command: Invalid input. cannot use null as input");
            checkTrue(false, true,
                    "SELECT command: Invalid input. cannot use null as input");
            return;
        }
        int counter = RETRY;
        String actualOptions[] = null;
        String valueStr = "";
        boolean multiSelect = false;
        int indexNo = 0;

        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Checking whether the list box is available
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            // Checking whether the list option is available
            Select selectElement = new Select(element);

            List<WebElement> actualElementOptions = selectElement.getOptions();

            actualOptions = new String[actualElementOptions.size()];
            for (int i = 0; i < actualElementOptions.size(); i++) {
                actualOptions[i] = actualElementOptions.get(i).getText();
            }
            multiSelect =
                    checkSelectOptions(objectID, value, selectElement,
                            actualOptions);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;

                    if (!multiSelect) {

                        if (value.startsWith("regexp:")) {

                            Pattern pattern =
                                    Pattern.compile(value.substring(
                                            value.indexOf(":") + 1,
                                            value.length()));
                            for (String actualOption : actualOptions) {
                                Matcher matcher = pattern.matcher(actualOption);
                                if (matcher.matches()) {
                                    valueStr = actualOption;
                                    break;
                                }
                            }
                            selectElement.selectByVisibleText(valueStr);

                        } else if (value.startsWith("index=")) {

                            indexNo =
                                    Integer.parseInt(value
                                            .replace("index=", ""));

                            selectElement.selectByIndex(indexNo);

                        } else {
                            valueStr = value;
                            selectElement.selectByVisibleText(valueStr);

                        }
                        reportresult(true, "SELECT :" + objectName + "",
                                "PASSED", "");
                        break;
                    }

                    else {
                        String options[] = value.split("#");
                        for (String option : options) {
                            if (option.startsWith("index=")) {

                                indexNo =
                                        Integer.parseInt(option.replace(
                                                "index=", ""));
                                selectElement.selectByIndex(indexNo);

                            } else {
                                selectElement.selectByVisibleText(option);

                            }
                            reportresult(true, "SELECT :" + objectName + "",
                                    "PASSED", "");
                            break;
                        }
                    }

                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception ex) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        ex.printStackTrace();
                        reportresult(true, "SELECT :" + objectName + "",
                                "FAILED",
                                "SELECT command cannot access :Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "SELECT command cannot access  :Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // waiting for the maximum amount of waiting time before failing the
            // test case
            // Several checks were introduced to narrow down to the failure to
            // the exact cause.
            if (!(counter > 0)) {
                e.printStackTrace();
                reportresult(true, "SELECT :" + objectName + "", "FAILED",
                        "SELECT command  :Element (" + objectName + ") ["
                                + objectID + "] not present");
                checkTrue(false, true, "SELECT command  :Element ("
                        + objectName + ") [" + objectID + "] not present");
            } else if (e.getMessage().equalsIgnoreCase("Option")) {
                e.printStackTrace();
                reportresult(true, "SELECT :" + objectName + "", "FAILED",
                        "SELECT command  :Element (" + objectName + ") ["
                                + objectID + "]: Selected Option  " + value
                                + " not present");
                checkTrue(false, true, "SELECT command  :Element ("
                        + objectName + ") [" + objectID + "]: Option '" + value
                        + "' not present");

            } else if (e.getMessage().equalsIgnoreCase("Element")) {
                e.printStackTrace();
                reportresult(true, "SELECT :" + objectName + "", "FAILED",
                        "SELECT command  :Element (" + objectName + ") ["
                                + objectID + "] not present");
                checkTrue(false, true, "SELECT command  :Element ("
                        + objectName + ") [" + objectID + "] not present");

            } else if (e.getMessage().equalsIgnoreCase("No_Item")) {
                reportresult(true, "SELECT :" + objectName + "", "FAILED",
                        "SELECT User item mismatch ( List Items:"
                                + Arrays.asList(actualOptions).toString()
                                + ", Input:" + objValue + ") ");
                checkTrue(false, true, "SELECT :" + objectName
                        + "- SELECT User item mismatch ( List Items:"
                        + Arrays.asList(actualOptions).toString() + ", Input:"
                        + objValue + ") ");

            } else if (e.getMessage().equalsIgnoreCase("Index Out of bound")) {
                reportresult(true, "SELECT :" + objectName + "", "FAILED",
                        "SELECT User input index is out of bound ( List Items:"
                                + Arrays.asList(actualOptions).toString()
                                + ", Input index:" + indexNo + ") ");
                checkTrue(
                        false,
                        true,
                        "SELECT :"
                                + objectName
                                + "- SELECT User input index is out of bound ( List Items:"
                                + Arrays.asList(actualOptions).toString()
                                + ", Input index:" + indexNo + ") ");

            }
        }

    }

    /**
     * Sleeps for the specified number of milliseconds
     * */
    public void pause(final String waitingTime) {
        long waitingMilliSeconds = Long.parseLong(waitingTime);
        try {
            Thread.sleep(waitingMilliSeconds);
            reportresult(true, "PAUSE Command: (" + waitingMilliSeconds
                    + " ms)", "PASSED", "Pausing for " + waitingTime
                    + " Milliseconds.");
        } catch (InterruptedException e) {
            reportresult(true, "PAUSE Command: ", "FAILED",
                    "Pause commad interrupted error : " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Double clicks on a link, button, checkbox or radio button. If the double
     * click action causes a new page to load <br>
     * (like a link usually does), call waitForPageToLoad. <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the object to be doubleclicked.
     * @param coordinates
     *            : X,Y coordinates of the position to be clicked with respect
     *            to the object
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void doubleClick(final String objectName, final String identifire) {
        SeleniumTestBase.identifire = identifire;
        doubleClick(objectName);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Double clicks on a link, button, checkbox or radio button. If the double
     * click action causes a new page to load <br>
     * (like a link usually does), call waitForPageToLoad. <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the object to be doubleclicked.
     * 
     * @param coordinates
     *            : X,Y coordinates of the position to be clicked with respect
     *            to the object
     * */

    public void doubleClick(final String objectName) {
        // Retrieve the actual object name from the object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        int counter = RETRY;
        try {
            // First chacking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;
                    element.click();
                    Actions dClick = new Actions(driver);
                    dClick.moveToElement(element).doubleClick();
                    dClick.build().perform();
                    reportresult(true, "DOUBLE CLICK :" + objectName + "",
                            "PASSED", "");
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "DOUBLE CLICK :" + objectName + "",
                                "FAILED",
                                "DOUBLE CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "DOUBLE CLICK command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // if object not found exception is raised fail the test cases
            e.printStackTrace();
            reportresult(true, "DOUBLE CLICK :" + objectName + "", "FAILED",
                    "DOUBLE CLICK command  :Element (" + objectName + ") ["
                            + objectID + "] not present");
            checkTrue(false, true, "DOUBLE CLICK command  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    /**
     * check(locator) <br>
     * Arguments:<br>
     * locator - an element locator<br>
     * Check a toggle-button (checkbox/radio)<br>
     * <br>
     * 
     * @param objectName
     *            : logical name of the object assigned by the user
     * @param isSelect
     *            : specify whether to check or uncheck the button
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void check(final String objectName, final String identifire,
            final boolean isSelect) {
        SeleniumTestBase.identifire = identifire;
        check(objectName, isSelect);
        SeleniumTestBase.identifire = "";
    }

    /**
     * check(locator) <br>
     * Arguments:<br>
     * locator - an element locator<br>
     * Check a toggle-button (checkbox/radio)<br>
     * <br>
     * 
     * @param objectName
     *            : logical name of the object assigned by the user
     * @param isSelect
     *            : specify whether to check or uncheck the button
     * 
     * */
    public void check(final String objectName, final boolean isSelect) {
        int counter = RETRY;
        String option = "";
        // Getting the actual object identification from the object map
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    if (isSelect) {
                        option = "Select";
                        // Calling the actual command
                        if (!element.isSelected()) {
                            element.click();
                        }
                        reportresult(true,
                                "CHECK (Select) :" + objectName + "", "PASSED",
                                "");
                    } else {
                        option = "DeSelect";
                        if (element.isSelected()) {
                            element.click();
                        }
                        reportresult(true, "CHECK (DeSelect) :" + objectName
                                + "", "PASSED", "");
                    }

                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "CHECK : (" + option + ")"
                                + objectName + "", "FAILED",
                                "CHECK command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]");
                        checkTrue(false, true,
                                "CHECK command cannot access :Element ("
                                        + objectName + ") [" + objectID + "]");
                    }
                }

            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // if any exception was raised, report a test failure
            e.printStackTrace();
            reportresult(true, "CHECK (" + option + "):" + objectName + "",
                    "FAILED", "CHECK (" + option + ") (" + objectName + ") ["
                            + objectID + "] element not present");
            checkTrue(false, true, "CHECK (" + option + ") (" + objectName
                    + ") [" + objectID + "] element not present");

        }

    }

    /**
     * Doubleclicks on a link, button, checkbox or radio button. If the action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad.<br>
     * The main differentiator of <b> DoubleClickAt </b> is, user can make the
     * script click on a relative location to the element <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the object provided by the user
     * @param locator
     *            : the coordination string to be click which is relative to the
     *            element <br>
     *            this should be specified using relative X and Y coordinates,
     *            in the following format "X,Y"
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusability of the locator.
     *            The usage can be defined using the following example <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */

    public void doubleClickAt(final String objectName, final String identifire,
            final String coordinates) {
        SeleniumTestBase.identifire = identifire;
        doubleClickAt(objectName, coordinates);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Doubleclicks on a link, button, checkbox or radio button. If the action
     * causes a new page to load (like a link usually does), call
     * waitForPageToLoad.<br>
     * The main differentiator of <b> DoubleClickAt </b> is, user can make the
     * script click on a relative location to the element <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the object provided by the user
     * @param locator
     *            : the coordination string to be click which is relative to the
     *            element <br>
     *            this should be specified using relative X and Y coordinates,
     *            in the following format "X,Y"
     */

    public void doubleClickAt(final String objectName,
            final String coordinateString) {
        int counter = RETRY;
        int xOffset = 0;
        int yOffset = 0;

        // Retrieve the actual object identification from the OR
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {

            // Precheck done to check whether the element is available if
            // element is not
            // present, code will move to the catch block and report an error
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            try {
                xOffset =
                        Integer.parseInt((coordinateString.split(",")[0])
                                .trim());
                yOffset =
                        Integer.parseInt((coordinateString.split(",")[1])
                                .trim());
            } catch (Exception e) {

                e.printStackTrace();

                reportresult(true, "DOUBLE CLICK AT :" + objectName + "",
                        "FAILED", "DOUBLE CLICK AT coordinate string ("
                                + coordinateString + ") for :Element ("
                                + objectName + ") [" + objectID
                                + "] is invalid");

                checkTrue(false, true, "DOUBLE CLICK AT coordinate string ("
                        + coordinateString + ") " + "for :Element ("
                        + objectName + ") [" + objectID + "] is invalid");
            }

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                counter--;
                try {

                    Actions doubleClickAt = new Actions(driver);
                    doubleClickAt.moveToElement(element, xOffset, yOffset)
                            .doubleClick();
                    doubleClickAt.build().perform();

                    reportresult(true, "DOUBLE CLICK AT :" + objectName + "",
                            "PASSED", "");
                    break;
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    // The main possibility of throwing exception at this point
                    // should be due to the element was not
                    // fully loaded, in this catch block handle the exception
                    // untill retry amount of attempts
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "DOUBLE CLICK AT :" + objectName
                                + "", "FAILED",
                                "DOUBLE CLICK AT command  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                        checkTrue(false, true,
                                "DOUBLE CLICK AT command  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                    }
                }
            }

            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {
            // if any exception was raised report report an error and fail the
            // test case
            if (!(counter > 0)) {
                e.printStackTrace();
                reportresult(true, "DOUBLE CLICK AT :" + objectName + "",
                        "FAILED", "DOUBLE CLICK AT command  :Element ("
                                + objectName + ") [" + objectID
                                + "] not present");
                checkTrue(false, true, "DOUBLE CLICK AT command  :Element ("
                        + objectName + ") [" + objectID + "] not present");
            }
        }

    }

    /**
     * checks whether the object referred by the logical id exists.<br>
     * if the object is not exists, further continuation of the script execution
     * will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * 
     * @param objectName
     *            : logical name of the object provided by the user
     * @param stopExecution
     *            : if <b> true </b> : stop the execution after the failure <br>
     *            if <b> false </b>: Continue the execution after the failure
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusability of the locator.
     *            The usage can be defined using the following example <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void checkElementPresent(final String objectName,
            final String identifire, final boolean stopExecution) {
        SeleniumTestBase.identifire = identifire;
        checkElementPresent(objectName, stopExecution);
        SeleniumTestBase.identifire = "";
    }

    /**
     * checks whether the object referred by the logical id exists.<br>
     * if the object is not exists, further continuation of the script execution
     * will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * 
     * @param objectName
     *            : logical name of the object provided by the user
     * @param stopExecution
     *            : if <b> true </b> : stop the execution after the failure <br>
     *            if <b> false </b>: Continue the execution after the failure
     */
    public void checkElementPresent(final String objectName,
            final boolean stopExecution) {
        // Retrieve the actual object id from the OR
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Check whether the element is present, the validation
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            // if reached this point, test case should be passed
            reportresult(true, "CHECK ELEMENT PRESENT :" + objectName + "",
                    "PASSED", "");
        } catch (Exception e) {
            // if any exception was thrown the faliure should be reported
            // but the continuation will be decided by stpoExecution
            e.printStackTrace();
            reportresult(stopExecution, "CHECK ELEMENT PRESENT :" + objectName
                    + "", "FAILED",
                    " command checkElementPresent()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
            checkTrue(false, stopExecution,
                    " command checkElementPresent()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
        }
    }

    /**
     * checks whether the object referred by the logical id exists.<br>
     * Returs true or false based on the availability of the element. <br>
     * 
     * @param objectName
     *            : logical name of the object provided by the user
     */

    public boolean checkElementPresent(final String objectName) {
        // Retrieve the actual object id from the OR
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        if (objectID.isEmpty()) {
            objectID = objectName;
        }
        boolean isElementPresent = false;
        try {
            // Check whether the element is present, the validation
            element = objectLocator(objectID);
            isElementPresent = true;
            return isElementPresent;

        } catch (Exception e) {
            return isElementPresent;
        }
    }

    /**
     * checks whether the Text referred by the logical id exists.<br>
     * if the text does not exists, further continuation of the script execution
     * will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * 
     * @param objectName
     *            : object name alias given by the user.
     * @param stopOnFailure
     *            : if <b> true </b> : stop the execution after the failure <br>
     *            if <b> false </b>: Continue the execution after the failure
     * 
     * 
     * */
    public void checkTextPresent(final Object objSearchText,
            final boolean stopOnFailure) {

        String searchText = checkNullObject(objSearchText);
        if (searchText == null) {
            reportresult(stopOnFailure, "CHECK TEXT PRESENT :" + searchText
                    + "", "FAILED",
                    "CheckTextPresent command: Invalid input. cannot use null as input");
            checkTrue(false, stopOnFailure,
                    "CheckTextPresent command: Invalid input. cannot use null as input");
            return;
        }
        int counter = RETRY;
        // retrieves the objectid from the object repository
        String objectID = ObjectMap.getObjectSearchPath(searchText, identifire);

        // if, in case, the user has given the search text itself instead of
        // objectid this
        // code segment acts as a contingency

        if (objectID.equalsIgnoreCase("")) {
            objectID = searchText;
        }
        /*
         * START DESCRIPTION following for loop was added to make the command
         * more consistent try the command for give amount of time (can be
         * configured through class variable RETRY) command will be tried for
         * "RETRY" amount of times or until command works. any exception thrown
         * within the tries will be handled internally.
         * 
         * can be exited from the loop under 2 conditions 1. if the command
         * succeeded 2. if the RETRY count is exceeded
         */
        while (counter > 0) {

            boolean objectFound = false;
            try {
                counter--;

                objectFound = driver.getPageSource().contains(objectID);
                if (objectFound) {
                    reportresult(true,
                            "CHECK TEXT PRESENT :" + searchText + "", "PASSED",
                            "");
                    break;
                }
                Thread.sleep(RETRY_INTERVAL);

                if ((!(counter > 0)) && (objectFound == false)) {
                    // if the retry count has exceeded and still the text is not
                    // present,
                    // report a test failure
                    reportresult(stopOnFailure, "CHECK TEXT PRESENT :"
                            + searchText + "", "FAILED",
                            " command checkTextPresent()  :Text (" + searchText
                                    + ") [" + objectID + "] not present");
                    checkTrue(false, stopOnFailure,
                            " command checkTextPresent()  :Text (" + searchText
                                    + ") [" + objectID + "] not present");
                    break;
                }
                /*
                 * END DESCRIPTION
                 */
            } catch (Exception e) {

                if (!(counter > 0)) {
                    // if the retry count has exceeded and still the text is not
                    // present,
                    // report a test failure
                    e.printStackTrace();
                    reportresult(stopOnFailure, "CHECK TEXT PRESENT :"
                            + searchText + "", "FAILED",
                            " command checkTextPresent()  :Text (" + searchText
                                    + ") [" + objectID + "] not present");
                    checkTrue(false, stopOnFailure,
                            " command checkTextPresent()  :Text (" + searchText
                                    + ") [" + objectID + "] not present");
                }
            }
        }

    }

    /**
     * Checks whether the Object property given by the property name is exists
     * if the property does not exists, further continuation of the script
     * execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page
     * 
     * @param objectName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param expectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     * 
     * @param Identifire
     *            :
     * 
     *            Identifier is us to increase the reusability of the locator.
     *            The usage can be defined using the following example <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * 
     * @throws Exception
     * */
    public void checkObjectProperty(final String objectName,
            final String identifire, final String propertyname,
            final Object expectedvale, final boolean stopOnFailure) {
        SeleniumTestBase.identifire = identifire;
        checkObjectProperty(objectName, propertyname, expectedvale,
                stopOnFailure);
        SeleniumTestBase.identifire = "";
    }

    public static enum ObjectValidationType {
        ALLOPTIONS, SELECTEDOPTION, MISSINGOPTION, ELEMENTPRESENT, PROPERTYPRESENT
    };

    /**
     * Checks whether the Object property given by the property name is exists
     * if the property does not exists, further continuation of the script
     * execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page
     * 
     * @param objectName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param expectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     */

    public void checkObjectProperty(final String objectName,
            final String propertyname, final Object objExpectedvale,
            final boolean stopOnFailure) {

        String expectedvale = checkNullObject(objExpectedvale);
        if (expectedvale == null) {
            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName,
                    "FAILED",
                    "CheckObjectProperty command: Invalid input. cannot use null as input");
            checkTrue(false, stopOnFailure,
                    "CheckObjectProperty command: Invalid input. cannot use null as input");
            return;
        }
        // Call the relavant internal method based on the
        // TableValidationType provided by the user
        if (propertyname.equals(ObjectValidationType.ALLOPTIONS.toString())) {

            checkAllSelectOptions(objectName, propertyname, expectedvale,
                    stopOnFailure);
        } else if (propertyname.equals(ObjectValidationType.SELECTEDOPTION
                .toString())) {

            checkSelectedOption(objectName, propertyname, expectedvale,
                    stopOnFailure);
        } else if (propertyname.equals(ObjectValidationType.MISSINGOPTION
                .toString())) {

            checkMissingOption(objectName, propertyname, expectedvale,
                    stopOnFailure);

        } else if (propertyname.equals(ObjectValidationType.ELEMENTPRESENT
                .toString())) {

            checkElementNotPresent(objectName, propertyname, expectedvale,
                    stopOnFailure);
        } else if (propertyname.equals(ObjectValidationType.PROPERTYPRESENT
                .toString())) {

            checkPropertyPresent(objectName, propertyname, expectedvale,
                    stopOnFailure);
        } else {

            checkObjectOtherProperty(objectName, propertyname, expectedvale,
                    stopOnFailure);
        }
    }

    /**
     * Check if a property value is present or not in an element
     */
    private void checkPropertyPresent(final String objectName,
            final String property, final String expectedvale,
            final boolean stopOnFailure) {

        int counter = RETRY;
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);

        String propertyName = "";
        String condition = "";

        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            try {
                String[] commandSet = expectedvale.split("\\|");
                propertyName = commandSet[0];
                condition = commandSet[1];

            } catch (Exception ex) {
                throw new Exception("Input");
            }
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;
                    String isAttributePresent = "";
                    if (propertyName.equalsIgnoreCase("textContent")) {

                        if (element.getText().equals("")
                                || element.getText() == null) {
                            isAttributePresent = "false";
                        } else {
                            isAttributePresent = "true";
                        }
                    } else {
                        if (element.getAttribute(propertyName.toUpperCase()) != null) {
                            isAttributePresent = "true";
                        } else {
                            isAttributePresent = "false";
                        }
                    }
                    if (isAttributePresent.equalsIgnoreCase(condition.trim())) {
                        reportresult(true, "CHECK OBJECT PROPERTY PRESENT :"
                                + objectName + "." + property, "PASSED",
                                "Input Value = " + expectedvale);

                        break;
                    } else {
                        reportresult(
                                stopOnFailure,
                                "CHECK OBJECT PROPERTY :" + objectName + "."
                                        + property,
                                "FAILED",
                                " command checkObjectProperty : "
                                        + objectName
                                        + " OBJECT PROPERTY PRESENT : ["
                                        + property
                                        + "] condition is different from the expected. Actual : "
                                        + isAttributePresent
                                        + " Expected condition : " + condition
                                        + "");
                        checkTrue(
                                false,
                                stopOnFailure,
                                " command checkObjectProperty : "
                                        + objectName
                                        + " OBJECT PROPERTY PRESENT : ["
                                        + property
                                        + "] condition is different from the expected. Actual : "
                                        + isAttributePresent
                                        + " Expected condition : " + condition
                                        + "");
                        break;
                    }

                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + property, "FAILED",
                                " command checkObjectProperty :Element : ["
                                        + objectName + "] is not accessible");
                        checkTrue(false, stopOnFailure,
                                " command checkObjectProperty :Element : ["
                                        + property + "] is not accessible");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            if (e.getMessage().equals("Input")) {
                reportresult(
                        stopOnFailure,
                        "CHECK OBJECT PROPERTY :" + objectName + "." + property,
                        "FAILED",
                        " command checkObjectProperty : User inputs ["
                                + expectedvale
                                + "] are not in the correct format. Correct format: attributeName|condition");
                checkTrue(
                        false,
                        stopOnFailure,
                        " command checkObjectProperty : User inputs ["
                                + expectedvale
                                + "] are not in the correct format. Correct format: attributeName|condition");
            } else {
                reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                        + objectName + "." + property, "FAILED",
                        " command checkObjectProperty :Element : ["
                                + objectName + "] is not present");
                checkTrue(false, stopOnFailure,
                        " command checkObjectProperty :Element : ["
                                + objectName + "] is not present");
            }
        }
    }

    /**
     * Check if a option is not displaying in the webpage
     */
    private void checkMissingOption(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure) {

        int counter = RETRY;
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            Select selectElement = new Select(element);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;
                    List<WebElement> elementOptions =
                            selectElement.getOptions();
                    String[] selectOptions = new String[elementOptions.size()];
                    for (int i = 0; i < elementOptions.size(); i++) {
                        selectOptions[i] = elementOptions.get(i).getText();
                    }
                    for (String option : selectOptions) {

                        if (option.equals(expectedvale)) {

                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    " command checkObjectProperty "
                                            + propertyname + ": OPTION :"
                                            + expectedvale + "  : of object ["
                                            + objectName + "] is present");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty "
                                            + propertyname + ": OPTION :"
                                            + expectedvale + "  : of object ["
                                            + objectName + "] is present");
                            break;
                        }
                    }
                    reportresult(true, "CHECK OBJECT PROPERTY :" + objectName
                            + "." + propertyname, "PASSED", "Input Value = "
                            + expectedvale);
                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "FAILED",
                                " command checkObjectProperty :Element : ["
                                        + objectName + "] is not accessible");
                        checkTrue(false, stopOnFailure,
                                " command checkObjectProperty :Element : ["
                                        + objectName + "] is not accessible");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName
                    + "." + propertyname, "FAILED",
                    " command checkObjectProperty :Element : [" + objectName
                            + "] is not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty :Element : [" + objectName
                            + "] is not present");
        }
    }

    /**
     * Check if a element is displaying or not in the webpage
     */

    public void checkElementNotPresent(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure) {

        int counter = RETRY;
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);

        String isObjectFound = "false";
        /*
         * START DESCRIPTION following for loop was added to make the command
         * more consistent try the command for give amount of time (can be
         * configured through class variable RETRY) command will be tried for
         * "RETRY" amount of times or until command works. any exception thrown
         * within the tries will be handled internally.
         * 
         * can be exited from the loop under 2 conditions 1. if the command
         * succeeded 2. if the RETRY count is exceeded
         */
        while (counter > 0) {

            WebElement webElement = null;
            try {

                counter--;
                try {
                    webElement = objectLocator(objectID);
                    isObjectFound = "True";
                } catch (Exception ex) {

                }
                if (isObjectFound.equalsIgnoreCase(expectedvale)) {
                    reportresult(true, "CHECK OBJECT PROPERTY :" + objectName
                            + ".ELEMENTPRESENT", "PASSED", "");
                    break;
                } else {
                    if (counter < 1) {
                        throw new Exception("Element");
                    }
                }

            } catch (Exception e) {
                try {
                    Thread.sleep(RETRY_INTERVAL);
                } catch (InterruptedException e1) {
                }

                if (e.getMessage().contains("Element")) {
                    reportresult(
                            stopOnFailure,
                            "CHECK OBJECT PROPERTY: " + objectName
                                    + ".ELEMENTPRESENT",
                            "FAILED",
                            " command checkObjectProperty :Element : ["
                                    + objectName
                                    + "] [ELEMENTPRESENT] condition is different from actual. Expected :"
                                    + expectedvale + " Actual : "
                                    + isObjectFound + "");
                    checkTrue(
                            false,
                            stopOnFailure,
                            " command checkObjectProperty :Element : ["
                                    + objectName
                                    + "]  [ELEMENTPRESENT] condition is different from actual. Expected :"
                                    + expectedvale + " Actual : "
                                    + isObjectFound + "");
                    break;
                } else {
                    reportresult(true, "CHECK OBJECT PROPERTY : " + objectName
                            + ".ELEMENTPRESENT", "FAILED",
                            " command checkObjectProperty :Element : ["
                                    + objectName
                                    + "] [ELEMENTPRESENT] is not accessible");
                    checkTrue(false, stopOnFailure,
                            " command checkObjectProperty :Element : ["
                                    + objectName
                                    + "] [ELEMENTPRESENT] is not accessible");
                    break;
                }

            }
        }

    }

    /**
     * Checks all the options in a select element. <br>
     * The option value count must be same in actual and expected.
     **/
    private void checkAllSelectOptions(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure) {

        int counter = RETRY;
        String verificationErrors = "";
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            Select selectElement = new Select(element);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            String[] expectedSelectOptions = expectedvale.split(",");

            while (counter > 0) {
                try {
                    counter--;
                    List<WebElement> elementOptions =
                            selectElement.getOptions();
                    String[] actualSelectOptions =
                            new String[elementOptions.size()];
                    for (int i = 0; i < elementOptions.size(); i++) {
                        actualSelectOptions[i] =
                                elementOptions.get(i).getText();
                    }
                    // Check if the input option count is different from the
                    // actual option count
                    if (actualSelectOptions.length == expectedSelectOptions.length) {

                        for (int optionIndex = 0; optionIndex < actualSelectOptions.length; optionIndex++) {

                            if (!Arrays.asList(actualSelectOptions).contains(
                                    expectedSelectOptions[optionIndex])) {

                                verificationErrors +=
                                        ("\n Option :"
                                                + optionIndex
                                                + " : "
                                                + expectedSelectOptions[optionIndex]
                                                + " Option is not available in the actual element. Actual ["
                                                + Arrays.toString(actualSelectOptions) + "]");
                            }
                        }
                        // If there is a mismatch
                        if (!verificationErrors.isEmpty()) {

                            // VTAF result reporter call
                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    "CHECK OBJECT PROPERTY :Element ("
                                            + objectName + ") Error Str:"
                                            + verificationErrors);

                            // VTAF specific validation framework reporting
                            checkTrue(false, stopOnFailure,
                                    "CHECK OBJECT PROPERTY :Element ("
                                            + objectName + ") Error Str:"
                                            + verificationErrors);
                            break;

                        } else {
                            reportresult(true, "CHECK OBJECT PROPERTY :"
                                    + objectName + "." + propertyname,
                                    "PASSED", "Input Value " + expectedvale);
                            break;
                        }

                        // If the length of the input does not match with the
                        // actual option count
                    } else {
                        reportresult(
                                stopOnFailure,
                                "CHECK OBJECT PROPERTY :" + objectName + "."
                                        + propertyname,
                                "FAILED",
                                " command checkObjectProperty()  "
                                        + ":Expected options count :"
                                        + expectedSelectOptions.length
                                        + ""
                                        + " is diffrernt from the Actual options count : "
                                        + actualSelectOptions.length
                                        + " of ["
                                        + objectName
                                        + "] Expected : "
                                        + Arrays.toString(expectedSelectOptions)
                                        + " Actual : " + ""
                                        + Arrays.toString(actualSelectOptions)
                                        + "");
                        checkTrue(
                                false,
                                stopOnFailure,
                                " command checkObjectProperty()  "
                                        + ":Expected options count :"
                                        + expectedSelectOptions.length
                                        + ""
                                        + " is diffrernt from the Actual options count : "
                                        + actualSelectOptions.length
                                        + " of ["
                                        + objectName
                                        + "] Expected : "
                                        + Arrays.toString(expectedSelectOptions)
                                        + " Actual : " + ""
                                        + Arrays.toString(actualSelectOptions)
                                        + "");
                        break;
                    }
                } catch (StaleElementReferenceException staleElementException) {

                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);

                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(stopOnFailure,
                                "CHECK ALL SELECT OPTIONS :" + objectName + "."
                                        + propertyname, "FAILED",
                                " command checkObjectProperty()  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                        checkTrue(false, stopOnFailure,
                                " command checkObjectProperty()  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amount, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            e.printStackTrace();
            reportresult(stopOnFailure, "CHECK ALL SELECT OPTIONS :"
                    + objectName + "." + propertyname, "FAILED",
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
        }
    }

    /**
     * Check the current selected option value in a select element
     */
    private void checkSelectedOption(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure) {

        int counter = RETRY;
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            Select selectElement = new Select(element);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;
                    String selectedOptionLabel =
                            selectElement.getFirstSelectedOption().getText();

                    if (selectedOptionLabel.equals(expectedvale)) {

                        reportresult(true, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "PASSED",
                                "Input Value " + expectedvale);
                        break;
                    } else {
                        reportresult(stopOnFailure, "CHECK OBJECT PROPERTY : "
                                + objectName + "." + propertyname, "FAILED",
                                " object property match Expected:"
                                        + expectedvale + " Actual:"
                                        + selectedOptionLabel);
                        checkTrue(false, stopOnFailure,
                                " object property match Expected:"
                                        + expectedvale + " Actual:"
                                        + selectedOptionLabel);
                        break;
                    }

                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                    selectElement = new Select(element);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "FAILED",
                                " command checkObjectProperty()  :Element ("
                                        + "[" + objectName
                                        + "] cannot access element");
                        checkTrue(false, stopOnFailure,
                                " command checkObjectProperty()  :Element ("
                                        + "[" + objectName
                                        + "] cannot access element");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName
                    + "." + propertyname, "FAILED",
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty()  :Element (" + objectName
                            + ") [" + objectID + "] not present");
        }
    }

    /**
     * Check user defined attribute value in the specific element.
     */

    private void checkObjectOtherProperty(final String objectName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure) {
        int counter = RETRY;
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    String attributeValue =
                            validateObjectProperty(objectID, propertyname,
                                    false);
                    if (attributeValue.trim().equals(expectedvale.trim())) {
                        reportresult(true, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "PASSED",
                                "Input Value " + expectedvale);
                        break;
                    } else {
                        reportresult(true, "CHECK OBJECT PROPERTY :"
                                + objectName + "." + propertyname, "FAILED",
                                " object property match Expected:"
                                        + expectedvale + " is not Presence");
                        checkTrue(false, stopOnFailure,
                                " object property match Expected:"
                                        + expectedvale + " is not Presence");
                        break;
                    }

                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    /*
                     * after the retry amout, if still the object is not found,
                     * report the failure error will be based on the exception
                     * message, if e contains attribute report attribute failure
                     * else if e contains element, report object not found
                     */
                    if (!(counter > 0)) {
                        if (e.getMessage().equals("Attribute")) {
                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    " command checkObjectProperty() :Atrribute ("
                                            + propertyname + ")of ["
                                            + objectName + "] not present");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty() :Atrribute ("
                                            + propertyname + ")of ["
                                            + objectName + "] not present");
                        } else if (e.getMessage().equals("Element")) {
                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    " command checkObjectProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty() :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                        }
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName
                    + "." + propertyname, "FAILED",
                    " command checkObjectProperty() :Element (" + objectName
                            + ") [" + objectID + "] not present");
            checkTrue(false, stopOnFailure,
                    " command checkObjectProperty() :Element (" + objectName
                            + ") [" + objectID + "] not present");
        }
    }

    /**
     * Checks whether the Object property given by the property name is exists
     * if the property does not exists, further continuation of the script
     * execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page
     * 
     * @param objectName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param expectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     */
    public void checkObjectProperty2(final String objectName,
            final String propertyname, final Object objExpectedvale,
            final boolean stopOnFailure) {

        String expectedvale = checkNullObject(objExpectedvale);
        if (expectedvale == null) {
            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + objectName,
                    "FAILED",
                    "CheckObjectProperty command: Invalid input. cannot use null as input");
            checkTrue(false, stopOnFailure,
                    "CheckObjectProperty command: Invalid input. cannot use null as input");
            return;
        }

        int counter = RETRY;
        // retrieve the actual object ID from object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Checking whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    String attributeValue =
                            validateObjectProperty(objectID, propertyname,
                                    false);
                    if (attributeValue == null) {
                        throw new Exception("Attribute");
                    } else if (attributeValue.equalsIgnoreCase("null")) {
                        throw new Exception("Attribute");
                    } else if (attributeValue.equalsIgnoreCase("")) {

                        throw new Exception("Attribute");

                    } else {
                        checkTrue((attributeValue.equals(expectedvale)),
                                stopOnFailure,
                                " object property match Expected:"
                                        + expectedvale + " Actual:"
                                        + attributeValue);

                    }

                    reportresult(true, "CHECK OBJECT PROPERTY :" + objectName
                            + "." + propertyname, "PASSED", "");
                    break;
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    /*
                     * after the retry amout, if still the object is not found,
                     * report the failure error will be based on the exception
                     * message, if e contains attribute report attribute failure
                     * else if e contains element, report object not found
                     */
                    if (!(counter > 0)) {
                        if (e.getMessage().equals("Attribute")) {
                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    " command checkObjectProperty()  :Atrribute ("
                                            + propertyname + ")of  ["
                                            + objectName + "] not present");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty()  :Atrribute ("
                                            + propertyname + ")of  ["
                                            + objectName + "] not present");
                        } else if (e.getMessage().equals("Element")) {
                            reportresult(stopOnFailure,
                                    "CHECK OBJECT PROPERTY :" + objectName
                                            + "." + propertyname, "FAILED",
                                    " command checkObjectProperty()  :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                            checkTrue(false, stopOnFailure,
                                    " command checkObjectProperty()  :Element ("
                                            + objectName + ") [" + objectID
                                            + "] not present");
                        }
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            /*
             * after the retry amout, if still the object is not found, report
             * the failure error will be based on the exception message, if e
             * contains attribute report attribute failure else if e contains
             * element, report object not found
             */
            if (!(counter > 0)) {
                if (e.getMessage().equals("Attribute")) {
                    reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                            + objectName + "." + propertyname, "FAILED",
                            " command checkObjectProperty()  :Atrribute ("
                                    + propertyname + ")of  [" + objectName
                                    + "] not present");
                    checkTrue(false, stopOnFailure,
                            " command checkObjectProperty()  :Atrribute ("
                                    + propertyname + ")of  [" + objectName
                                    + "] not present");
                } else if (e.getMessage().equals("Element")) {
                    reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :"
                            + objectName + "." + propertyname, "FAILED",
                            " command checkObjectProperty()  :Element ("
                                    + objectName + ") [" + objectID
                                    + "] not present");
                    checkTrue(false, stopOnFailure,
                            " command checkObjectProperty()  :Element ("
                                    + objectName + ") [" + objectID
                                    + "] not present");
                }
            }
        }

    }

    /**
     * Checks whether the object is enabled or disabled, further continuation of
     * the script execution will be decided <br>
     * besed on value of the <b> stopExecution </b> parameter provided by the
     * user <br>
     * <br>
     * in the web page
     * 
     * @param objectName
     *            : the logical name of the object <br>
     * @param doCheckEnabled
     *            : specified the expected state enabled\Disabled <br>
     * @param stopOnFailure
     *            : if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure <br>
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusability of the locator.
     *            The usage can be defined using the following example <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * 
     * @throws Exception
     * */
    public void checkObjectEnabled(final String objectName,
            final String identifire, final boolean doCheckEnabled,
            final boolean stopOnFailure) {
        SeleniumTestBase.identifire = identifire;
        checkObjectEnabled(objectName, doCheckEnabled, stopOnFailure);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Checks whether the object is enabled or disabled, further continuation of
     * the script execution will be decided <br>
     * besed on value of the <b> stopExecution </b> parameter provided by the
     * user <br>
     * <br>
     * in the web page
     * 
     * @param objectName
     *            : the logical name of the object <br>
     * @param doCheckEnabled
     *            : specified the expected state enabled\Disabled <br>
     * @param stopOnFailure
     *            : if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure <br>
     * 
     */
    public void checkObjectEnabled(final String objectName,
            final boolean doCheckEnabled, final boolean stopOnFailure) {
        int counter = RETRY;
        // retrieve objectid from the object repository
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {

            // Checcks whether the element is present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    if (doCheckEnabled) {

                        if (element.isEnabled()) {
                            reportresult(true, "CHECK OBJECT ENABLED :"
                                    + objectName + "", "PASSED", "");

                        } else {
                            reportresult(
                                    stopOnFailure,
                                    "CHECK OBJECT ENABLED :" + objectName + "",
                                    "FAILED",
                                    " command checkObjectEnabled()  :Element ("
                                            + objectName
                                            + ") ["
                                            + objectID
                                            + "] is disabled | Expected enabled");
                            checkTrue(
                                    element.isEnabled(),
                                    stopOnFailure,
                                    " command checkObjectEnabled()  :Element ("
                                            + objectName
                                            + ") ["
                                            + objectID
                                            + "] is disabled | Expected enabled");
                        }
                    } else {
                        if (!element.isEnabled()) {
                            reportresult(true, "CHECK OBJECT DISABLED :"
                                    + objectName + "", "PASSED", "");

                        } else {
                            reportresult(
                                    stopOnFailure,
                                    "CHECK OBJECT DISABLED :" + objectName + "",
                                    "FAILED",
                                    " command checkObjectEnabled()  :Element ("
                                            + objectName
                                            + ") ["
                                            + objectID
                                            + "] is Enabled | Expected Disabled");
                            checkTrue(
                                    element.isEnabled(),
                                    stopOnFailure,
                                    " command checkObjectEnabled()  :Element ("
                                            + objectName
                                            + ") ["
                                            + objectID
                                            + "] is Enabled | Expected Disabled");
                        }
                    }

                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    // handle the exception till retry amount is exceeded
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        // once the retry amount is exceeded report a defect
                        reportresult(stopOnFailure, "CHECK OBJECT ENABLED :"
                                + objectName + "", "FAILED",
                                " command checkObjectEnabled()  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                        checkTrue(false, true,
                                " command checkObjectEnabled()  :Element ("
                                        + objectName + ") [" + objectID
                                        + "] not present");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {

            // if element not present is found report failure and continue the
            // execution based on the
            // the value of stopOnFailure variable
            if (!(counter > 0)) {
                e.printStackTrace();
                reportresult(stopOnFailure, "CHECK OBJECT ENABLED :"
                        + objectName + "", "FAILED",
                        " command checkObjectEnabled()  :Element ("
                                + objectName + ") [" + objectID
                                + "] not present");
                checkTrue(false, stopOnFailure,
                        " command checkObjectEnabled()  :Element ("
                                + objectName + ") [" + objectID
                                + "] not present");
            }
        }

    }

    /**
     * This is a multipurpose function which performes various validations in a
     * web table. This is the only function provided with the VTAF which does
     * many types of validations in table. Function provides the following
     * validations
     * 
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>Option</th>
     * <th>Description</th>
     * <th>Option specifier</th>
     * <th>Expected data examples</th>
     * <th>expected result</th>
     * <tbody >
     * <tr>
     * <td>
     * Validate Row Count</td>
     * <td>
     * Checks whether the table contains the expected row count.</td>
     * <td>
     * TableValidationType.ROWCOUNT</td>
     * <td>
     * and integer value specifying the row conut <br>
     * E.g. 10.</td>
     * <td>
     * Pass if the table contains the expectd row count</td>
     * </tr>
     * <tr>
     * <td>Validate Table Cell Value</td>
     * <td>Validating whether the expected data is contained in the cell
     * specified by row and column indexes</td>
     * <td>TableValidationType.CELL</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>firstname</th>
     * <th>lastname</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tbody > </tbody>
     * </table>
     * <br>
     * To validate whether data 'Navaratne' exists in cell given by (1,2) <br>
     * where 1 is the row and column is 2, <br>
     * passing data should be given as, <br>
     * 1,2,Navaratne <br>
     * <br>
     * Note that the index numbering for rows and columns starts from 0 <br>
     * inclusive table header</td>
     * 
     * <td>Pass if the expected data exists in the specified cell</td>
     * </tr>
     * <tr>
     * <td>
     * Validate Column Count</td>
     * <td>
     * Checks whether the column count is maching the expected column count</td>
     * <td>
     * TableValidationType.COLCOUNT</td>
     * <td>
     * and integer value specifying the column conut <br>
     * E.g. 2.</td>
     * <td>
     * Pass if the table contains the expectd column count</td>
     * </tr>
     * 
     * 
     * <tr>
     * <td>
     * validating table data</td>
     * <td>
     * Validating whether the expected data is contained in the table</td>
     * <td>
     * TableValidationType.TABLEDATA</td>
     * <td>
     * A comma seperated list of data specifying the values to be tested <br>
     * E.g. Nadee,Navaratne,29</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * <tr>
     * <td>
     * Relative data validation</td>
     * <td>
     * Validating data in a cell, which is n numbers of cells away from the cell
     * <br>
     * which has the base data</td>
     * <td>
     * TableValidationType.RELATIVE</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>first name</th>
     * <th>sur name</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>Damith</td>
     * <td>Chandana</td>
     * <td>26</td>
     * </tr>
     * 
     * <tbody > </tbody>
     * </table>
     * E.g. 1 <br>
     * suppose if user wants to check the age of Nadee,So the value should be <br>
     * #Nadee,2,29<br>
     * The above data says, cell which is 2 cells away from Nadee should contain
     * 29<br>
     * <br>
     * 
     * E.g. 2<br>
     * suppose if we need to validate the ages of all three users <br>
     * the passing data shlould be<br>
     * #Nadee,2,29#Kanchana,2,28#Damith,2,26 <br>
     * <br>
     * 
     * E.g. 3 <br>
     * suppose the data we know is different,<br>
     * we know<br>
     * first name of the first user<br>
     * sur name of the second user<br>
     * id of the third user<br>
     * <br>
     * 
     * the string to validate age should be <br>
     * #Nadee,2,29#Wickramasingha,1,28#3,3,26 W *</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * </tbody>
     * </table>
     * 
     * @param objectName
     *            Logical name of the object
     * @param validationType
     *            validation type code please refer the above table for detailed
     *            information
     * @param expectedvale
     *            value\value string which will be provided by the user to
     *            specify the expected result
     * @param stopOnFaliure
     *            specify the continuation of the test script if the validation
     *            fails
     * @param identifire
     * 
     *            identifier is us to increase the reusability of the locator.
     *            The usage can be defined using the following example <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Logical Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void checkTable(final String objectName, final String identifire,
            final String validationType, final Object expectedvale,
            final boolean stopOnFaliure) {
        SeleniumTestBase.identifire = identifire;
        checkTable(objectName, validationType, expectedvale, stopOnFaliure);
        SeleniumTestBase.identifire = "";

    }

    public int getObjectCount(final String objectName, final String identifire) {
        SeleniumTestBase.identifire = identifire;
        int objCount = getObjectCount(objectName);
        SeleniumTestBase.identifire = "";
        return objCount;
    }

    /**
     * Select an option\options from a drop-down using an option locator. <br>
     * 
     * Option locators provide different ways of specifying options of an HTML
     * Select element (e.g. for selecting a specific option, or for asserting
     * that the selected option satisfies a specification). There are several
     * forms of Select Option Locator.<br>
     * <br>
     * 
     * <b>label=labelPattern:</b> matches options based on their labels, i.e.
     * the visible text. (This is the default.) label=regexp:^[Oo]ther<br>
     * <b>value=valuePattern:</b> matches options based on their values.
     * value=other<br>
     * <b>id=id:</b> matches options based on their ids. id=option1<br>
     * <b>index=index:</b> matches an option based on its index (offset from
     * zero). index=2<br>
     * <br>
     * 
     * If no option locator prefix is provided, the default behaviour is to
     * match on label. <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter <br>
     * <br>
     * @param value
     *            : value to be selected from the drpodown <br>
     *            If working with multi-select list box, to select multiple
     *            items <br>
     *            give the multiple values seperated by <b> # </b> symbol e.g. <br>
     * <br>
     * 
     *            for selecting single option : apple <br>
     *            for selecting multiple options : apple#orange#mango <br>
     * <br>
     * 
     * */
    public int getObjectCount(final String objectName) {
        int counter = RETRY;
        int objectCount = 0;
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */

            while (counter > 0) {
                try {
                    counter--;

                    List<WebElement> elements =
                            driver.findElements(getLocatorType(objectID));
                    objectCount = elements.size();
                    reportresult(true, "GET OBJECT COUNT :" + objectName + "",
                            "PASSED", "getObjectCount command :Element ("
                                    + objectName + ") [" + objectID + "] ");
                    break;
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "GET OBJECT COUNT :" + objectName
                                + "", "FAILED",
                                "getObjectCount command cannot access :Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "getObjectCount command cannot access  :Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // waiting for the maximum amount of waiting time before failing the
            // test case
            // Several checks were introduced to narrow down to the failure to
            // the exact cause.
            if (!(counter > 0)) {
                e.printStackTrace();
                reportresult(true, "SELECT :" + objectName + "", "FAILED",
                        "GET OBJECT COUNT command  :Element (" + objectName
                                + ") [" + objectID + "] not present");
                checkTrue(false, true, "GET OBJECT COUNT command  :Element ("
                        + objectName + ") [" + objectID + "] not present");
            } else if (e.getMessage().equalsIgnoreCase("Element")) {
                e.printStackTrace();
                reportresult(true, "GET OBJECT COUNT :" + objectName + "",
                        "FAILED", "GET OBJECT COUNT command  :Element ("
                                + objectName + ") [" + objectID
                                + "] not present");
                checkTrue(false, true, "GET OBJECT COUNT command  :Element ("
                        + objectName + ") [" + objectID + "] not present");

            }

        }
        return objectCount;
    }

    /**
     * This is a multipurpose function which performes various validations in a
     * web table. This is the only function provided with the VTAF which does
     * many types of validations in table. Function provides the following
     * validations
     * 
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>Option</th>
     * <th>Description</th>
     * <th>Option specifier</th>
     * <th>Expected data examples</th>
     * <th>expected result</th>
     * <tbody >
     * <tr>
     * <td>
     * Validate Row Count</td>
     * <td>
     * Checks whether the table contains the expected row count.</td>
     * <td>
     * TableValidationType.ROWCOUNT</td>
     * <td>
     * and integer value specifying the row conut <br>
     * E.g. 10.</td>
     * <td>
     * Pass if the table contains the expectd row count</td>
     * </tr>
     * <tr>
     * <td>Validate Table Cell Value</td>
     * <td>Validating whether the expected data is contained in the cell
     * specified by row and column indexes</td>
     * <td>TableValidationType.CELL</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>firstname</th>
     * <th>lastname</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tbody > </tbody>
     * </table>
     * <br>
     * To validate whether data 'Navaratne' exists in cell given by (1,2) <br>
     * where 1 is the row and column is 2, <br>
     * passing data should be given as, <br>
     * 1,2,Navaratne <br>
     * <br>
     * Note that the index numbering for rows and columns starts from 0 <br>
     * inclusive table header</td>
     * 
     * <td>Pass if the expected data exists in the specified cell</td>
     * </tr>
     * <tr>
     * <td>
     * Validate Column Count</td>
     * <td>
     * Checks whether the column count is maching the expected column count</td>
     * <td>
     * TableValidationType.COLCOUNT</td>
     * <td>
     * and integer value specifying the column conut <br>
     * E.g. 2.</td>
     * <td>
     * Pass if the table contains the expectd column count</td>
     * </tr>
     * 
     * 
     * <tr>
     * <td>
     * validating table data</td>
     * <td>
     * Validating whether the expected data is contained in the table</td>
     * <td>
     * TableValidationType.TABLEDATA</td>
     * <td>
     * A comma seperated list of data specifying the values to be tested <br>
     * E.g. Nadee,Navaratne,29</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * <tr>
     * <td>
     * Relative data validation</td>
     * <td>
     * Validating data in a cell, which is n numbers of cells away from the cell
     * <br>
     * which has the base data</td>
     * <td>
     * TableValidationType.RELATIVE</td>
     * <td>
     * see the bellow example table
     * <table TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <th>id</th>
     * <th>first name</th>
     * <th>sur name</th>
     * <th>age</th>
     * <tr>
     * <td>1</td>
     * <td>Nadee</td>
     * <td>Navaratne</td>
     * <td>29</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Kanchana</td>
     * <td>Wickramasingha</td>
     * <td>28</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>Damith</td>
     * <td>Chandana</td>
     * <td>26</td>
     * </tr>
     * 
     * <tbody > </tbody>
     * </table>
     * E.g. 1 <br>
     * suppose if user wants to check the age of Nadee,So the value should be <br>
     * #Nadee,2,29<br>
     * The above data says, cell which is 2 cells away from Nadee should contain
     * 29<br>
     * <br>
     * 
     * E.g. 2<br>
     * suppose if we need to validate the ages of all three users <br>
     * the passing data shlould be<br>
     * #Nadee,2,29#Kanchana,2,28#Damith,2,26 <br>
     * <br>
     * 
     * E.g. 3 <br>
     * suppose the data we know is different,<br>
     * we know<br>
     * first name of the first user<br>
     * sur name of the second user<br>
     * id of the third user<br>
     * <br>
     * 
     * the string to validate age should be <br>
     * #Nadee,2,29#Wickramasingha,1,28#3,3,26 W *</td>
     * <td>
     * Pass if the table contains the data set</td>
     * </tr>
     * 
     * </tbody>
     * </table>
     * 
     * @param objectName
     *            Logical name of the object
     * @param validationType
     *            validation type code please refer the above table for detailed
     *            information
     * @param expectedvale
     *            value\value string which will be provided by the user to
     *            specify the expected result
     * @param stopOnFaliure
     *            specify the continuation of the test script if the validation
     *            fails
     */
    public void checkTable(final String objectName,
            final String validationTypeS, final Object objExpectedvale,
            final boolean stopOnFaliure) {

        int counter = RETRY;
        TableValidationType validationType =
                TableValidationType.valueOf(validationTypeS);
        String expectedvale = checkNullObject(objExpectedvale);
        if (expectedvale == null) {
            reportresult(stopOnFaliure, "CHECK TABLE : " + objectName,
                    "FAILED",
                    "CheckTable command: Invalid input. cannot use null as input");
            checkTrue(false, stopOnFaliure,
                    "CheckTable command: Invalid input. cannot use null as input");
            return;
        }
        String objectID = "";
        // load the actual object id from the OR
        objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // checks the element presence
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);

            // Call the relavant internal method based on the
            // TableValidationType provided by the user
            try {
                if (validationType == TableValidationType.ROWCOUNT) {

                    validateTableRowCount(objectID, expectedvale, stopOnFaliure);
                } else if (validationType == TableValidationType.COLCOUNT) {

                    validateTableColCount(objectID, expectedvale, stopOnFaliure);
                } else if (validationType == TableValidationType.TABLEDATA) {

                    compareTableData(objectID, expectedvale, stopOnFaliure);
                } else if (validationType == TableValidationType.RELATIVE) {

                    validateTableOffset(objectID, expectedvale, stopOnFaliure);
                } else if (validationType == TableValidationType.TABLECELL) {

                    validateCellValue(objectID, expectedvale, stopOnFaliure);
                }
            } catch (StaleElementReferenceException staleElementException) {
                element = checkElementPresence(objectID);
            } catch (Exception e) {
                // waiting for the maximum amount of waiting time before
                // failing the test case
                // Several checks were introduced to narrow down to the
                // failure to the exact cause.

                reportresult(true, "CHECK TABLE :" + validationType + " :"
                        + objectName + "", "FAILED", objectName
                        + " is not accessible");
                checkTrue(false, stopOnFaliure,
                        " command checkTable()  :Element (" + objectName
                                + ") [" + objectID + "] is not accessible");

            }
        } catch (Exception e) {
            // if object is not present catch the exception and repor the
            // error
            e.printStackTrace();
            reportresult(true, "CHECK TABLE :" + validationType + " :"
                    + objectName + "", "FAILED", objectName + " not present");
            checkTrue(false, stopOnFaliure, " command checkTable()  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    /**
     * Validate table cell value function
     * 
     * */
    private void validateCellValue(final String objectName,
            final String expectedvalue, final boolean fail) throws Exception {

        ArrayList<String> inputStringArray = new ArrayList<String>();
        boolean failedOnce = false;
        int row = -1;
        int col = -1;
        String cellText = "";
        String result = "";
        ArrayList<String> htmlTable = new ArrayList<String>();

        inputStringArray =
                new ArrayList<String>(Arrays.asList(expectedvalue.split(
                        "(?<!\\\\),", Integer.MAX_VALUE)));

        ArrayList<String> tempInputTable = new ArrayList<String>();
        for (String inputVal : inputStringArray) {
            String formattedValue = inputVal.replaceAll("\\\\,", ",");
            tempInputTable.add(formattedValue);
        }
        inputStringArray = tempInputTable;

        if (inputStringArray.size() < 3) {
            failedOnce = true;
            result = " verification data not provided correctly";
            reportresult(true, "CHECK TABLE :TABLE CELL", "FAILED", objectName
                    + "'s  CELL validation " + " is not as expected  " + result);
            checkTrue(false, fail, objectName + "'s  CELL validation "
                    + " is not as expected  " + result);
            return;
        }
        row = Integer.parseInt(inputStringArray.get(0));
        col = Integer.parseInt(inputStringArray.get(1));

        cellText =
                StringUtils.join(
                        inputStringArray.subList(2, inputStringArray.size())
                                .toArray(), ",");

        try {
            htmlTable = getAppTableRow(objectName, row);
        } catch (Exception ex) {
            failedOnce = true;
            result =
                    result + "|Expected Row : " + row
                            + " cannot be found in the actual table \n";
        }

        int verifyIndex = col;// get the sequential index of the value to be
                              // verified

        String verifyValue = "";

        try {
            verifyValue = htmlTable.get(verifyIndex).trim();

            if (!cellText.equals(verifyValue)) {
                failedOnce = true;
                result =
                        result + "|Expected : " + cellText + " Actual :"
                                + htmlTable.get(verifyIndex) + "\n";

            }

        } catch (Exception ex) {
            failedOnce = true;
            result =
                    result + "|Expected Column : " + verifyIndex
                            + " cannot be found in the actual table \n";
        }

        if (failedOnce) {
            reportresult(true, "CHECK TABLE :TABLE CELL", "FAILED", objectName
                    + "'s  TABLECELL validation " + " is not as expected  "
                    + result);
            checkTrue(false, fail, objectName + "'s  TABLECELL validation "
                    + " is not as expected  " + result);

        } else {

            reportresult(true, "CHECK TABLE :TABLE CELL", "PASSED", objectName
                    + " . Input Value = " + expectedvalue);
        }

    }

    /**
     * Validate table offset function
     * 
     * */
    private void validateTableOffset(final String objectName,
            final String expectedvalue, final boolean fail) throws Exception {

        ArrayList<String> inputStringArray = new ArrayList<String>();
        boolean failedOnce = false;
        String parentText = "";
        Integer offset = 0;
        String cellText = "";
        Integer indexParent = 0;
        String inputStringCurrStr = "";
        String result = "";
        ArrayList<String> htmlTable = new ArrayList<String>();
        ArrayList<String> inputTable = new ArrayList<String>();

        htmlTable = getAppTable(objectName);

        ArrayList<String> inputStringCurrArray = new ArrayList<String>();
        inputStringArray =
                new ArrayList<String>(Arrays.asList(expectedvalue.split("#")));

        for (int i = 0; i < inputStringArray.size(); i++) {

            // Split the string to parts and entered to an array NAMED
            // inputTable

            // Getting the values out
            inputStringCurrStr = inputStringArray.get(i);

            inputStringCurrArray =
                    new ArrayList<String>(Arrays.asList(inputStringCurrStr
                            .split("(?<!\\\\),", Integer.MAX_VALUE)));

            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputStringCurrArray) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputStringCurrArray = tempInputTable;

            parentText = inputStringCurrArray.get(0);
            offset = Integer.parseInt(inputStringCurrArray.get(1));
            cellText = inputStringCurrArray.get(2);

            if (htmlTable.contains(parentText)) {

                ArrayList<Integer> parentTextIndexList =
                        new ArrayList<Integer>();
                for (int k = 0; k < htmlTable.size(); k++) {
                    if (htmlTable.get(k).equals(parentText)) {
                        parentTextIndexList.add(k);
                    }
                }
                boolean isOffsetMatched = false;
                for (int j = 0; j < parentTextIndexList.size(); j++) {

                    indexParent = parentTextIndexList.get(j);
                    String actualText = "";
                    try {
                        actualText = htmlTable.get((indexParent + offset));
                        if (!cellText.equals(actualText)) {
                            result =
                                    result
                                            + "|Expected : "
                                            + cellText
                                            + " Actual :"
                                            + htmlTable
                                                    .get((indexParent + offset))
                                            + "\n";
                        } else {
                            isOffsetMatched = true;
                            break;
                        }

                    } catch (Exception ex) {
                        failedOnce = true;
                        result =
                                result + "|Expected value : " + cellText
                                        + " cannot be found in the field: "
                                        + (indexParent + offset)
                                        + " in the actual table\n";
                    }
                }
                if (!isOffsetMatched) {
                    failedOnce = true;
                }
            } else {
                failedOnce = true;
                result =
                        result + "|Expected RELATIVE text: " + parentText
                                + " is not present in the actual table \n";
            }
        }

        if (failedOnce) {
            reportresult(fail, "CHECK TABLE :RELATIVE", "FAILED", objectName
                    + "'s  RELATIVE validation " + " is not as expected  "
                    + result);
            checkTrue(false, fail, objectName + "'s  RELATIVE validation "
                    + " is not as expected  " + result);

        } else {

            reportresult(true, "CHECK TABLE :RELATIVE", "PASSED", objectName
                    + " . Input Value = " + expectedvalue);
        }

    }

    /**
     * Validate table row count function
     * 
     * */
    private int validateTableRowCount(final String TableName,
            final String expectedValue, final boolean fail) {
        int rowCount = 0;
        try {
            rowCount = element.findElements(By.tagName("tr")).size();
            if (rowCount == Integer.parseInt(expectedValue)) {
                reportresult(true, "CHECK TABLE :ROW COUNT", "PASSED",
                        "CHECK TABLE :ROW COUNT" + TableName
                                + "| Input Value = " + expectedValue);

            } else {
                reportresult(true, "CHECK TABLE :ROW COUNT", "FAILED",
                        "CHECK TABLE :ROW COUNT" + TableName + "| Expected :"
                                + expectedValue + " |Actual : " + rowCount);
                checkTrue(false, fail,
                        "CHECK TABLE [ROWCOUNT]  : Row count mismatch Expected:"
                                + expectedValue + " Actual:" + rowCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
            reportresult(fail, "CHECK TABLE :ROW COUNT :", "FAILED",
                    e.getMessage());
            checkTrue(false, fail,
                    "CHECK TABLE [ROWCOUNT]  : " + e.getMessage());
        }
        return rowCount;
    }

    /**
     * validate Table Column count function
     */
    private int validateTableColCount(final String TableName,
            final String expectedValue, final boolean fail) {

        List<WebElement> rowElements = null;
        int actualdValue = 0;
        try {
            rowElements = element.findElements(By.tagName("tr"));
            actualdValue =
                    rowElements.get(1).findElements(By.tagName("td")).size();

            if (actualdValue == Integer.parseInt(expectedValue)) {
                reportresult(true, "CHECK TABLE :COLUMN COUNT ", "PASSED",
                        " CHECK TABLE COLUMN COUNT" + TableName
                                + " Input Value = " + expectedValue);

            } else {
                reportresult(fail, "CHECK TABLE :COLUMN COUNT", "FAILED",
                        "Expected :" + expectedValue + " |Actual : "
                                + actualdValue);
                checkTrue(false, fail,
                        "checkTable [COLUMNCOUNT]  : Column count mismatch Expected:"
                                + expectedValue + " Actual:" + actualdValue);
            }

        } catch (Exception e) {
            e.printStackTrace();
            reportresult(fail, "CHECK TABLE :COLUMN COUNT", "FAILED",
                    e.getMessage());
            checkTrue(false, fail, e.getMessage());
        }
        return actualdValue;

    }

    /**
     * Reads the online table and load the contents to an arraylist
     * 
     * */
    private ArrayList<String> getAppTable(final String locator)
            throws Exception {

        WebElement rowElement;
        List<WebElement> columnElements;
        List<WebElement> rowElements;

        ArrayList<String> htmlTable = new ArrayList<String>();
        ArrayList<String> inputTable = new ArrayList<String>();

        rowElements = element.findElements(By.tagName("tr"));
        int rowNum = rowElements.size();

        if (rowNum > 0) {
            String value = "";
            for (int i = 0; i < rowNum; i++) {

                rowElement = rowElements.get(i);

                columnElements = rowElement.findElements(By.tagName("td"));
                if (columnElements.isEmpty()) {
                    columnElements = rowElement.findElements(By.tagName("th"));
                }

                int colNum = columnElements.size();

                for (int j = 0; j < colNum; j++) {

                    value = columnElements.get(j).getText();
                    if (value != null) {
                        htmlTable.add(value);
                    } else if (value == null) {
                        htmlTable.add("");
                    }
                }
            }
        } else {
            throw new Exception();

        }
        return htmlTable;
    }

    /**
     * Reads the online table and load the contents to an arraylist
     * 
     * */
    private ArrayList<String> getAppTableRow(final String locator, final int row)
            throws Exception {

        List<WebElement> rowElements;
        List<WebElement> colElements;
        WebElement rowElement;

        ArrayList<String> htmlTable = new ArrayList<String>();

        rowElements = element.findElements(By.tagName("tr"));
        rowElement = rowElements.get(row);
        colElements = rowElement.findElements(By.tagName("th"));
        colElements.addAll(rowElement.findElements(By.tagName("td")));
        int colNum = colElements.size();

        String value = "";
        for (int j = 0; j < colNum; j++) {

            value = colElements.get(j).getText();
            if (value != null) {
                htmlTable.add(value);
            } else if (value == null) {
                htmlTable.add("");
            }
        }

        return htmlTable;
    }

    /**
     * Checks in a table whether the given table is in
     * 
     * */
    private void compareTableData(final String objectName,
            final String expectedvale, final boolean fail) {

        ArrayList<String> htmlTable = new ArrayList<String>();
        ArrayList<String> inputTable = new ArrayList<String>();
        try {
            htmlTable = getAppTable(objectName);

            inputTable =
                    new ArrayList<String>(Arrays.asList(expectedvale
                            .split("(?<!\\\\),")));
            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputTable) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputTable = tempInputTable;

            String inputTableStr = StringUtils.join(inputTable, "|");
            String actualTableStr = StringUtils.join(htmlTable, "|");

            if (actualTableStr.contains(inputTableStr)) {

                reportresult(true, "CHECK TABLE :TABLE DATA ", "PASSED",
                        objectName + " :Input Value = " + expectedvale);

            } else {

                reportresult(fail, "CHECK TABLE :TABLE DATA ", "FAILED",
                        objectName + "'s  TABLEDATA is not as expected  "
                                + inputTable.toString() + ": Actual :"
                                + htmlTable.toString());
                checkTrue(false, fail,
                        objectName + "'s  TABLEDATA is not as expected  "
                                + inputTable.toString() + ": Actual :"
                                + htmlTable.toString());
            }

        } catch (Exception e) {
            reportresult(fail, "CHECK TABLE :TABLE DATA", "FAILED",
                    e.getMessage());
            checkTrue(false, fail, e.getMessage());
        }
    }

    public enum LocatorType {
        CLASSNAME, CSS, ID, LINK, NAME, TAGNAME, XPATH;
    }

    public By getLocatorType(final String objectID) throws Exception {

        String typeString = "";
        String ref = "";
        if (!objectID.toLowerCase().startsWith("/")) {
            typeString = objectID.substring(0, objectID.indexOf('='));
            ref =
                    objectID.substring(objectID.indexOf('=') + 1,
                            objectID.length());
        }

        if (objectID.toLowerCase().startsWith("/")) {

            return By.xpath(objectID);
        } else if (typeString.toLowerCase().contains("xpath")) {

            return By.xpath(ref);
        } else if (typeString.toLowerCase().contains("css")) {

            return By.cssSelector(ref);
        } else if (typeString.toLowerCase().contains("id")) {

            return By.id(ref);
        } else if (typeString.toLowerCase().contains("link")) {

            return By.linkText(ref);
        } else if (typeString.toLowerCase().contains("tagname")) {

            return By.tagName(ref);
        } else if (typeString.toLowerCase().contains("name")) {

            return By.name(ref);
        } else if (typeString.toLowerCase().contains("classname")) {

            return By.className(ref);
        } else {

            throw new Exception("Invalid Locator Type Passed");
        }

    }

    public WebElement objectLocator(final LocatorType type, final String ref) {
        switch (type) {
        case ID:
            return driver.findElement(By.id(ref));
        case CLASSNAME:
            return driver.findElement(By.className(ref));
        case XPATH:
            return driver.findElement(By.xpath(ref));
        case CSS:
            return driver.findElement(By.cssSelector(ref));
        case LINK:
            return driver.findElement(By.partialLinkText(ref));
        case NAME:
            return driver.findElement(By.name(ref));
        case TAGNAME:
            return driver.findElement(By.tagName(ref));
        }
        return null;
    }

    public WebElement objectLocator(final String objectID) throws Exception {

        System.out.println("INFO : Finding Element [ " + objectID + " ]");
        String typeString = "";
        String ref = "";
        if (!objectID.toLowerCase().startsWith("/")) {
            typeString = objectID.substring(0, objectID.indexOf('='));
            ref =
                    objectID.substring(objectID.indexOf('=') + 1,
                            objectID.length());
        }
        if (objectID.toLowerCase().startsWith("/")) {

            return objectLocator(LocatorType.XPATH, objectID);
        } else if (typeString.toLowerCase().equals("xpath")) {

            return objectLocator(LocatorType.XPATH, ref);
        } else if (typeString.toLowerCase().equals("css")) {

            return objectLocator(LocatorType.CSS, ref);
        } else if (typeString.toLowerCase().equals("id")) {

            return objectLocator(LocatorType.ID, ref);
        } else if (typeString.toLowerCase().equals("link")) {

            return objectLocator(LocatorType.LINK, ref);
        } else if (typeString.toLowerCase().equals("tagname")) {

            return objectLocator(LocatorType.TAGNAME, ref);
        } else if (typeString.toLowerCase().equals("classname")
                || typeString.toLowerCase().equals("class")) {

            return objectLocator(LocatorType.CLASSNAME, ref);
        } else if (typeString.toLowerCase().equals("name")) {

            return objectLocator(LocatorType.NAME, ref);
        } else {
            System.err
                    .println("Invalid Locator Type Passed "
                            + objectID
                            + ". Expected locator types : XPATH, CSS, ID, NAME, LINK, TAGNAME, CLASSNAME");
            throw new Exception("Invalid Locator Type Passed");
        }

    }

    /**
     * internal method which actually checks whether the given element is exists
     * 
     **/
    private WebElement checkElementPresence(final String searchPath)
            throws Exception {

        WebElement webElement = null;
        String locator = searchPath;

        int count = RETRY;
        boolean elementPresent = false;

        setCommandStartTime(getCurrentTime());
        do {
            try {
                webElement = objectLocator(locator);
                if (webElement != null) {
                    System.out.println("INFO : Element [ " + searchPath
                            + " ] Found");
                    elementPresent = true;
                }

                if (elementPresent) {
                    try {
                        jsExecutor = (JavascriptExecutor) driver;
                        jsExecutor.executeScript(
                                "arguments[0].scrollIntoView(false);",
                                webElement);
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                    }
                    break;
                }
            } catch (Exception ex) {
                Thread.sleep(WAITTIME);
            }

        } while (!elementPresent && --count > 0);

        if (!elementPresent && count < 1) {
            System.out.println("ERROR : Element [ " + searchPath
                    + " ] Not Found");
            throw new Exception("Element");
        }

        return webElement;
    }

    /**
     * Internal method which checks whether the option is available in the
     * select box
     * 
     * */
    private boolean checkSelectOptions(final String searchPath,
            final String valuetoBeSelect, final Select selectElement,
            final String[] actualOptions) throws Exception {

        String locator = searchPath;
        String optionList[] = valuetoBeSelect.split("#");
        int count = RETRY;
        boolean elementPresent = false;
        boolean multiSelect = false;
        int notFoundCount = 0;
        String notFoundItems = "";

        if (optionList.length > 1) {
            multiSelect = true;
        }

        do {
            try {

                if (optionList[0].startsWith("index=")) {
                    for (String option : optionList) {

                        int indexNo =
                                Integer.parseInt(option.replace("index=", ""));

                        if (actualOptions.length <= indexNo) {
                            notFoundCount++;
                            notFoundItems =
                                    notFoundItems.concat(
                                            String.valueOf(indexNo)).concat(
                                            " , ");
                        }
                    }

                    if (notFoundCount > 0) {
                        errorMessages =
                                errorMessages + "Input index out of bound |"
                                        + notFoundItems;
                        throw new Exception("Index Out of bound");
                    }

                } else if (optionList[0].startsWith("regexp:")) {

                    Pattern pattern =
                            Pattern.compile(optionList[0].substring(
                                    optionList[0].indexOf(":") + 1,
                                    optionList[0].length()));
                    boolean optionFound = false;
                    for (String actualOption : actualOptions) {
                        Matcher matcher = pattern.matcher(actualOption);
                        if (matcher.matches()) {
                            optionFound = true;
                            break;
                        }
                    }
                    if (!optionFound) {
                        notFoundItems =
                                notFoundItems.concat(optionList[0]).concat(
                                        " | ");
                    }

                } else {
                    for (String option : optionList) {
                        elementPresent =
                                Arrays.asList(actualOptions).contains(option);

                        if (!elementPresent) {
                            notFoundCount++;
                            notFoundItems =
                                    notFoundItems.concat(option).concat(" | ");

                        }

                    }

                    if (notFoundCount > 0) {
                        errorMessages =
                                errorMessages + " Options cannot be found |"
                                        + notFoundItems;
                        throw new Exception("No_Item");
                    }

                }

            } catch (Exception x) {
                if (x.getMessage().equalsIgnoreCase("No_Item")) {
                    throw new Exception("No_Item");
                }
                if (x.getMessage().equalsIgnoreCase("Index Out of bound")) {
                    throw new Exception("Index Out of bound");
                }

            }

        } while (!elementPresent && --count > 0);

        return multiSelect;

    }

    /**
     * Validating the value of the given property of the object, further
     * continuation of the script execution will be decided <br>
     * besed on value of the <b> stopExecution </b> parameter provided by the
     * user <br>
     * <br>
     * in the web page
     * 
     * @param searchPath
     *            : The logical path of the object
     * @param propertyname
     *            : Name of the object property to be validated
     * @param expectedvale
     *            : Expected property value of the object
     * @param stopOnFailure
     *            : if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure <br>
     * 
     * 
     * 
     * */
    private String validateObjectProperty(final String searchPath,
            final String propertyname, final boolean stopOnFailure)
            throws Exception {
        String attributeValue = "";
        if (propertyname.equals("textContent")) {
            try {
                attributeValue = element.getText();

            } catch (Exception ex) {
                throw new Exception("Attribute");
            }
        } else if (propertyname.equals("checked")) {
            try {
                if (element.isSelected()) {
                    attributeValue = "true";
                } else {
                    attributeValue = "false";
                }
            } catch (Exception ex) {
                throw new Exception("Attribute");
            }
        } else {
            try {
                attributeValue = element.getAttribute(propertyname);

                if (attributeValue == null) {
                    throw new Exception("Attribute");
                }
            } catch (Exception e1) {

                throw new Exception("Attribute");
            }
        }
        return attributeValue;
    }

    /**
     * evaluate the given logical condition and report the result Further
     * execution or termination of test will be decided by the value of isAssert
     * variable
     * 
     **/
    private void checkTrue(final boolean checkingCondition,
            final boolean isAssert, final String failedMessage) {
        if (isAssert) {

            endTestReporting(isAssert);
            assertTrue("Failed " + failedMessage + "\n" + errorMessages
                    + " [At : " + callingClassName + "." + currentMethod
                    + "(Line:" + lineNumber + ")]" + "\n", checkingCondition);
        } else {
            try {
                ITestResult reult = new TestResult();
                errorMessages =
                        errorMessages + "\n" + failedMessage + " [At : "
                                + callingClassName + "." + currentMethod
                                + "(Line:" + lineNumber + ")]" + "\n";
                reult = Reporter.getCurrentTestResult();
                reult.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
                reult.setThrowable(new Exception(errorMessages));
                Reporter.setCurrentTestResult(reult);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieve the data table for the parameterized executin
     * */
    public DataTable getTable(final String name) {
        File file;
        if (tables == null) {
            File tempFile = new File("tempFile");
            System.out.println("running location :"
                    + tempFile.getAbsolutePath());
            if (tempFile.getAbsolutePath().contains("grid")) {
                file =
                        new File("grid" + File.separator
                                + "selenium-grid-1.0.6" + File.separator
                                + "data" + File.separator + "DataTables.xml");
                System.out.println("Location of data file is :"
                        + file.getAbsolutePath());
            } else {
                file = new File("data" + File.separator + "DataTables.xml");
                System.out.println("Location of data file is :"
                        + file.getAbsolutePath());
            }
            tables = DataTablesParser.parseTables(file);
        }
        return tables.get(name);
    }

    /**
     * read the DataTable and convert it to a two dimentional array
     * 
     * */
    public Object[][] getTableArray(final DataTable table) {

        Object[][] tabArray = null;
        Integer rowcount = table.getRowCount();
        Integer colcount = table.getcolCount();

        tabArray = new Object[rowcount][colcount];

        for (int row = 0; row < rowcount; row++) {
            for (int col = 0; col < colcount; col++) {
                tabArray[row][col] = table.get(row, col);
            }

        }

        return tabArray;

    }

    /**
     * Retrieving all the browser titles opened
     **/
    public Set<String> getAllWindows() throws Exception {

        Set<String> availableWindows;
        try {
            availableWindows = driver.getWindowHandles();

            // allData = new String[ (availableWindows.size() )];
            return availableWindows;

        } catch (Exception e) {
            throw new Exception("cannot access the windows");
        }
    }

    /**
     * Simulates the back button click event of the browser. <br>
     * 
     * The goBack command waits for the page to load after the navigation
     * 
     * @param waitTime
     *            : Time to wait for goBack command to complete
     * 
     * */
    public void goBack(final String waitTime) {
        try {
            driver.navigate().back();
            super.pause(Integer.parseInt(waitTime));
            reportresult(true, "GO BACK :", "PASSED", "");
        } catch (Exception e) {
            reportresult(true, "GO BACK  :", "FAILED", e.getMessage());
            checkTrue(false, true, "BROWSER BACK :" + "FAILED" + e.getMessage());

        }
    }

    /**
     * Retrieves a String value previously stored
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return String value stored for the given <b>key</b>
     */
    public String retrieveString(final String key) {
        String value = retrieve(key, "String");
        return value;
    }

    /**
     * Retrieve the value of a given key previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the given key is not stored previously</li> <li>
     * stored value type mismatches the type expected</li> <br>
     * <br>
     * 
     * @param key
     *            : key for the value to be retrieved
     * @param type
     *            : type of the previously stored value
     * @return value for the particular <b>key</b>
     */
    public String retrieve(final String key, final String type) {

        String value = null;

        String projectPropertiesLocation = "project_data.properties";

        Properties prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(projectPropertiesLocation);

            try {

                prop.load(fis);

            } catch (IOException e) {
                reportresult(true, "RETRIEVE Value : " + type + " " + key
                        + " :", "FAILED", e.getMessage());
                checkTrue(false, true, "RETRIEVE Value : " + type + " " + key
                        + " :" + "FAILED " + e.getMessage());
            }

            value = prop.getProperty(key + "_Val");
            if (value != null) {
                String type2 = prop.getProperty(key + "_Type");
                if (!type2.equalsIgnoreCase(type)) {
                    // compare against stored type
                    reportresult(true, "RETRIEVE Value : " + type + " " + key
                            + " :", "FAILED", "Trying to retrieve " + type
                            + ", found" + type2);
                    checkTrue(false, true, "RETRIEVE Value : " + type + " "
                            + key + " :" + "FAILED " + "Trying to retrieve "
                            + type + ", found" + type2);
                }
            }

            reportresult(true, "RETRIEVE Value : " + type + " " + key + " :",
                    "PASSED", "Value = " + value);

        } catch (FileNotFoundException e) {

            reportresult(true, "RETRIEVE Value : " + type + " " + key + " :",
                    "PASSED", "RETRIEVE Value : " + type + " " + key + " :");
            return null;

        }
        return value;
    }

    /**
     * Retrieves an int value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to int</li> <br>
     * <br>
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return int value stored for the given <b>key</b> , default is -1
     */
    public int retrieveInt(final String key) {
        String value = retrieve(key, "Int");
        try {
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            reportresult(true, "RETRIEVE Value : Int" + " " + key + " : ",
                    "FAILED", e.getMessage());
            checkTrue(false, true, "RETRIEVE Value : Int " + " " + key + " : "
                    + "FAILED " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a float value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to float</li> <br>
     * <br>
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return float value stored for the given <b>key</b> , default is -1
     */
    public float retrieveFloat(final String key) {
        String value = retrieve(key, "Float");
        try {
            if (value != null) {
                return Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {
            reportresult(true, "RETRIEVE FLOAT: " + " " + key + " : ",
                    "FAILED", e.getMessage());
            checkTrue(false, true, "RETRIEVE FLOAT: " + " " + key + " : "
                    + "FAILED " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a boolean value previously stored <br>
     * <br>
     * <b>Fails</b> if, <li>the stored value is not parsable to boolean</li> <br>
     * <br>
     * 
     * @param key
     *            : key for the value to be retrieved
     * @return boolean value stored for the given <b>key</b> , default is false
     */
    public boolean retrieveBoolean(final String key) {
        String value = retrieve(key, "Boolean");
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        } else {
            reportresult(true, "RETRIEVE BOOLEAN: " + " " + key + " : ",
                    "FAILED", "Cannot parse value to boolean");
            checkTrue(false, true, "RETRIEVE BOOLEAN: " + " " + key + " : "
                    + "FAILED " + "Cannot parse value to boolean");
            return false;
        }
    }

    /**
     * Stores a given key-value pair of given type <br>
     * Overwrites any existing value of same key <br>
     * <br>
     * <b>Fails</b> if, <li>data store file cannot be created</li> <li>data
     * cannot be written to file</li> <li>type of the value to be stored
     * mismatches the type specified</li> <br>
     * <br>
     * 
     * @param key
     *            : key for the value to be stored
     * @param type
     *            : type of value to be stored
     * @param value
     *            : value to be stored
     */
    public void store(final String key, final String type, final Object objValue) {
        String value = checkNullObject(objValue);
        if (value == null) {
            reportresult(true, "STORE command:", "FAILED",
                    "STORE command: Invalid input. cannot use null as input");
            checkTrue(false, true,
                    "STORE command: Invalid input. cannot use null as input");
            return;
        }

        String projectPropertiesLocation = "project_data.properties";
        Properties prop = new Properties();
        FileInputStream fis = null;
        File file = new File(projectPropertiesLocation);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                reportresult(true, "STORE :" + value + " : " + type + " : "
                        + key + " :", "FAILED", e.getMessage());
                checkTrue(false, true, "STORE value " + value + " : " + type
                        + " : " + key + " :" + "FAILED " + e.getMessage());
            }
        }

        try {
            fis = new FileInputStream(file.getAbsoluteFile());
        } catch (FileNotFoundException e) {
            reportresult(true, "STORE value : " + value + " : " + type + " : "
                    + key + " :", "FAILED", e.getMessage());
            checkTrue(false, true, "STORE value " + value + " : " + type
                    + " : " + key + " :" + "FAILED " + e.getMessage());
        }

        try {
            prop.load(fis);
        } catch (IOException e) {
            reportresult(true, "STORE value : " + value + " : " + type + " : "
                    + key + " :", "FAILED", e.getMessage());
            checkTrue(false, true, "STORE value " + value + " : " + type
                    + " : " + key + " :" + "FAILED " + e.getMessage());
        }

        prop.setProperty(key + "_Val", value);
        prop.setProperty(key + "_Type", type);

        // check for type mismatch errors
        if (type.equalsIgnoreCase("Int")) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                reportresult(true, "STORE value : " + value + " Int" + " "
                        + key + " :", "FAILED", e.getMessage());
                checkTrue(false, true, "STORE value " + value + " Int " + " "
                        + key + " :" + "FAILED " + e.getMessage());
            }
        } else if (type.equalsIgnoreCase("Boolean")) {
            if (value.equalsIgnoreCase("true")
                    || value.equalsIgnoreCase("false")) {
                Boolean.parseBoolean(value);
            } else {
                reportresult(true, "STORE Value Boolean:" + " " + key + " :",
                        "FAILED", "Cannot parse value to boolean");
                checkTrue(false, true, "STORE value Boolean " + " " + key
                        + " :" + "FAILED " + "Cannot parse value to boolean");
            }
        } else if (type.equalsIgnoreCase("Float")) {
            try {
                Float.parseFloat(value);
            } catch (NumberFormatException e) {
                reportresult(true, "STORE value : " + value + " Float" + " "
                        + key + " :", "FAILED", e.getMessage());
                checkTrue(false, true, "STORE value " + value + " Float " + " "
                        + key + " :" + "FAILED " + e.getMessage());
            }
        }

        try {
            prop.store(new FileOutputStream(projectPropertiesLocation),
                    "project settings");
            reportresult(true, "STORE Value : " + value + " " + type + " "
                    + key + " :", "PASSED", value);
        } catch (FileNotFoundException e) {
            reportresult(true, "STORE Value : " + value + " " + type + " "
                    + key + " :", "FAILED", e.getMessage());
            checkTrue(false, true, "STORE Value : " + value + " " + type + " "
                    + key + " :" + "FAILED " + e.getMessage());
        } catch (IOException e) {
            reportresult(true, "STORE Value : " + value + " " + type + " "
                    + key + " :", "FAILED", e.getMessage());
            checkTrue(false, true, "STORE Value : " + value + " " + type + " "
                    + key + " :" + "FAILED " + e.getMessage());
        }

    }

    int countp;
    boolean isPopupHandled;
    String inputStringp = "";
    String waitTimep = "";

    public void handlePopup(final String actionFlow, final String waitTime)
            throws Exception {
        robot = new Robot();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.inputStringp = actionFlow;
        this.waitTimep = waitTime;
        this.countp = 10;

        Thread newThread = new Thread(new Runnable() {

            @Override
            public void run() {

                String actualAlertText = "";
                isPopupHandled = false;
                String verificationErrors = "";
                try {
                    Thread.sleep(Integer.parseInt(waitTimep));
                    System.out
                            .println("==============Second Thread Continue==============");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (inputStringp.startsWith("FORCE%")) {
                    try {
                        forceHandlePopup(robot, inputStringp.split("%")[1]);
                        isPopupHandled = true;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    /*
                     * If the popup is not a forcrfully handled it will be
                     * handled in the normal way
                     */

                    String commands[] = inputStringp.split("\\|");
                    try {
                        actualAlertText = driver.switchTo().alert().getText();
                    } catch (NoAlertPresentException e) {

                        reportresult(true,
                                "HANDLE POPUP : failed. No Alert Present",
                                "FAILED", "");
                        checkTrue(false, true,
                                "HANDLE POPUP : failed. No Alert Present");
                    }
                    for (String command : commands) {
                        if (command.toLowerCase().startsWith("type=")) {
                            String typeStr =
                                    command.substring(command.indexOf("=") + 1,
                                            command.length());
                            driver.switchTo().alert().sendKeys(typeStr);

                        } else if (command.toLowerCase().startsWith("verify=")) {
                            String verifyStr =
                                    command.substring(command.indexOf("=") + 1,
                                            command.length());
                            if (!verifyStr.equals(actualAlertText)) {
                                verificationErrors +=
                                        "VERIFY TEXT failed. Actual : " + ""
                                                + actualAlertText
                                                + " Expected : " + verifyStr
                                                + " ";
                            }
                        } else if (command.toLowerCase().startsWith("action=")) {
                            String actionStr =
                                    command.substring(command.indexOf("=") + 1,
                                            command.length());
                            if (actionStr.equalsIgnoreCase("ok")) {
                                driver.switchTo().alert().accept();
                                isPopupHandled = true;
                            } else if (actionStr.equalsIgnoreCase("cancel")) {
                                driver.switchTo().alert().dismiss();
                                isPopupHandled = true;
                            }
                        } else {
                            verificationErrors +=
                                    "Handle Popup command failed. Given input command ("
                                            + command
                                            + ")is not recognized. Supported commands : type, verify, action.";
                        }
                    }

                    if (!isPopupHandled) {
                        driver.switchTo().alert().accept();
                        isPopupHandled = true;
                    }
                    if (verificationErrors.isEmpty()) {
                        reportresult(true, "HANDLE POPUP :" + actualAlertText
                                + "", "PASSED", "");
                    } else {
                        reportresult(true, "HANDLE POPUP : failed", "FAILED",
                                "Errors : " + verificationErrors + "");
                        checkTrue(false, false,
                                "HANDLE POPUP : failed. Errors : "
                                        + verificationErrors + "");
                    }
                }

            }
        }

        );

        newThread.start();

    }

    private void checkForNewWindowPopups() throws Exception {

        Set<String> currentWinHandles = getAllWindows();
        if (currentWinHandles.size() < openWindowHandleIndex.size()) {

            for (int i = 0; i < openWindowHandleIndex.size(); i++) {

                String oldWinHandle = openWindowHandleIndex.get(i);
                if (!currentWinHandles.contains(oldWinHandle)) {
                    try {
                        openWindowHandleIndex.remove(oldWinHandle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } else if (currentWinHandles.size() == openWindowHandleIndex.size()) {

            for (String newWinHandle : currentWinHandles) {

                if (!openWindowHandleIndex.contains(newWinHandle)) {
                    try {
                        openWindowHandleIndex.add(newWinHandle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (int i = 0; i < openWindowHandleIndex.size(); i++) {
                String openWinHandle = openWindowHandleIndex.get(i);
                if (!currentWinHandles.contains(openWinHandle)) {
                    try {
                        openWindowHandleIndex.remove(openWinHandle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } else if (currentWinHandles.size() > openWindowHandleIndex.size()) {

            List<String> tempAllWinHandles = openWindowHandleIndex;
            currentWinHandles.removeAll(tempAllWinHandles);

            if (currentWinHandles.size() > 0) {
                for (String newWinHandle : currentWinHandles) {
                    try {
                        openWindowHandleIndex.add(newWinHandle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void forceHandlePopup(final Robot robot, final String inputString)
            throws ClassNotFoundException, InterruptedException {
        String commandSet[] = inputString.split("\\|");

        for (String fullCommand : commandSet) {
            Thread.sleep(500);
            String command = fullCommand.split("=")[0];
            String input = fullCommand.split("=")[1];
            if (command.equalsIgnoreCase("type")) {

                StringSelection stringSelection = new StringSelection(input);
                clipboard.setContents(stringSelection, null);

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);

            } else if (command.equalsIgnoreCase("Key")) {

                type(input);

            } else if (command.equalsIgnoreCase("wait")) {

                super.pause(Integer.parseInt(input));
            }

        }
    }

    public void type(final String character) {
        switch (character) {
        case "a":
            doType(KeyEvent.VK_A);
            break;
        case "b":
            doType(KeyEvent.VK_B);
            break;
        case "c":
            doType(KeyEvent.VK_C);
            break;
        case "d":
            doType(KeyEvent.VK_D);
            break;
        case "e":
            doType(KeyEvent.VK_E);
            break;
        case "f":
            doType(KeyEvent.VK_F);
            break;
        case "g":
            doType(KeyEvent.VK_G);
            break;
        case "h":
            doType(KeyEvent.VK_H);
            break;
        case "i":
            doType(KeyEvent.VK_I);
            break;
        case "j":
            doType(KeyEvent.VK_J);
            break;
        case "k":
            doType(KeyEvent.VK_K);
            break;
        case "l":
            doType(KeyEvent.VK_L);
            break;
        case "m":
            doType(KeyEvent.VK_M);
            break;
        case "n":
            doType(KeyEvent.VK_N);
            break;
        case "o":
            doType(KeyEvent.VK_O);
            break;
        case "p":
            doType(KeyEvent.VK_P);
            break;
        case "q":
            doType(KeyEvent.VK_Q);
            break;
        case "r":
            doType(KeyEvent.VK_R);
            break;
        case "s":
            doType(KeyEvent.VK_S);
            break;
        case "t":
            doType(KeyEvent.VK_T);
            break;
        case "u":
            doType(KeyEvent.VK_U);
            break;
        case "v":
            doType(KeyEvent.VK_V);
            break;
        case "w":
            doType(KeyEvent.VK_W);
            break;
        case "x":
            doType(KeyEvent.VK_X);
            break;
        case "y":
            doType(KeyEvent.VK_Y);
            break;
        case "z":
            doType(KeyEvent.VK_Z);
            break;
        case "A":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_A);
            break;
        case "B":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_B);
            break;
        case "C":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_C);
            break;
        case "D":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_D);
            break;
        case "E":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_E);
            break;
        case "F":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_F);
            break;
        case "G":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_G);
            break;
        case "H":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_H);
            break;
        case "I":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_I);
            break;
        case "J":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_J);
            break;
        case "K":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_K);
            break;
        case "L":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_L);
            break;
        case "M":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_M);
            break;
        case "N":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_N);
            break;
        case "O":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_O);
            break;
        case "P":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_P);
            break;
        case "Q":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q);
            break;
        case "R":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_R);
            break;
        case "S":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_S);
            break;
        case "T":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_T);
            break;
        case "U":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_U);
            break;
        case "V":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_V);
            break;
        case "W":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_W);
            break;
        case "X":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_X);
            break;
        case "Y":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y);
            break;
        case "Z":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z);
            break;
        case "`":
            doType(KeyEvent.VK_BACK_QUOTE);
            break;
        case "0":
            doType(KeyEvent.VK_0);
            break;
        case "1":
            doType(KeyEvent.VK_1);
            break;
        case "2":
            doType(KeyEvent.VK_2);
            break;
        case "3":
            doType(KeyEvent.VK_3);
            break;
        case "4":
            doType(KeyEvent.VK_4);
            break;
        case "5":
            doType(KeyEvent.VK_5);
            break;
        case "6":
            doType(KeyEvent.VK_6);
            break;
        case "7":
            doType(KeyEvent.VK_7);
            break;
        case "8":
            doType(KeyEvent.VK_8);
            break;
        case "9":
            doType(KeyEvent.VK_9);
            break;
        case "-":
            doType(KeyEvent.VK_MINUS);
            break;
        case "=":
            doType(KeyEvent.VK_EQUALS);
            break;
        case "~":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE);
            break;
        case "!":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_1);
            break;
        case "@":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_2);
            break;
        case "#":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_3);
            break;
        case "$":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_4);
            break;
        case "%":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_5);
            break;
        case "^":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_6);
            break;
        case "&":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_7);
            break;
        case "*":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_8);
            break;
        case "(":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_9);
            break;
        case ")":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_0);
            break;
        case "_":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS);
            break;
        case "+":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_EQUALS);
            break;
        case "\t":
            doType(KeyEvent.VK_TAB);
            break;
        case "\n":
            doType(KeyEvent.VK_ENTER);
            break;
        case "[":
            doType(KeyEvent.VK_OPEN_BRACKET);
            break;
        case "]":
            doType(KeyEvent.VK_CLOSE_BRACKET);
            break;
        case "\\":
            doType(KeyEvent.VK_BACK_SLASH);
            break;
        case "{":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET);
            break;
        case "}":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET);
            break;
        case "|":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH);
            break;
        case ";":
            doType(KeyEvent.VK_SEMICOLON);
            break;
        case ":":
            doType(KeyEvent.VK_COLON);
            break;
        case "'":
            doType(KeyEvent.VK_QUOTE);
            break;
        case "\"":
            doType(KeyEvent.VK_QUOTEDBL);
            break;
        case ",":
            doType(KeyEvent.VK_COMMA);
            break;
        case "<":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA);
            break;
        case ".":
            doType(KeyEvent.VK_PERIOD);
            break;
        case ">":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD);
            break;
        case "/":
            doType(KeyEvent.VK_SLASH);
            break;
        case "?":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH);
            break;
        case " ":
            doType(KeyEvent.VK_SPACE);
            break;
        case "alt":
            doType(KeyEvent.VK_ALT);
            break;
        case "ctrl":
            doType(KeyEvent.VK_CONTROL);
            break;
        case "esc":
            doType(KeyEvent.VK_ESCAPE);
            break;
        case "down":
            doType(KeyEvent.VK_DOWN);
            break;
        case "up":
            doType(KeyEvent.VK_UP);
            break;
        case "left":
            doType(KeyEvent.VK_LEFT);
            break;
        case "right":
            doType(KeyEvent.VK_RIGHT);
            break;
        case "F1":
            doType(KeyEvent.VK_F1);
            break;
        case "F2":
            doType(KeyEvent.VK_F2);
            break;
        case "F3":
            doType(KeyEvent.VK_F3);
            break;
        case "F4":
            doType(KeyEvent.VK_F4);
            break;
        case "F5":
            doType(KeyEvent.VK_F5);
            break;
        case "F6":
            doType(KeyEvent.VK_F6);
            break;
        case "F7":
            doType(KeyEvent.VK_F7);
            break;
        case "F8":
            doType(KeyEvent.VK_F8);
            break;
        case "F9":
            doType(KeyEvent.VK_F9);
            break;
        case "F10":
            doType(KeyEvent.VK_F10);
            break;
        case "F11":
            doType(KeyEvent.VK_F11);
            break;
        case "F12":
            doType(KeyEvent.VK_F12);
            break;
        case "alt+F4":
            doType(KeyEvent.VK_ALT, KeyEvent.VK_F4);
            break;
        case "alt+\t":
            doType(KeyEvent.VK_ALT, KeyEvent.VK_TAB);
            break;
        case "insert":
            doType(KeyEvent.VK_INSERT);
            break;
        case "home":
            doType(KeyEvent.VK_HOME);
            break;
        case "pageup":
            doType(KeyEvent.VK_PAGE_UP);
            break;
        case "backspace":
            doType(KeyEvent.VK_BACK_SPACE);
            break;
        case "delete":
            doType(KeyEvent.VK_DELETE);
            break;
        case "end":
            doType(KeyEvent.VK_END);
            break;
        case "pagedown":
            doType(KeyEvent.VK_PAGE_DOWN);
            break;
        case "shift+\t":
            doType(KeyEvent.VK_SHIFT, KeyEvent.VK_TAB);
            break;
        case "ctrl+o":
            doType(KeyEvent.VK_CONTROL, KeyEvent.VK_O);
            break;
        default:
            throw new IllegalArgumentException("Cannot type character "
                    + character);
        }
    }

    private void doType(final int... keyCodes) {
        doType(keyCodes, 0, keyCodes.length);
    }

    private void doType(final int[] keyCodes, final int offset, final int length) {
        if (length == 0) {
            return;
        }

        try {
            robot.keyPress(keyCodes[offset]);
            doType(keyCodes, offset + 1, length - 1);
            robot.keyRelease(keyCodes[offset]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Simulates a keypress in to an input field as though you typed it key by
     * key from the keyboard.<br>
     * The keys should be seperated form a | charater to be typed correctly.<br>
     * <br>
     * 
     * Example: A|B|C|ctrl|\n|\t|1|2|3 <br>
     * <br>
     * 
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param value
     *            : value to be typed in the object
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */

    public void keyPress(final String objectName, final String identifire,
            final Object value) {
        SeleniumTestBase.identifire = identifire;
        keyPress(objectName, value);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Simulates a keypress in to an input field as though you typed it key by
     * key from the keyboard.<br>
     * The keys should be seperated form a | charater to be typed correctly.<br>
     * <br>
     * 
     * Example: A|B|C|ctrl|\n|\t|1|2|3 <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param value
     *            : value to be typed in the object
     * 
     * */

    public void keyPress(final String objectName, final Object objValue) {

        String value = checkNullObject(objValue);
        if (value == null) {
            reportresult(true, "KEYPRESS :" + objectName + "", "FAILED",
                    "KEYPRESS command: Invalid input. cannot use null as input");
            checkTrue(false, true,
                    "KEYPRESS command: Invalid input. cannot use null as input");
            return;
        }
        int counter = RETRY;

        String[] valueStringsArr = value.split("\\|");

        // Getting the actual object identification from the object map
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    // Calling the actual command
                    element.sendKeys("");
                    Actions getFocus = new Actions(driver);
                    getFocus.moveToElement(element).build().perform();

                    for (int strLocation = 0; strLocation < valueStringsArr.length; strLocation++) {
                        if (!valueStringsArr[strLocation].isEmpty()) {
                            super.pause(Integer.parseInt("1000"));
                            type(valueStringsArr[strLocation]);
                        }
                    }

                    reportresult(true, "KEYPRESS :" + objectName + "",
                            "PASSED", "Input Value = " + value);
                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    // TODO Auto-generated catch block
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "KEYPRESS :" + objectName + "",
                                "FAILED",
                                "KEYPRESS command cannot access :Element ("
                                        + objectName + ") [" + objectID
                                        + "] [Input Value = " + value + "]");
                        checkTrue(false, true,
                                "KEYPRESS command cannot access :Element ("
                                        + objectName + ") [" + objectID
                                        + "] [Input Value = " + value + "]");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // if any exception was raised, report a test failure
            e.printStackTrace();
            reportresult(true, "KEYPRESS :" + objectName + "", "FAILED",
                    "KEYPRESS command  :Element (" + objectName + ") ["
                            + objectID + "] [Input Value = " + value
                            + "] not present");
            checkTrue(false, true, "KEYPRESS command  :Element (" + objectName
                    + ") [" + objectID + "] [Input Value = " + value
                    + "] not present");
        }
    }

    /**
     * Checks if a text in an element is according to a given pattern <br>
     * Can be used to check the value of labels, spans, inputs, etc.(Any element
     * which is containing inner text.)<br>
     * 
     * @param pattern
     * <br>
     *            For the pattern following format shouls be used.<br>
     * <br>
     *            For a uppercase string format should be <b>S</b><br>
     *            For a lowercase string format should be <b>s</b><br>
     *            For a digit format should be <b>d</b><br>
     *            For a special character the character should be entered as it
     *            is.<br>
     * <br>
     *            Ex 1:<br>
     *            For verifying a string like : abc-123#ABC <br>
     *            the pattern should be : sss-ddd#SSS <br>
     * <br>
     * 
     *            Ex 2:<br>
     *            For verifying a date : 12/March/2013 <br>
     *            the pattern should be : dd/Ssss/dddd <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void checkPattern(final String objectName, final String identifire,
            final String pattern) {
        SeleniumTestBase.identifire = identifire;
        checkPattern(objectName, pattern);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Checks if a text in an element is according to a given pattern <br>
     * Can be used to check the value of labels, spans, inputs, etc.(Any element
     * which is containing inner text.)<br>
     * 
     * @param pattern
     * <br>
     * <br>
     *            <b>(Simplified Use)</b><br>
     * <br>
     *            For the pattern following format shouls be used.<br>
     * <br>
     *            For a uppercase string format should be <b>S</b><br>
     *            For a lowercase string format should be <b>s</b><br>
     *            For a digit format should be <b>d</b><br>
     *            For a special character the character should be entered as it
     *            is.<br>
     * <br>
     *            Ex 1:<br>
     *            For verifying a string like : abc-123#ABC <br>
     *            the pattern should be : sss-ddd#SSS <br>
     * <br>
     * 
     *            Ex 2:<br>
     *            For verifying a date : 12/March/2013 <br>
     *            the pattern should be : dd/Ssss/dddd <br>
     * <br>
     * 
     *            <b>(Advanced Use)</b><br>
     * <br>
     *            For advanced use the pure java regex pattern can be passed for
     *            the pattern. The regex pattern should have a prefix of
     *            'regex='<br>
     * <br>
     * 
     *            Ex 1:<br>
     *            For verifying a string like : abc-123#ABC <br>
     *            An example pattern will be :
     *            regex=[a-z][a-z][a-z]-\d\d\d#[A-Z][A-Z][A-Z]
     * 
     * <br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * 
     * */

    public void checkPattern(final String objectName, String pattern) {

        int counter = RETRY;
        String regex = "";
        String returnValue = "";

        if (pattern.toLowerCase().startsWith("regex=")) {
            pattern =
                    pattern.substring(pattern.indexOf('=') + 1,
                            pattern.length());
            regex = pattern;
        } else {
            char[] patternChars = new char[pattern.length()];
            patternChars = pattern.toCharArray();
            for (int strIndex = 0; strIndex < patternChars.length; strIndex++) {

                if (patternChars[strIndex] == 'S') {
                    regex += "[A-Z]";
                } else if (patternChars[strIndex] == 's') {
                    regex += "[a-z]";
                } else if (patternChars[strIndex] == 'd') {
                    regex += "\\d";
                } else {
                    regex += patternChars[strIndex];
                }
            }
        }
        // Getting the actual object identification from the object map
        String objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
        try {
            // Check whether the element present
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    // Calling the actual command
                    returnValue = element.getText().trim();
                    if (returnValue.matches(regex)) {
                        reportresult(true, "CHECKPATTERN :" + objectName
                                + "Input Value = " + pattern, "PASSED",
                                "Input pattern : " + pattern);
                        break;
                    } else {
                        reportresult(
                                true,
                                "CHECKPATTERN :" + objectName + "",
                                "FAILED",
                                "Checked regex pattern ["
                                        + pattern
                                        + "] is different from the actual value : ("
                                        + returnValue + ")");
                        checkTrue(false, true, "Checked regex pattern ["
                                + pattern
                                + "] is different from the actual value : ("
                                + returnValue + ")");
                        break;
                    }

                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "CHECKPATTERN :" + objectName + "",
                                "FAILED",
                                "CHECKPATTERN command cannot access :Element ("
                                        + objectName + ") [" + objectID
                                        + "] pattern = [" + pattern + "]");
                        checkTrue(false, true,
                                "CHECKPATTERN command cannot access :Element ("
                                        + objectName + ") [" + objectID
                                        + "] pattern = [" + pattern + "]");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */
        } catch (Exception e) {
            // if any exception was raised, report a test failure
            e.printStackTrace();
            reportresult(true, "CHECKPATTERN :" + objectName + "", "FAILED",
                    "CHECKPATTERN command  :Element (" + objectName + ") ["
                            + objectID + "] pattern = [" + pattern
                            + "] not present");
            checkTrue(false, true, "CHECKPATTERN command  :Element ("
                    + objectName + ") [" + objectID + "] pattern = [" + pattern
                    + "] not present");
        }
    }

    /**
     * Adds a new comment line to the VTAF test manager report.<br>
     * Functions only with the VTAF test manager report<br>
     * <br>
     * 
     * @param message
     *            : Comment which should be added into the report.<br>
     * <br>
     * 
     *            Ex :<br>
     *            writeToReport("This is a comment")<br>
     * <br>
     **/

    public void writeToReport(final Object objMessage) {
        String message = checkNullObject(objMessage);
        if (message == null) {
            reportresult(true, "WRITE TO REPORT : ", "FAILED",
                    " WRITE TO REPORT command: Invalid input. cannot use null as input");
            checkTrue(false, true,
                    " WRITE TO REPORT command: Invalid input. cannot use null as input");
            return;
        }
        String testComment = "";
        try {
            prop.setProperty("tcComment", "\n" + message);
            prop.store(new FileOutputStream(propertiesLocation), null);
            reportresult(true, "WRITE TO REPORT : ", "PASSED", " [" + message
                    + "]");
        } catch (Exception e) {
            reportresult(true, "WRITE TO REPORT : ", "FAILED", " [" + message
                    + "]");
            e.printStackTrace();
        }

    }

    /**
     * Simulates a user hovering a mouse over the specified element. <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * 
     * @param identifire
     *            :
     * 
     *            Identifier is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifier</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * */
    public void mouseOver(final String objectName, final String identifire) {

        SeleniumTestBase.identifire = identifire;
        mouseOver(objectName);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Simulates a user hovering a mouse over the specified element. <br>
     * 
     * @param objectName
     *            : Logical name of the web element assigned by the automation
     *            scripter
     * 
     * */

    public void mouseOver(final String objectName) {
        String objectID = "";
        int counter = RETRY;
        try {
            // Retrieve the correct object locator from the object map
            objectID = ObjectMap.getObjectSearchPath(objectName, identifire);
            // first verify whether the element is present in the current web
            // pagge
            checkForNewWindowPopups();
            element = checkElementPresence(objectID);
            /*
             * START DESCRIPTION following while loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exitted from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;

                    Actions builder = new Actions(driver);
                    // call for selenium web driver command
                    builder.moveToElement(element).build().perform();
                    // if not exception is called consider and report the result
                    // as passed
                    reportresult(true, "MOUSE OVER :" + objectName + "",
                            "PASSED", "");
                    // if the testcase passed move out from the loop
                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {

                        e.printStackTrace();
                        reportresult(true, "MOUSE OVER :" + objectName + "",
                                "FAILED",
                                "MOUSE OVER command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                        checkTrue(false, true,
                                "MOUSE OVER command cannot access Element ("
                                        + objectName + ") [" + objectID + "] ");
                    }
                }
            }
            /*
             * END DESCRIPTION
             */

        } catch (Exception e) {
            e.printStackTrace();
            /*
             * VTAF result reporter call
             */
            reportresult(true, "MOUSE OVER :" + objectName + "", "FAILED",
                    "MOUSE OVER command  :Element (" + objectName + ") ["
                            + objectID + "] not present");

            /*
             * VTAF specific validation framework reporting
             */
            checkTrue(false, true, "MOUSE OVER command  :Element ("
                    + objectName + ") [" + objectID + "] not present");
        }

    }

    /**
     * Selects a frame within the current window. <br>
     * (You may invoke this command multiple times to select nested frames.) <br>
     * To select the parent frame, use "relative=parent" as a locator; to select
     * the top frame, use "relative=top". You can also select a frame by its
     * 0-based index number; select the first frame with "index=0", or the third
     * frame with "index=2". You may also use a DOM expression to identify the
     * frame you want directly, like this: dom=frames["main"].frames["subframe"]<br>
     * <br>
     * 
     * @param objectName
     *            : Logical name of the frame
     * @param identifire
     *            : identifire is us to increase the reusablity of the locator.
     *            The usage can be defined using the following examble <br>
     * <br>
     *            assume the following locator is assigned the following logical
     *            object name at the object map <br>
     * <br>
     *            <b>locator :</b> //a[@href='http://www.virtusa.com/']<br>
     *            <b>Name :</b> virtusaLink<br>
     * <br>
     * 
     *            If the user thinks that the locator can be made generalized,
     *            it can be parameterized like the following <br>
     * <br>
     *            //a[@href='http://&LTp1&GT/']<br>
     * <br>
     *            once the method is used, pass the <b>identifire</b> as follows<br>
     *            p1: www.virtusa.com<br>
     * <br>
     *            The absolute xpath will be dynamically generated
     * 
     * */
    public void selectFrame(final String objectName, final String identifire)
            throws Exception {

        SeleniumTestBase.identifire = identifire;
        selectFrame(objectName);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Selects a frame within the current window. <br>
     * (You may invoke this command multiple times to select nested frames.) <br>
     * To select the parent frame, use "relative=parent" as a locator; to select
     * the top frame, use "relative=top". You can also select a frame by its
     * 0-based index number; select the first frame with "index=0", or the third
     * frame with "index=2". You may also use a DOM expression to identify the
     * frame you want directly, like this: dom=frames["main"].frames["subframe"]
     * 
     * @param objectName
     *            : locator for the iframe
     * 
     * 
     * */

    public void selectFrame(final String frameName) {
        int counter = RETRY;
        boolean switchByIndex = false;
        boolean switchToParent = false;
        int frameIndex = -1;
        // Getting the actual object identification from the object map
        String objectID = ObjectMap.getObjectSearchPath(frameName, identifire);

        try {

            if (objectID.toLowerCase().startsWith("index=")) {

                switchByIndex = true;
                frameIndex =
                        Integer.parseInt(objectID.substring(
                                objectID.indexOf('=') + 1, objectID.length())
                                .trim());

            } else if (objectID.toLowerCase().trim().equals("parent")
                    || objectID.toLowerCase().trim().equals("null")) {

                switchToParent = true;
            } else {
                checkForNewWindowPopups();
                element = checkElementPresence(objectID);
            }

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or until command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    // Calling the actual command

                    if (switchByIndex) {
                        driver.switchTo().defaultContent();
                        driver.switchTo().frame(frameIndex);
                    } else if (switchToParent) {
                        driver.switchTo().defaultContent();
                    } else {
                        driver.switchTo().frame(element);
                    }

                    reportresult(true, "SELECT FRAME :" + frameName + "",
                            "PASSED", "");
                    break;
                } catch (StaleElementReferenceException staleElementException) {
                    element = checkElementPresence(objectID);
                } catch (Exception e) {
                    try {
                        Thread.sleep(RETRY_INTERVAL);
                    } catch (InterruptedException ex) {
                    }
                    if (!(counter > 0)) {
                        e.printStackTrace();
                        reportresult(true, "SELECT FRAME :" + frameName + "",
                                "FAILED",
                                "SELECT FRAME command cannot access :Frame ("
                                        + frameName + ") [" + objectID + "]");
                        checkTrue(false, true,
                                "SELECT FRAME command cannot access :Frame ("
                                        + frameName + ") [" + objectID + "]");
                    }
                }

            }
        } catch (Exception ex) {

            ex.printStackTrace();
            reportresult(true, "SELECT FRAME :" + frameName + "", "FAILED",
                    "SELECT FRAME command  :Frame (" + frameName + ") ["
                            + objectID + "] not present");
            checkTrue(false, true, "SELECT FRAME command  :Frame (" + frameName
                    + ") [" + objectID + "] not present");

        }
        /*
         * END DESCRIPTION
         */
    }

    /**
     * Performs a Java robot click on the specific coordinates. <br>
     * 
     * @param screenWidht
     *            : Screen width of the of the executing computer screen
     * @param screeHigt
     *            : Screen height of the of the executing computer screen
     * @param xCordinate
     *            : X coordinate of the element in the screen
     * @param yCordinate
     *            : Y coordinate of the element in the screen
     * 
     * */

    public void mouseMoveAndClick(String resolution, final String coordinates,
            final String waitTime) throws Exception {

        if (resolution.startsWith("prop=")) {

            String resolutionFromProp =
                    execProps.getProperty((resolution.split("prop=")[1]));
            if (resolutionFromProp != null) {
                resolution = resolutionFromProp;
            } else {
                reportresult(true, "MOUSE MOVE AND CLICK:", "FAILED",
                        "MOUSE MOVE AND CLICK command: Invalid property key value passed : "
                                + resolution);
                checkTrue(false, true,
                        "MOUSE MOVE AND CLICK command: Invalid property key value passed : "
                                + resolution);
            }
        }

        String[] resArr = resolution.split(",");
        String[] coordinatesArr = coordinates.split(",");

        float screenWidht = Float.parseFloat(resArr[0]);
        float screeHigt = Float.parseFloat(resArr[1]);
        float xCordinate = Float.parseFloat(coordinatesArr[0]);
        float yCordinate = Float.parseFloat(coordinatesArr[1]);
        String command = "";

        if (coordinatesArr.length > 2) {

            command = coordinatesArr[2];
        }

        Robot robot = new Robot();

        super.pause(Integer.parseInt(waitTime));

        int xCordinateAutual = (int) calWidth(screenWidht, xCordinate);
        int yCordinateAutual = (int) calHight(screeHigt, yCordinate);

        robot.keyPress(KeyEvent.VK_F11);
        robot.delay(10);
        robot.keyRelease(KeyEvent.VK_F11);
        Thread.sleep(2000);

        // Mouse Move
        robot.mouseMove(xCordinateAutual, yCordinateAutual);

        // Click
        if (command.equals("")) {

            robot.mousePress(InputEvent.BUTTON1_MASK);
            Thread.sleep(1000);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            reportresult(true, "MOUSE MOVE AND CLICK : ", "PASSED",
                    "MOUSE MOVE AND CLICK command: Resolution : " + resolution);

        }
        // Double Click
        else if (command.toLowerCase().equals("dclick")) {

            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.delay(500);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);

            reportresult(true, "MOUSE MOVE AND DOUBLE CLICK : ", "PASSED",
                    "MOUSE MOVE AND DOUBLE CLICK command: Resolution: "
                            + resolution);
            checkTrue(false, true, "MOUSE MOVE AND CLICK command: Resolution: "
                    + resolution);

        }

        robot.keyPress(KeyEvent.VK_F11);
        robot.delay(10);
        robot.keyRelease(KeyEvent.VK_F11);
    }

    /**
     * Support method for mouseMoveAndClick Calculate the width of the test
     * runner PC.
     * */

    public static double calWidth(final double oldSystemWidth,
            final double oldSystemX) {
        double newSystemX;
        double newSystemWidth = resizeScreen().width;

        System.out.println("New System width=" + newSystemWidth);

        newSystemX = (oldSystemX / oldSystemWidth) * newSystemWidth;
        System.out.println("New System x=" + newSystemX);
        return newSystemX;
    }

    /**
     * Support method for mouseMoveAndClick <br>
     * Calculate the height of the test runner PC.
     * */
    public static double calHight(final double oldSystemHigh,
            final double oldSystemY) {
        double newSystemY;
        double newSystemHigh = resizeScreen().height;
        System.out.println("New System height=" + newSystemHigh);
        newSystemY = (oldSystemY / oldSystemHigh) * newSystemHigh;
        System.out.println("New System y=" + newSystemY);
        return newSystemY;
    }

    /**
     * Support method for mouseMoveAndClick <br>
     * Resize the screen.
     * */
    private static Dimension resizeScreen() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        return dim;
    }

    /**
     * Fires a native robot event into the webpage. <br>
     * 
     * @param event
     *            : Specicy the event which should be performed<br>
     *            1. If a keyboard event event should be started with KEY%<br>
     * <br>
     *            Ex: KEY%\n|\t<br>
     * <br>
     *            2. If it is a mouse event event should be started with MOUSE%<br>
     * <br>
     *            Ex: MOUSE%CLICK|RCLICK <br>
     * <br>
     * @param waittime
     *            : Wait time before the events.
     * */
    public void fireEvent(final String event, final String waittime) {

        super.pause(Integer.parseInt(waittime));

        try {
            robot = new Robot();
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (event.startsWith("KEY%")) {

                fireKeyEvent(event.split("%")[1]);
            } else if (event.startsWith("MOUSE%")) {

                fireMouseEvent(event.split("%")[1]);
            } else if (event.startsWith("VERIFY%")) {

                fireEventVerifyValue(event.split("%")[1]);
            } else {
                reportresult(true, "FIRE EVENT :", "FAILED",
                        "Invalid event type passed :" + event);
                checkTrue(false, true, "Invalid event type passed :" + event);
            }

            reportresult(true, "FIRE EVENT Command : ", "PASSED",
                    "Input Events = " + event);
        } catch (Exception e) {

            if (e.getMessage().equals("Command")) {
                e.printStackTrace();
                reportresult(true, "FIRE EVENT :", "FAILED",
                        "FIRE EVENT passed command is invalid (" + event + ")");
                checkTrue(false, true, "FIRE EVENT passed command is invalid ("
                        + event + ") ");
            } else {
                e.printStackTrace();
                reportresult(true, "FIRE EVENT Command:", "FAILED",
                        "FIRE EVENT command cannot perform the event (" + event
                                + ")");
                checkTrue(false, true,
                        "FIRE EVENT command cannot cannot perform the event ("
                                + event + ") ");

            }
        }
    }

    /**
     * Get the selected text in webpage to the clipboard and compare the value
     * with the given input
     * 
     * */

    private void fireEventVerifyValue(final String value) throws Exception {

        Thread.sleep(500);
        String clipBoardText = "";

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        Thread.sleep(500);
        Transferable trans =
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .getContents(null);

        try {
            if (trans != null) {
                clipBoardText =
                        (String) trans.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (clipBoardText.equals(value)) {

            reportresult(true, "FIRE EVENT : VERIFY VALUE " + value + "",
                    "PASSED", "");
        } else {

            reportresult(true, "FIRE EVENT : VERIFY VALUE " + value + "",
                    "FAILED",
                    "FIRE EVENT : VERIFY VALUE : value match expected. Actual : "
                            + clipBoardText + " Expected : " + value + "");
            checkTrue(false, true,
                    "FIRE EVENT : VERIFY VALUE : value match expected. Actual : "
                            + clipBoardText + " Expected : " + value + "");
        }
    }

    /**
     * Fires a set of java robot key events into the webpage
     * 
     * */

    private void fireKeyEvent(final String commands) throws Exception {

        String commandSet[] = commands.split("\\|");

        for (String fullCommand : commandSet) {
            Thread.sleep(500);
            String command = fullCommand.split("=")[0];
            String input = fullCommand.split("=")[1];
            if (command.equalsIgnoreCase("type")) {

                StringSelection stringSelection = new StringSelection(input);
                clipboard.setContents(stringSelection, null);

                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);

            } else if (command.equalsIgnoreCase("Key")) {

                type(input);
            } else if (command.equalsIgnoreCase("wait")) {

                super.pause(Integer.parseInt(input));
            } else {
                throw new Exception("Command");
            }
        }
    }

    /**
     * Fires a set of java robot mouse events into the webpage
     * 
     * */

    private void fireMouseEvent(final String commands) throws Exception {

        String commandSet[] = commands.split("\\|");

        for (String fullCommand : commandSet) {
            Thread.sleep(500);
            String command = fullCommand.split("=")[0];
            String input = fullCommand.split("=")[1];

            if (command.equalsIgnoreCase("MOVE")) {

                String[] coords = input.split(",");
                int resolutionWidth = Integer.parseInt(coords[0]);
                int resolutionHeight = Integer.parseInt(coords[1]);
                int X = Integer.parseInt(coords[2]);
                int Y = Integer.parseInt(coords[3]);

                int xCordinateAutual = (int) calWidth(resolutionWidth, X);
                int yCordinateAutual = (int) calHight(resolutionHeight, Y);

                robot.keyPress(KeyEvent.VK_F11);
                robot.delay(10);
                robot.keyRelease(KeyEvent.VK_F11);
                Thread.sleep(2000);

                // Mouse Move
                robot.mouseMove(xCordinateAutual, yCordinateAutual);

                robot.keyPress(KeyEvent.VK_F11);
                robot.delay(10);
                robot.keyRelease(KeyEvent.VK_F11);

            } else if (command.equalsIgnoreCase("SCROLL")) {

                robot.mouseWheel(Integer.parseInt(input));

            } else if (command.equalsIgnoreCase("wait")) {

                super.pause(Integer.parseInt(input));
            } else {
                throw new Exception("Command");
            }
        }
    }

    /**
     * Switch between diffent users by user name
     * 
     * */
    public void switchUser(final String instanceName) {

        boolean isNewInstance;
        if (seleniumInstances.containsKey(instanceName)) {
            isNewInstance = false;
        } else {
            isNewInstance = true;
        }
        if (!isNewInstance) {

            driver = seleniumInstances.get(instanceName);
            reportresult(true, "SWITCH USER :" + instanceName + "", "PASSED",
                    "SWITCH USER command :Current user changed to New User("
                            + instanceName + ")");
        } else {
            reportresult(true, "SWITCH USER :" + instanceName + "", "FAILED",
                    "SWITCH USER command : User (" + instanceName
                            + ") is not created. ");
            checkTrue(false, true, "SWITCH USER command : User ("
                    + instanceName + ") is not created. ");
        }

    }

    /**
     * Creates a new user profile and launches a seperate browser session for
     * the user
     * 
     * */

    public void createUser(final String instanceName, final String browser,
            final String serverConfig) {

        boolean isNewInstance;
        if (seleniumInstances.containsKey(instanceName)) {
            isNewInstance = false;
        } else {
            isNewInstance = true;
        }

        if (isNewInstance) {

            try {
                setUp(instanceName, browser, serverConfig);
                startBrowserSession(browser);
                startOfTestCase();
                reportresult(true, "CREATE USER :" + instanceName + "",
                        "PASSED", "CREATE USER command : User (" + instanceName
                                + ") is Created. ");
            } catch (Exception e) {
                reportresult(true, "CREATE USER :" + instanceName + "",
                        "FAILED",
                        "CREATE USER command : Error occured while invoking the new user. Error : "
                                + e.getMessage());
                checkTrue(false, true,
                        "CREATE USER command : Error occured while invoking the new user. Error : "
                                + e.getMessage());
            }

        } else {
            reportresult(true, "CREATE USER :" + instanceName + "", "FAILED",
                    "CREATE USER command : User (" + instanceName
                            + ") is already available. ");
            checkTrue(false, true, "CREATE USER command : User ("
                    + instanceName + ") is already available. ");
        }

    }

    /**
     * Checks the properties of an browser property given by the property name
     * is exists if the property does not exists, further continuation of the
     * script execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page.
     * 
     * Property name : WINDOWPRESENT <br>
     * Check if the window is present
     * 
     * @param windowName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param objExpectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     * */
    public void checkWindowProperty(final String windowName,
            final String identifier, final String propertyname,
            final Object objExpectedvale, final boolean stopOnFailure) {

        SeleniumTestBase.identifire = identifier;
        checkWindowProperty(windowName, propertyname, objExpectedvale,
                stopOnFailure);
        SeleniumTestBase.identifire = "";
    }

    /**
     * Checks the properties of an browser property given by the property name
     * is exists if the property does not exists, further continuation of the
     * script execution will be decided <br>
     * besed on value of the <b> continueExecution </b> parameter provided by
     * the user <br>
     * <br>
     * in the web page.
     * 
     * Property name : WINDOWPRESENT <br>
     * Check if the window is present
     * 
     * @param windowName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param objExpectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     * */

    public void checkWindowProperty(final String windowName,
            final String propertyname, final Object objExpectedvale,
            final boolean stopOnFailure) {

        String expectedvale = checkNullObject(objExpectedvale);
        if (expectedvale == null) {
            reportresult(stopOnFailure, "CHECK OBJECT PROPERTY :" + windowName,
                    "FAILED",
                    "CheckObjectProperty command: Invalid input. cannot use null as input");
            checkTrue(false, stopOnFailure,
                    "CheckObjectProperty command: Invalid input. cannot use null as input");
            return;
        }

        if (propertyname.equals(WindowValidationType.WINDOWPRESENT.toString())) {

            checkWindowPresent(windowName, propertyname, expectedvale,
                    stopOnFailure);
        } else if (propertyname.equals(WindowValidationType.CHECKTITLE
                .toString())) {

            throw new NotImplementedException();
        }

    }

    /**
     * Check if the window is present in the context.
     * 
     * @param windowName
     *            : object name alias given by the user.
     * @param propertyname
     *            : Name of the object property
     * @param expectedvale
     *            : value expected for the given property
     * @param stopOnFailure
     *            :if <I> true </I> : stop the execution after the failure <br>
     *            if <I> false </I>: Continue the execution after the failure
     * */

    private void checkWindowPresent(final String windowName,
            final String propertyname, final String expectedvale,
            final boolean stopOnFailure) {

        int counter = RETRY;
        boolean objectFound = false;
        // Getting the actual object identification from the object map
        String window = ObjectMap.getObjectSearchPath(windowName, identifire);
        try {
            checkForNewWindowPopups();
            Set<String> windowarr = getAllWindows();

            /*
             * START DESCRIPTION following for loop was added to make the
             * command more consistent try the command for give amount of time
             * (can be configured through class variable RETRY) command will be
             * tried for "RETRY" amount of times or untill command works. any
             * exception thrown within the tries will be handled internally.
             * 
             * can be exited from the loop under 2 conditions 1. if the command
             * succeeded 2. if the RETRY count is exceeded
             */
            while (counter > 0) {
                try {
                    counter--;
                    String currentWinHandle = driver.getWindowHandle();
                    for (String windowname : windowarr) {

                        if (window.startsWith("regexp:")
                                || window.startsWith("glob:")) {

                            Pattern pattern =
                                    Pattern.compile(window.substring(
                                            window.indexOf(":") + 1,
                                            window.length()));
                            Matcher matcher =
                                    pattern.matcher(driver.switchTo()
                                            .window(windowname).getTitle());
                            if (matcher.matches()) {
                                objectFound = true;
                                break;
                            }
                        } else {
                            if (driver.switchTo().window(windowname).getTitle()
                                    .equals(window)) {
                                objectFound = true;
                                break;
                            }
                        }
                    }
                    driver.switchTo().window(currentWinHandle);
                    if (expectedvale.equalsIgnoreCase(String
                            .valueOf(objectFound))) {

                        reportresult(true, "CHECK WINDOW PROPERTY:"
                                + propertyname + "", "PASSED", "");
                    } else {
                        reportresult(true, "CHECK WINDOW PROPERTY:"
                                + propertyname + "", "FAILED",
                                "Expected Property : " + propertyname
                                        + " expected value [ " + expectedvale
                                        + " ]does not match the actual ["
                                        + objectFound + "] for the window ["
                                        + windowName + "] [" + window + "]");
                        checkTrue(false, stopOnFailure, "Expected Property : "
                                + propertyname + " expected value [ "
                                + expectedvale
                                + " ]does not match the actual [" + objectFound
                                + "] for the window [" + windowName + "] ["
                                + window + "]");
                    }
                    break;
                } catch (Exception ex) {
                    Thread.sleep(RETRY_INTERVAL);
                    if (!(counter > 0)) {
                        reportresult(true, "CHECK WINDOW PROPERTY:"
                                + propertyname + "", "FAILED",
                                "CHECK WINDOW PROPERTY  :Window (" + windowName
                                        + ") [" + window
                                        + "] is not accessible");
                        checkTrue(false, stopOnFailure,
                                "CHECK WINDOW PROPERTY  :Window (" + windowName
                                        + ") [" + window
                                        + "] is not accessible");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            // if any exception is raised, report failure
            reportresult(true, "CHECK WINDOW PROPERTY:" + propertyname + "",
                    "FAILED", "CHECK WINDOW PROPERTY  :Window (" + windowName
                            + ") [" + window + "] is not accessible");
            checkTrue(false, stopOnFailure, "CHECK WINDOW PROPERTY  :Window ("
                    + windowName + ") [" + window + "] is not accessible");

        }

    }

    private ArrayList<Object> getDBTable(final String instanceName,
            final String query) throws Exception {

        ArrayList<Object> arrList = new ArrayList<>();
        Statement stmt = null;
        ResultSet result = null;
        Connection con = null;

        con = databaseInstances.get(instanceName);
        if (con == null) {

            throw new Exception("Connection instance unavaliable");
        }

        stmt = (Statement) con.createStatement();
        result = stmt.executeQuery(query);
        ResultSetMetaData md = (ResultSetMetaData) result.getMetaData();
        int count = md.getColumnCount();

        while (result.next()) {

            for (int i = 1; i <= count; i++) {
                arrList.add(result.getObject(i));

            }

        }
        if (arrList.isEmpty()) {
            throw new NullPointerException("Empty Result set for the query :- "
                    + query);
        }
        return arrList;

    }

    public void checkDBResults(final String instanceName, final String query,
            final String expectedValue, final boolean stopOnFaliure) {

        ArrayList<Object> objArrList = new ArrayList<>();
        ArrayList<String> inputTable = new ArrayList<>();
        ArrayList<String> strArrList = new ArrayList<>();
        try {
            objArrList = getDBTable(instanceName, query);

            inputTable =
                    new ArrayList<String>(Arrays.asList(expectedValue
                            .split("(?<!\\\\),")));
            ArrayList<String> tempInputTable = new ArrayList<String>();
            for (String inputVal : inputTable) {
                String formattedValue = inputVal.replaceAll("\\\\,", ",");
                tempInputTable.add(formattedValue);
            }
            inputTable = tempInputTable;
            for (Object obj : objArrList) {
                strArrList.add(obj.toString());
            }
            String inputTableStr = StringUtils.join(inputTable, "|");
            String actualTableStr = StringUtils.join(strArrList, "|");
            if (actualTableStr.contains(inputTableStr)) {

                reportresult(true, "CHECK DB RESULTS : ", "PASSED",
                        "For Query = " + query + " :EXPECTED Value = "
                                + expectedValue);

            } else {

                reportresult(stopOnFaliure, "CHECK DB RESULTS : ", "FAILED",
                        "For Query = " + query
                                + " , TABLEDATA is not as expected  "
                                + inputTable.toString() + ": Actual :"
                                + objArrList.toString());
                checkTrue(false, stopOnFaliure,
                        "For Query = " + query
                                + " , TABLEDATA is not as expected  "
                                + inputTable.toString() + ": Actual :"
                                + objArrList.toString());
            }

        } catch (SQLException e) {
            reportresult(stopOnFaliure, "CHECK DB RESULTS :", "FAILED",
                    "SQL Error occured" + e.getMessage());
            checkTrue(false, stopOnFaliure,
                    "SQL Error occured" + e.getMessage());

        } catch (NullPointerException e) {
            reportresult(stopOnFaliure, "CHECK DB RESULTS :", "FAILED",
                    e.getMessage());
            checkTrue(false, stopOnFaliure, e.getMessage());
        } catch (Exception e) {
            if (e.getMessage().equals("Connection instance unavaliable")) {
                reportresult(true, "CHECK DB RESULTS :" + instanceName + "",
                        "FAILED", "CHECK DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
                checkTrue(false, true,
                        "CHECK DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
            } else {
                reportresult(stopOnFaliure, "CHECK DB RESULTS :", "FAILED",
                        e.getMessage());
                checkTrue(false, stopOnFaliure, e.getMessage());
            }
        }

    }

    public String getStringDBResult(final String instanceName,
            final String query) {

        ArrayList<Object> arrList = new ArrayList<>();
        String Value = null;
        try {
            arrList = getDBTable(instanceName, query);
            Value = arrList.get(0).toString();
            if (arrList.size() >= 2) {
                reportresult(
                        true,
                        "SET DB RESULTS : ",
                        "PASSED",
                        "For Query = "
                                + query
                                + " Actual result contains more than one value. Actual Values :- "
                                + arrList + " Return Value :- " + Value);
            }
            reportresult(true, "SET DB RESULTS : ", "PASSED", "For Query = "
                    + query);
        } catch (SQLException e) {
            reportresult(true, "SET DB RESULTS :", "FAILED",
                    "SQL Error occured" + e.getMessage());
            checkTrue(false, true, "SQL Error occured" + e.getMessage());
        } catch (NullPointerException e) {
            reportresult(true, "SET DB RESULTS :", "FAILED", e.getMessage());
            checkTrue(false, false, e.getMessage());
        } catch (Exception e) {
            if (e.getMessage().equals("Connection instance unavaliable")) {
                reportresult(true, "SET DB RESULTS :" + instanceName + "",
                        "FAILED", "SET DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
                checkTrue(false, true, "SET DB RESULTS command : connection ("
                        + instanceName + ") is not created. ");
            } else {
                reportresult(true, "SET DB RESULTS :", "FAILED", e.getMessage());
                checkTrue(false, false, e.getMessage());
            }
        }
        return Value;
    }

    public int getIntDBResult(final String instanceName, final String query)
            throws NumberFormatException {

        ArrayList<Object> arrList = new ArrayList<>();
        Integer Value = null;
        try {
            arrList = getDBTable(instanceName, query);
            if (!(arrList.get(0) instanceof Integer)) {
                throw new NumberFormatException("The value trying to retrive ("
                        + arrList.get(0).toString()
                        + ") is not stored as an interger in the database.");
            }
            Value = (Integer) arrList.get(0);
            reportresult(true, "SET DB RESULTS : ", "PASSED", "For Query = "
                    + query);
        } catch (SQLException e) {
            reportresult(true, "SET DB RESULTS :", "FAILED",
                    "SQL Error occured" + e.getMessage());
            checkTrue(false, true, "SQL Error occured" + e.getMessage());
        } catch (NullPointerException e) {
            reportresult(true, "SET DB RESULTS :", "FAILED", e.getMessage());
            checkTrue(false, false, e.getMessage());
        } catch (Exception e) {
            if (e.getMessage().equals("Connection instance unavaliable")) {
                reportresult(true, "SET DB RESULTS :" + instanceName + "",
                        "FAILED", "SET DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
                checkTrue(false, true, "SET DB RESULTS command : connection ("
                        + instanceName + ") is not created. ");
            } else {
                reportresult(true, "SET DB RESULTS :", "FAILED", e.getMessage());
                checkTrue(false, false, e.getMessage());
            }
        }
        return Value.intValue();
    }

    public Boolean getBooleanDBResult(final String instanceName,
            final String query) {

        ArrayList<Object> arrList = new ArrayList<>();
        Boolean Value = null;
        try {
            arrList = getDBTable(instanceName, query);
            if (!(arrList.get(0) instanceof Boolean)) {
                throw new Exception("The value trying to retrive ("
                        + arrList.get(0).toString()
                        + ") is not stored as a boolean in the database.");
            }

            Value = (Boolean) arrList.get(0);
            reportresult(true, "SET DB RESULTS : ", "PASSED", "For Query = "
                    + query);
        } catch (SQLException e) {
            reportresult(true, "SET DB RESULTS :", "FAILED",
                    "SQL Error occured" + e.getMessage());
            checkTrue(false, true, "SQL Error occured" + e.getMessage());
        } catch (NullPointerException e) {
            reportresult(true, "SET DB RESULTS :", "FAILED", e.getMessage());
            checkTrue(false, false, e.getMessage());
        } catch (Exception e) {
            if (e.getMessage().equals("Connection instance unavaliable")) {
                reportresult(true, "SET DB RESULTS :" + instanceName + "",
                        "FAILED", "SET DB RESULTS command : connection ("
                                + instanceName + ") is not created. ");
                checkTrue(false, true, "SET DB RESULTS command : connection ("
                        + instanceName + ") is not created. ");
            } else {
                reportresult(true, "SET DB RESULTS :", "FAILED", e.getMessage());
                checkTrue(false, false, e.getMessage());
            }
        }
        System.out.println(Value.booleanValue());
        return Value.booleanValue();
    }

    public void createDBConnection(final String databaseType,
            final String instanceName, final String url, final String username,
            final String password) {
        Connection con = null;
        boolean isNewInstance;

        if (databaseInstances.containsKey(instanceName)) {
            isNewInstance = false;
        } else {
            isNewInstance = true;
        }

        if (isNewInstance) {

            try {
                if (databaseType.equalsIgnoreCase("mysql")) {
                    String dbClass = "com.mysql.jdbc.Driver";
                    Class.forName(dbClass).newInstance();
                    con = DriverManager.getConnection(url, username, password);
                } else if (databaseType.equalsIgnoreCase("oracle")) {
                    DriverManager
                            .registerDriver(new oracle.jdbc.driver.OracleDriver());
                    con = DriverManager.getConnection(url, username, password);
                } else if (databaseType.equalsIgnoreCase("mssql")) {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    con = DriverManager.getConnection(url, username, password);
                } else if (databaseType.isEmpty()) {
                    reportresult(true, "CREATE DB CONNECTION :", "FAILED",
                            "Database type not selected");
                    checkTrue(false, true,
                            "CREATE DB CONNECTION command: Error :- No database type selected.");
                }
                databaseInstances.put(instanceName, con);
                reportresult(true, "CREATE DB CONNECTION :", "PASSED", "");
            } catch (SQLException e) {
                e.printStackTrace();
                reportresult(true, "CREATE DB CONNECTION :", "FAILED",
                        "SQL service is not started");
                checkTrue(
                        false,
                        true,
                        "CREATE DB CONNECTION command: Error :- "
                                + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                reportresult(true, "CREATE DB CONNECTION :", "FAILED",
                        e.getMessage());
                checkTrue(
                        false,
                        true,
                        "CREATE DB CONNECTION command: Error :- "
                                + e.getMessage());
            }

        }

    }

    private ScreenRegion isImagePresent(final String path,
            final boolean isRotatable) {

        int retry = RETRY;

        double regQuality = maxRecQuality;

        Target target;

        javaxt.io.Image img = new javaxt.io.Image(path);
        ScreenRegion targetRegion = null;
        while (retry > 0) {
            retry--;
            if (isRotatable) {

                for (int i = 0; i < 360; i += rotationDegree) {

                    ScreenRegion s = new DesktopScreenRegion();
                    target = new ImageTarget(img.getBufferedImage());
                    target.setMinScore(regQuality);
                    targetRegion = s.find(target);
                    if (targetRegion == null) {
                        if (retry > 0) {
                            img.rotate(rotationDegree);
                            sleep(RETRY_INTERVAL);
                            continue;
                        } else {
                            break;
                        }
                    } else {

                        break;

                    }
                }
                if (targetRegion != null) {
                    break;
                }
            }

            else {
                ScreenRegion s = new DesktopScreenRegion();
                target = new ImageTarget(img.getBufferedImage());
                target.setMinScore(regQuality);
                targetRegion = s.find(target);
                if (targetRegion == null) {
                    if (retry > 0) {
                        sleep(RETRY_INTERVAL);
                        continue;
                    }

                } else {

                    break;
                }
            }

        }
        if (targetRegion == null) {
            while (targetRegion == null && regQuality >= minRecQuality) {
                ScreenRegion s = new DesktopScreenRegion();
                target = new ImageTarget(img.getBufferedImage());
                target.setMinScore(regQuality);
                targetRegion = s.find(target);
                regQuality = regQuality - 0.1;
                if (targetRegion == null) {
                    sleep(RETRY_INTERVAL);
                }

            }
        }

        return targetRegion;
    }

    private static void sleep(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleImagePopup(final String imagePath,
            final String actionFlow, final String waitTime) throws Exception {
        robot = new Robot();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        this.inputStringp = actionFlow;
        this.waitTimep = waitTime;
        this.countp = 10;

        Thread newThread = new Thread(new Runnable() {

            @Override
            public void run() {

                isPopupHandled = false;
                try {
                    Thread.sleep(Integer.parseInt(waitTimep));
                    System.out
                            .println("==============Second Thread Continue==============");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    ScreenRegion targetImage = isImagePresent(imagePath, false);
                    if (targetImage != null) {
                        try {
                            forceHandlePopup(robot, inputStringp);
                            reportresult(true, "HANDLE IMAGE POPUP :",
                                    "PASSED", "");
                        } catch (Exception e) {
                            e.printStackTrace();
                            reportresult(true, "HANDLE IMAGE POPUP :",
                                    "FAILED", e.getMessage());
                            checkTrue(
                                    false,
                                    true,
                                    "HANDLE IMAGE POPUP command: Error :- "
                                            + e.getMessage());
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    reportresult(true, "HANDLE IMAGE POPUP :", "FAILED",
                            e.getMessage());
                    checkTrue(
                            false,
                            true,
                            "HANDLE IMAGE POPUP command: Error :- "
                                    + e.getMessage());
                }

            }
        });
        newThread.start();
    }

    public boolean checkImagePresent(final String path,
            final boolean isRotatable) {

        boolean isElementPresent = false;
        try {

            isElementPresent = isImagePresent(path, isRotatable) != null;
            if (isElementPresent) {
                reportresult(true, "CHECK IMAGE PRESENT:", "PASSED", path);

            } else {
                reportresult(true, "CHECK IMAGE PRESENT:", "FAILED",
                        "CHECK IMAGE PRESENT command cannot find the image :- "
                                + path + " in current screen.");

            }
        } catch (Exception e) {
            e.printStackTrace();
            reportresult(true, "CHECK IMAGE PRESENT :", "FAILED",
                    "Trying to access Invalid Image :- " + path + "");

        }
        return isElementPresent;

    }

    public void checkImagePresent(final String path, final boolean isRotatable,
            final boolean stopOnFailure) {

        boolean isElementPresent = false;
        try {

            isElementPresent = isImagePresent(path, isRotatable) != null;
            if (isElementPresent) {
                reportresult(true, "CHECK IMAGE PRESENT:", "PASSED", path);

            } else {
                reportresult(true, "CHECK IMAGE PRESENT:", "FAILED",
                        "CHECK IMAGE PRESENT command cannot find the image :- "
                                + path + " in current screen.");
                checkTrue(false, stopOnFailure,
                        "CHECK IMAGE PRESENT command cannot find the image :- "
                                + path + " in current screen.");

            }
        } catch (Exception e) {
            e.printStackTrace();
            reportresult(true, "CHECK IMAGE PRESENT :", "FAILED",
                    "Trying to access Invalid Image :- " + path + "");
            checkTrue(false, stopOnFailure,
                    "CHECK IMAGE PRESENT command:Trying to access Invalid Image :- "
                            + path + "");

        }

    }
}
