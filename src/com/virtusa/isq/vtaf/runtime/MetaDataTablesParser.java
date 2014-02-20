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

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MetaDataTablesParser extends DefaultHandler {
    private DataTable testDataTable;

    private int columnIndex = -1;
    private int rowIndex = -1;
    private boolean isValue = false;

    private static HashMap<String, DataTable> tables =
            new HashMap<String, DataTable>();

    public MetaDataTablesParser() {
    }

    public static HashMap<String, DataTable> parseTables(final File file)
            throws RuntimeException {
        MetaDataTablesParser dtp = new MetaDataTablesParser();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(file, dtp);
        } catch (Exception x) {
            throw new RuntimeException("Error parsing file ("
                    + file.getAbsolutePath() + ")", x);
        }

        return dtp.getTables();

    }

    public HashMap<String, DataTable> getTables() {
        return tables;
    }

    // private String page;
    @Override
    public void startElement(final String uri, final String localName,
            final String qName, final Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("TABLE")) {

            testDataTable = new DataTable();
            tables.put(attributes.getValue("name"), testDataTable);
            rowIndex = -1;
        } else if (qName.equalsIgnoreCase("COLUMN")) {
            testDataTable.addColumn(attributes.getValue("name"));
        } else if (qName.equalsIgnoreCase("ROW")) {
            rowIndex++;
            columnIndex = -1;
        } else if (qName.equalsIgnoreCase("VALUE")) {
            columnIndex++;
            isValue = true;
        }

    }

    @Override
    public void endElement(final String uri, final String localName,
            final String qName) throws SAXException {

        if (qName.equalsIgnoreCase("COLUMN")) {
        } else if (qName.equalsIgnoreCase("VALUE")) {
            isValue = false;
        } else if (qName.equalsIgnoreCase("TABLE")) {
        }

    }

    @Override
    public void characters(final char ch[], final int start, final int length)
            throws SAXException {
        String value = new String(ch, start, length);
        if (isValue) {
            testDataTable.setValue(rowIndex, columnIndex, value);
        }
    }

}
