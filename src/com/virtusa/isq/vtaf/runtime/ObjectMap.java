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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ObjectMap {

    public static String getObjectSearchPath(final String objectName,
            final String identifire) {
        String searchPath = "";

        try {

            Class<?> clz =
                    Class.forName("com.virtusa.isq.vtaf.runtime.pages."
                            + objectName.split("\\.")[0]);
            /* Use method added in Java 1.5. */
            Object[] consts = clz.getEnumConstants();
            /* Enum constants are in order of declaration. */

            for (int i = 0; i < consts.length; i++) {

                if (consts[i].toString().equalsIgnoreCase(
                        objectName.split("\\.")[1])) {
                    Class<?> sub = consts[i].getClass();
                    Method mth = sub.getDeclaredMethod("getSrachPath");
                    searchPath = (String) mth.invoke(consts[i]);
                    /* Prove it worked. */
                    // System.out.println(val);
                    break;

                }

            }
            if (identifire != "") {
                return getResolvedSearchPath(searchPath, identifire);

            } else {
                return searchPath;
            }
        } catch (Exception e) {

            return "";

        }

    }

    public List<String> getIdentifires(final String objectSearchPath) {

        String str;
        List<String> identifires = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(objectSearchPath, "<");
        while (st.hasMoreElements()) {
            str = st.nextElement().toString();
            if (str.contains(">")) {
                identifires.add(str.split(">")[0]);
            }
        }
        return identifires;
    }

    public static List<String> getParameterValues(final String parameters) {

        List<String> parameterValues = new ArrayList<String>();
        String[] st = parameters.split("_PARAM,");

        for (int i = 0; i < st.length; i++) {

            parameterValues.add(st[i]);

        }
        return parameterValues;

    }

    public static String getResolvedSearchPath(final String searchPath,
            final String identifire) {
        String resolvedSearchPath = searchPath;
        List<String> parameterValues = getParameterValues(identifire);
        for (int i = 0; i < parameterValues.size(); i++) {
            resolvedSearchPath =
                    resolvedSearchPath.replace("<"
                            + parameterValues.get(i).split("_PARAM:")[0] + ">",
                            parameterValues.get(i).split("_PARAM:")[1]);
        }
        return resolvedSearchPath;
    }
}
