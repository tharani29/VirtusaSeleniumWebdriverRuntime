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
*http://www.apache.org/licenses/LICENSE-2.0
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import com.virtusa.VTAF.reporter.reader.ReportBase;
import com.virtusa.isq.vtaf.runtime.DataTable;
import com.virtusa.isq.vtaf.runtime.DataTablesParser;
import com.virtusa.isq.vtaf.runtime.MetaDataTablesParser;

public class VTAFTestListener implements IInvokedMethodListener {

    public ReportBase reporter = new ReportBase();
    static String browser = "";
    public int dataIteration = 0;
    public HashMap<String, DataTable> tables = null;
    List tableHeaders = new ArrayList();
    String prevDataProvider = "";

    // @Override
    public void onTestFailure(final ITestResult tr) {

        System.out.println(tr.getTestName()
                + "99999999999999999999999999999999999999999999999: FAILURE");
        endTestReporting("skipped");

    }

    // @Override

    public void onTestSkipped(final ITestResult tr) {
        System.out
                .println("99999999999999999999999999999999999999999999999: SKIPPED");
        endTestReporting("failed");

    }

    // @Override
    public void onTestSuccess(final ITestResult tr) {
        System.out
                .println("99999999999999999999999999999999999999999999999: SUCCESS");
        endTestReporting("passed");

    }

    public void endTestReporting(final String testFailed) {

    }

    @Override
    public void afterInvocation(final IInvokedMethod method,
            final ITestResult result) {
        if (method.isTestMethod()) {
            if (result.getStatus() == ITestResult.SKIP) {
                System.out
                        .println("99999999999999999999999999999999999999999999999"
                                + method.toString() + ": SKIPPED");
                endTestReporting("skipped");
            } else if (result.getStatus() == ITestResult.FAILURE) {
                System.out
                        .println("99999999999999999999999999999999999999999999999"
                                + method.toString() + ": FAILURE");
                endTestReporting("failed");
            } else if (result.getStatus() == ITestResult.SUCCESS) {
                System.out
                        .println("99999999999999999999999999999999999999999999999"
                                + method.toString() + ": SUCCESS");
                endTestReporting("passed");
            }

        }

    }

    @Override
    public void beforeInvocation(final IInvokedMethod methodtest,
            final ITestResult result) {

        if (methodtest.isTestMethod()) {
            String dataString = "";
            String dataProvider = "";

            Method method =
                    methodtest.getTestMethod().getConstructorOrMethod()
                            .getMethod();
            Annotation testAnnot[] = method.getAnnotations();
            for (Annotation annot : testAnnot) {

                if (annot instanceof Test) {

                    Test tAnnot = (Test) annot;
                    System.out
                            .println("Test annot 66666666666666666666666666666666   "
                                    + tAnnot.getClass().getName());
                    dataProvider = tAnnot.dataProvider();
                    if (prevDataProvider.equalsIgnoreCase("")
                            || !(prevDataProvider
                                    .equalsIgnoreCase(dataProvider))) {
                        dataIteration = 0;
                        prevDataProvider = dataProvider;

                    }

                }

            }
            if (dataProvider.indexOf(method.getName()) > -1) {
                Object[][] dataset =
                        getDataTabaleMeta(dataProvider.substring(0,
                                dataProvider.indexOf("_" + method.getName())));

                System.out.println("Length=" + dataset.length
                        + " | DataIteration =" + dataIteration);

                if (dataset.length > dataIteration) {

                    for (int columns = 0; columns < dataset[dataIteration].length; columns++) {

                        dataString =
                                dataString + " |  " + tableHeaders.get(columns)
                                        + "=" + dataset[dataIteration][columns]
                                        + "";

                    }
                }
            }
            System.out.println("Invoking method dataset |="
                    + method.getDeclaringClass().getCanonicalName() + "88888="
                    + method.getName() + "88888888=" + browser + "88888888="
                    + dataString + "  Data iteration =" + dataIteration);
            dataIteration++;

        }

    }

    public Object[][] getDataTabale(final String tableName) {
        DataTable table = getTable(tableName);

        Object[][] retObjArr = this.getTableArray(table);
        return (retObjArr);
    }

    public Object[][] getDataTabaleMeta(final String tableName) {
        DataTable table = getTableMeta(tableName);
        tableHeaders = table.getColumns();
        Object[][] retObjArr = this.getTableArray(table);
        return (retObjArr);
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

                file = new File("data" + File.separator + "DataTables.xml");
                System.out.println("Location of data file is :"
                        + file.getAbsolutePath());
            } else {

                file =
                        new File("grid" + File.separator
                                + "selenium-grid-1.0.6" + File.separator
                                + "data" + File.separator + "DataTables.xml");
                System.out.println("Location of data file is :"
                        + file.getAbsolutePath());
            }
            tables = DataTablesParser.parseTables(file);
        }
        return tables.get(name);
    }

    /**
     * Retrieve the data table for the parameterized executin
     * */
    public DataTable getTableMeta(final String name) {
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
            tables = MetaDataTablesParser.parseTables(file);
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
                System.out.print(table.get(row, col) + "|");
            }
            System.out.println();

        }

        return tabArray;

    }

}
