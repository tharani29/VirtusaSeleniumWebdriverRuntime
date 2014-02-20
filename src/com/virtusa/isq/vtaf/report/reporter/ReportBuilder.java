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
package com.virtusa.isq.vtaf.report.reporter;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.virtusa.isq.vtaf.report.model.TestCase;
import com.virtusa.isq.vtaf.report.model.TestExecution;
import com.virtusa.isq.vtaf.report.model.TestStep;
import com.virtusa.isq.vtaf.report.model.TestSuite;

public class ReportBuilder {

    private TestCase testCase;
    private TestSuite testSuite;
    private TestStep testStep;
    private TestExecution testExecution;
    private String reportFolderLocation;

    private int rid;

    public ReportBuilder(final String reportFolderLocation) {
        this.reportFolderLocation = reportFolderLocation;
        rid = 0;
    }

    public String getReportFolderLocation() {
        return this.reportFolderLocation;
    }

    public void addNewTestCase(final String modulename, final String duration) {

        String moduletype = "UserCode";
        String type = "test module";
        String rid = getRid();

        testCase = new TestCase(modulename, moduletype, duration, type, rid);
        testSuite.testCases.add(testCase);
    }

    public void addNewTestSuite(final String testSuiteName,
            final String duration) {
        String iterationcount = "1";
        String maxchildren = "0";
        String type = "folder";
        String rid = getRid();

        testSuite =
                new TestSuite(testSuiteName, iterationcount, maxchildren,
                        duration, type, rid);

        testExecution.testSuites.add(testSuite);

    }

    public void addNewTestExecution() {

        int totalerrorcount = 0;
        int totalwarningcount = 0;
        int totalsuccesscount = 0;
        int totalfailedcount = 0;
        int totalblockedcount = 0;
        String host = "UNKNOWN";
        String user = "UNKNOWN";
        String osversion = "UNKNOWN";
        String language = "EN-US";
        String screenresolution = "UNKNOWN";
        String timestamp = "UNKNOWN";
        String duration = "UNKNOWN";
        String type = "root";
        try {
            user = System.getProperty("user.name");
            host = InetAddress.getLocalHost().getHostName();
            osversion = System.getProperty("os.version");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            screenresolution = screenSize.width + "X" + screenSize.height;

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
            timestamp = sdf.format(date);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }

        testExecution =
                new TestExecution(user, host, osversion, language,
                        screenresolution, timestamp, duration, type,
                        totalerrorcount, totalwarningcount, totalsuccesscount,
                        totalfailedcount, totalblockedcount);

    }

    public void addNewTestStep(final boolean isPassed, final String category,
            final String message, final String loglvl) {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss");
        String time = sdf.format(date);
        String codefile = "UNKNOWN";
        String codeline = "UNKNOWN";

        testStep =
                new TestStep(isPassed, time, category, message, codefile,
                        codeline, loglvl);

        testCase.testSteps.add(testStep);

    }

    public void addNewTestStep(final boolean isPassed, final String category,
            final String errimg, final String errthumb, final String message,
            final String stacktrace, final String loglvl) {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss");
        String time = sdf.format(date);
        String codefile = "UNKNOWN";
        String codeline = "UNKNOWN";

        testStep =
                new TestStep(isPassed, time, category, errimg, errthumb,
                        message, stacktrace, codefile, codeline, loglvl);

        testCase.testSteps.add(testStep);

        if (!isPassed) {
            testCase.setResult("Failed");
            testSuite.setResult("Failed");
        }
    }

    public void setExecutionSummary() {

        for (TestSuite ts : testExecution.testSuites) {
            for (TestCase tc : ts.testCases) {
                if (tc.getResult().equals("Failed")) {
                    testExecution.setTotalfailedcount(1);
                    testExecution.setTotalerrorcount(1);
                } else if (tc.getResult().equals("Success")) {
                    testExecution.setTotalsuccesscount(1);
                }
            }
        }

        System.out.println("Report created successfully to the folder "
                + getReportFolderLocation());
    }

    public TestExecution getTestExecution() {
        return testExecution;
    }

    public String getRid() {
        rid++;
        return String.valueOf(rid);
    }

}
