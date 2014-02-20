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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.IResultListener;

public class ScreenshotListenerVir implements IResultListener {

    File outputDirectory;
    WebDriver driver;

    public ScreenshotListenerVir(final File outputDirectory,
            final WebDriver driver) {
        this.outputDirectory = outputDirectory;
        this.driver = driver;
    }

    @Override
    public void onTestFailure(final ITestResult result) {
        Reporter.setCurrentTestResult(result);

        try {

            System.out.println("Test failed");
            outputDirectory.mkdirs();
            System.out.println("Created directory "
                    + outputDirectory.getAbsolutePath());

            System.out.println("Out file deleted...");
            String fileloc = getProperties();
            File scrFile =
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(fileloc + ".png"));
            Reporter.log("<a href='" + fileloc + ".png'>screenshot</a>");
        } catch (Exception e) {
            e.printStackTrace();
            Reporter.log("Couldn't create screenshot");
            Reporter.log(e.getMessage());
        }

        Reporter.setCurrentTestResult(null);
    }

    private static String getProperties() throws FileNotFoundException,
            IOException {
        Properties props = new Properties();
        File file = new File("");
        String absPath = file.getAbsolutePath();
        if (!(absPath.indexOf("grid") > -1)) {
            absPath = absPath;
        } else {

            absPath =
                    absPath + File.separator + "grid" + File.separator
                            + "selenium-grid-1.0.6";
        }
        String fileName = "";
        props.load(new FileInputStream(new File(absPath + File.separator
                + "project.properties")));
        fileName = props.getProperty("currentScreenShot");

        props.clear();

        return fileName;
    }

    @Override
    public void onConfigurationFailure(final ITestResult result) {
        onTestFailure(result);
    }

    @Override
    public void onFinish(final ITestContext context) {
    }

    @Override
    public void onStart(final ITestContext context) {
        outputDirectory = new File(context.getOutputDirectory());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {
    }

    @Override
    public void onTestSkipped(final ITestResult result) {
    }

    @Override
    public void onTestStart(final ITestResult result) {
    }

    @Override
    public void onTestSuccess(final ITestResult result) {
    }

    @Override
    public void onConfigurationSuccess(final ITestResult itr) {
    }

    @Override
    public void onConfigurationSkip(final ITestResult itr) {
    }
}
