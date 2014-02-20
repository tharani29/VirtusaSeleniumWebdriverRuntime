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

package com.virtusa.isq.vtaf.report.model;

import java.util.ArrayList;
import java.util.List;

public class TestExecution {

    private String user;
    private String host;
    private String osversion;
    private String language;
    private String screenresolution;
    private String timestamp;
    private String result;
    private String duration;
    private String type;
    private int totalerrorcount;
    private int totalwarningcount;
    private int totalsuccesscount;
    private int totalfailedcount;
    private int totalblockedcount;

    public ArrayList<TestSuite> testSuites;

    /**
     * Constructs the TestExecution Object
     */
    public TestExecution(final String user, final String host,
            final String osversion, final String language,
            final String screenresolution, final String timestamp,
            final String duration, final String type,
            final int totalerrorcount, final int totalwarningcount,
            final int totalsuccesscount, final int totalfailedcount,
            final int totalblockedcount) {
        super();
        this.user = user;
        this.host = host;
        this.osversion = osversion;
        this.language = language;
        this.screenresolution = screenresolution;
        this.timestamp = timestamp;
        this.result = "Success";
        this.duration = duration;
        this.type = type;
        this.totalerrorcount = totalerrorcount;
        this.totalwarningcount = totalwarningcount;
        this.totalsuccesscount = totalsuccesscount;
        this.totalfailedcount = totalfailedcount;
        this.totalblockedcount = totalblockedcount;

        testSuites = new ArrayList<TestSuite>();
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getOsversion() {
        return osversion;
    }

    public String getLanguage() {
        return language;
    }

    public String getScreenresolution() {
        return screenresolution;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getResult() {
        return result;
    }

    public String getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }

    public int getTotalerrorcount() {
        return totalerrorcount;
    }

    public int getTotalwarningcount() {
        return totalwarningcount;
    }

    public int getTotalsuccesscount() {
        return totalsuccesscount;
    }

    public int getTotalfailedcount() {
        return totalfailedcount;
    }

    public int getTotalblockedcount() {
        return totalblockedcount;
    }

    public List<TestSuite> getTestSuites() {
        return testSuites;
    }

    public void setTotalerrorcount(final int totalerrorcount) {

        this.totalerrorcount += totalerrorcount;
    }

    public void setTotalwarningcount(final int totalwarningcount) {
        this.totalwarningcount += totalwarningcount;
    }

    public void setTotalsuccesscount(final int totalsuccesscount) {
        this.totalsuccesscount += totalsuccesscount;
    }

    public void setTotalfailedcount(final int totalfailedcount) {
        this.totalfailedcount += totalfailedcount;
    }

    public void setTotalblockedcount(final int totalblockedcount) {
        this.totalblockedcount += totalblockedcount;
    }

}
