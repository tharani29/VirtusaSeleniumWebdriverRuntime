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

public class TestStep {

    private String time;
    private String category;
    private String errimg;
    private String errthumb;
    private String message;
    private String stacktrace;
    private String codefile;
    private String codeline;
    private String loglvl;
    private boolean isPassed;

    /**
     * Success Test Step
     */
    public TestStep(final boolean isPassed, final String time,
            final String category, final String message, final String codefile,
            final String codeline, final String loglvl) {
        super();
        this.isPassed = isPassed;
        this.time = time;
        this.category = category;
        this.message = message;
        this.codefile = codefile;
        this.codeline = codeline;
        this.loglvl = loglvl;
    }

    /**
     * Failed Test Step
     */
    public TestStep(final boolean isPassed, final String time,
            final String category, final String errimg, final String errthumb,
            final String message, final String stacktrace,
            final String codefile, final String codeline, final String loglvl) {
        super();
        this.isPassed = isPassed;
        this.time = time;
        this.category = category;
        this.errimg = errimg;
        this.errthumb = errthumb;
        this.message = message;
        this.stacktrace = stacktrace;
        this.codefile = codefile;
        this.codeline = codeline;
        this.loglvl = loglvl;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public String getTime() {
        return time;
    }

    public String getCategory() {
        return category;
    }

    public String getErrimg() {
        return errimg;
    }

    public String getErrthumb() {
        return errthumb;
    }

    public String getMessage() {
        return message;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public String getCodefile() {
        return codefile;
    }

    public String getCodeline() {
        return codeline;
    }

    public String getLoglvl() {
        return loglvl;
    }

}
