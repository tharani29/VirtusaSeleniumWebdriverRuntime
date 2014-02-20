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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A test data table allows a table of test data to be maintained and referenced
 * 
 * @author cmendis
 * 
 */
public class DataTable {

    private ArrayList<HashMap<String, String>> table =
            new ArrayList<HashMap<String, String>>();

    private ArrayList<String> columns = new ArrayList<String>();

    public void addColumn(final String column) {
        this.columns.add(column);
    }

    public void addColumn(final int index, final String column) {
        this.columns.add(index, column);
    }

    public void addRowAt(final int rowIndex) {
        this.table.add(rowIndex, new HashMap<String, String>());
    }

    public String get(final int row, final String column) {
        return table.get(row).get(column);
    }

    public String get(final int row, final int col) {
        String column = columns.get(col);
        return get(row, column);
    }

    public void setValue(final int row, final int column, final String value) {
        this.setValue(row, columns.get(column), value);
    }

    // Note: Assumes that sequential addition row-wise. row is 0-indexed
    public void setValue(final int row, final String column, final String value) {
        HashMap<String, String> map;
        if (table.size() < row + 1) { // need to add a row
            map = new HashMap<String, String>();
            table.add(map);
        } else {
            map = table.get(row);
        }
        map.put(column, value);
    }

    public int getRowCount() {
        return table.size();
    }

    public int getcolCount() {
        return this.columns.size();
    }

    public ArrayList<HashMap<String, String>> getRows() {
        return table;
    }

    public List<String> getColumns() {
        return this.columns;
    }

    public int intValue(final int row, final String column) {
        return Integer.valueOf(this.get(row, column));
    }

    public String stringValue(final int row, final String column) {
        return (this.get(row, column));
    }

}
