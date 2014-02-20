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

package com.virtusa.isq.vtaf.report.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.virtusa.isq.vtaf.report.reporter.ReportBuilder;

/**
 * Contains the generator routines that generates code based on the
 * StringTemplate files
 * 
 * @author cmendis
 * 
 */
public class Generator {

    public Generator() {
    }

    public void generateReport(final ReportBuilder reportBuilder) {
        // String templateFolderRoot = "";

        String targetHtmlDataFile =
                reportBuilder.getReportFolderLocation() + File.separator
                        + "report.html.data";
        try {
            createContent("report", reportBuilder,
                    getTemplateStringReader("ReportHtmlData.stg"),
                    targetHtmlDataFile);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 
     * Evaluates the Content() template function in the given template file and
     * saves to the file returned by the FileName() template function passing
     * the settings and the object passed.
     * 
     * @param map
     * @param scriptPropertyName
     * @throws VTAFException
     */

    private void createContent(final String varName, final Object objectToPass,
            final StringReader reader, final String outputFileName)
            throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        // map.put("settings", settings);
        map.put(varName, objectToPass);

        StringTemplateGroup group = new StringTemplateGroup(reader);
        StringTemplate contentTemplate = group.getInstanceOf("Content");
        contentTemplate.setAttributes(map);

        String outputContent = null;
        try {
            outputContent = contentTemplate.toString();
        } catch (IllegalArgumentException e) {

            if ((e.getMessage()).startsWith("Can't find template")) {
                String msg = e.getMessage();
                String newmsg =
                        msg.substring(("Can't find template").length(),
                                msg.indexOf(".st;"));
                System.out.println(newmsg);
                throw new RuntimeException("Error writing file "
                        + outputFileName + ": Command '" + newmsg
                        + "' not supported!", e);
                // java.lang.IllegalArgumentException: Can't find template
                // Retrieve.st; context is [Content BusinessComponent
                // else_subtemplate TestCommand
                // if(testCommand.specialCommand)_subtemplate]; group hierarchy
                // is [Library]
            } else {
                throw new RuntimeException("Error writing file "
                        + outputFileName, e);
            }
        }

        String folderName =
                outputFileName.substring(0, outputFileName.lastIndexOf('.')); // strip
        // the
        // text
        folderName =
                folderName.substring(0, folderName.lastIndexOf(File.separator)); // strip
        // the
        // filename

        if (folderName != null) {
            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }

        FileWriter out;
        try {
            out = new FileWriter(outputFileName);
            out.write(outputContent);
            out.close();
        } catch (Exception e) {
            // if (e != null && e.getMessage() != null) {
            // //logger.error(e.getMessage(), e);
            // // DialogManager.showErrorText("Cannnot write to file \n"
            // // + outputFileName);
            // } else {
            // // logger.error("Cannnot write to file \n" + outputFileName);
            // // DialogManager.showErrorText("Cannnot write to file \n"
            // // + outputFileName);
            // }
            throw new RuntimeException("Error writing file " + outputFileName,
                    e);
        }

    }

    // here get the templates from the inside package and put them to input
    // stream and then put it to sting reader
    private StringReader getTemplateStringReader(final String filePath) {

        InputStream stream = null;
        stream = Generator.class.getResourceAsStream(filePath);

        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        try {
            for (int n; (n = stream.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
        } catch (IOException e) {
            if (e != null && e.getMessage() != null) {
                // logger.error(e.getMessage(), e);
                // DialogManager.showErrorText(e.getMessage());
            } else {
                // logger.error("Error in VTAF: Error occured in templates");
                // DialogManager
                // .showErrorText("VTAF Error: Error occured in templates");
            }
            System.out.println("Error occured in templates");
        }
        return new StringReader(out.toString());
    }

}
