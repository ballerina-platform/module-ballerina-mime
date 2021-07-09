/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
 */

package io.ballerina.stdlib.mime.util;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A unit test class for Mime module EntityHeaderHandler class functions.
 */
public class EntityHeaderHandlerTest {

    @Test
    public void testGetHeaderValueWithNullEntity() {
        BObject entity = Mockito.mock(BObject.class);
        String headerName = "testHeaderName";
        String returnVal = EntityHeaderHandler.getHeaderValue(entity, headerName);
        Assert.assertNull(returnVal);
    }

    @Test
    public void testGetEntityHeaderMapWithNullEntity() {
        BObject entity = Mockito.mock(BObject.class);
        BMap<BString, Object> returnVal = EntityHeaderHandler.getEntityHeaderMap(entity);
        BMap<BString, Object> actualHttpHeaders = EntityHeaderHandler.getNewHeaderMap();
        Assert.assertEquals(returnVal, actualHttpHeaders);
    }

    @Test
    public void testAddHeaderWithNullEntity() {
        BObject entity = Mockito.mock(BObject.class);
        BMap<BString, Object> headers = Mockito.mock(BMap.class);
        String key = "testKey";
        String value = "testValue";
        EntityHeaderHandler.addHeader(entity, headers, key, value);
    }

}
