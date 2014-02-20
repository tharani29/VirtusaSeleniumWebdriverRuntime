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

public class TestCase {

    private String modulename;
    private String moduletype;
    private String result;
    private String duration;
    private String type;
    private String rid;

    public ArrayList<TestStep> testSteps;

    /**
     * @param modulename
     * @param moduletype
     * @param result
     * @param duration
     * @param type
     * @param rid
     */
    public TestCase(final String modulename, final String moduletype,
            final String duration, final String type, final String rid) {
        super();
        this.modulename = modulename;
        this.moduletype = moduletype;
        this.result = "Success";
        this.duration = duration;
        this.type = type;
        this.rid = rid;

        testSteps = new ArrayList<TestStep>();
    }

    public String getModulename() {
        return modulename;
    }

    public String getModuletype() {
        return moduletype;
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

    public void setResult(final String result) {

        this.result = result;
    }

}
