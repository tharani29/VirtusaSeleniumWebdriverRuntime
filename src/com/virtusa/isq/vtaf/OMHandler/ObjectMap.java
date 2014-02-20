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

import java.util.HashMap;

/**
 * A test data table allows a table of test data to be maintained and referenced
 * 
 * @author damaithC
 * 
 */

public class ObjectMap {
    private static HashMap<String, String> map = new HashMap<String, String>();

    public static void setObject(final String key, final String value) {
        map.put(key, value);
    }

    public static String getObject(final String key) {

        return map.get(key);

    }

    public HashMap<String, String> getObjectMap() {

        return ObjectMap.map;
    }

}
