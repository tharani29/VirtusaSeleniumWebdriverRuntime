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
package com.virtusa.isq.vtaf.OMHandler;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ObjectMapParser extends DefaultHandler {
    private ObjectMap testDataTable;

    private int columnIndex = -1;
    private int rowIndex = -1;
    private boolean isValue = false;
    private static String parent = "";

    private static ObjectMap objectMap = new ObjectMap();

    public ObjectMapParser() {
    }

    public static HashMap<String, String> parseObjectMap(final File file)
            throws RuntimeException {
        ObjectMapParser obm = new ObjectMapParser();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(file, obm);
        } catch (Exception x) {
            throw new RuntimeException("Error parsing file ("
                    + file.getAbsolutePath() + ")", x);
        }
        return ObjectMapParser.objectMap.getObjectMap();
    }

    @Override
    public void startElement(final String uri, final String localName,
            final String qName, final Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("Page")) {

            objectMap.getObjectMap().put(attributes.getValue("name"),
                    attributes.getValue("url"));
            parent = attributes.getValue("name");
        } else if (qName.equalsIgnoreCase("Object")) {
            objectMap.getObjectMap().put(
                    parent + "." + attributes.getValue("name"),
                    attributes.getValue("searchPath"));
        }

    }

    @Override
    public void endElement(final String uri, final String localName,
            final String qName) throws SAXException {
    }

    @Override
    public void characters(final char ch[], final int start, final int length)
            throws SAXException {

    }

}
