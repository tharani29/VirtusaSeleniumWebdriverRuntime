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

public class TestSuite {

    private String testsuitename;
    private String iterationcount;
    private String maxchildren;
    private String result;
    private String duration;
    private String type;
    private String rid;

    public ArrayList<TestCase> testCases;

    /**
     * @param testcasename
     * @param iterationcount
     * @param maxchildren
     * @param result
     * @param duration
     * @param type
     * @param rid
     */
    public TestSuite(final String testcasename, final String iterationcount,
            final String maxchildren, final String duration, final String type,
            final String rid) {
        super();
        this.testsuitename = testcasename;
        this.iterationcount = iterationcount;
        this.maxchildren = maxchildren;
        this.result = "Success";
        this.duration = duration;
        this.type = type;
        this.rid = rid;

        testCases = new ArrayList<TestCase>();
    }

    public String getTestsuitename() {
        return testsuitename;
    }

    public String getIterationcount() {
        return iterationcount;
    }

    public String getMaxchildren() {
        return maxchildren;
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

    public String getRid() {
        return rid;
    }

    public ArrayList<TestCase> getTestCases() {
        return testCases;
    }

    public void setResult(final String result) {
        this.result = result;
    }

}
