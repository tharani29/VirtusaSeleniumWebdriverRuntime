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

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.*;

import com.virtusa.isq.vtaf.report.exporter.Generator;

public class Reporter {

    private ReportBuilder builder;

    public Reporter() {

        builder = new ReportBuilder(initReportDirectory());
    }

    private String initReportDirectory() {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ssa");
        String timestamp = sdf.format(date);

        File reportFolder =
                new File("Reports" + File.separator + "ExecutionReport"
                        + timestamp);

        try {
            if (!reportFolder.exists()) {
                reportFolder.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyReportHelperFiles(reportFolder.getAbsolutePath());

        System.out.println(reportFolder.getAbsolutePath());
        return reportFolder.getAbsolutePath();
    }

    private void copyReportHelperFiles(final String reportFolderStr) {
        File reportTemplateHtml =
                new File(new File("ReportTemplate").getAbsolutePath());
        File reportFolder = new File(reportFolderStr);
        try {
            FileUtils.copyDirectory(reportTemplateHtml, reportFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewTestExecution() {
        builder.addNewTestExecution();
    }

    public void addNewTestSuite(final String testSuiteName) {
        builder.addNewTestSuite(testSuiteName, "0ms");
    }

    public void addNewTestCase(final String testCaseName) {
        builder.addNewTestCase(testCaseName, "0ms");
    }

    public void reportStepResults(final boolean isPassed,
            final String category, final String message, final String loglvl,
            final String stacktrace) {

        if (isPassed) {
            builder.addNewTestStep(isPassed, category, message, loglvl);
        } else {

            String screenShot =
                    saveScreenShot(builder.getReportFolderLocation());
            String thumbScreenShot = saveScreenshotThumb(screenShot);
            builder.addNewTestStep(isPassed, category, "images"
                    + File.separator + screenShot, thumbScreenShot, message,
                    stacktrace, "Error");
        }

    }

    private String saveScreenShot(final String reportFolderLocation) {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String timestamp = sdf.format(date);
        Rectangle screenRect =
                new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage capture;
        String screenShotFile = timestamp + ".png";
        String screenShotImgFolder =
                reportFolderLocation + File.separator + "images";
        try {
            capture = new Robot().createScreenCapture(screenRect);
            File screenShotImgFolderFile = new File(screenShotImgFolder);

            if (!screenShotImgFolderFile.exists()) {
                screenShotImgFolderFile.mkdirs();
            }
            File screenShotImg =
                    new File(screenShotImgFolder + File.separator
                            + screenShotFile);
            ImageIO.write(capture, "png", screenShotImg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AWTException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return screenShotFile;
    }

    private String saveScreenshotThumb(final String screenShotFile) {
        String screenShotThumb =
                "images" + File.separator + screenShotFile + "_Thumb.png";
        try {
            String screenShotOriginalFile =
                    builder.getReportFolderLocation() + File.separator
                            + "images" + File.separator + screenShotFile;
            BufferedImage img = ImageIO.read(new File(screenShotOriginalFile));
            BufferedImage thumb =
                    Scalr.resize(img, Method.SPEED, 150, 100,
                            Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER);

            ImageIO.write(thumb, "png", new File(screenShotOriginalFile
                    + "_Thumb.png"));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return screenShotThumb;
    }

    public void endTestReporting() {

        builder.setExecutionSummary();
        Generator generator = new Generator();
        generator.generateReport(builder);
    }

}
