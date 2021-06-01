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

package org.ballerinalang.mime.util;

import io.ballerina.runtime.api.values.BObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A unit test class for Mime module MimeUtil class functions.
 */
public class MimeUtilTest {

    @Test
    public void testIsValidContentTypeWithCorrectContentType() {
        String contentType = "application/test+xml; charset=utf-8";
        boolean returnVal = MimeUtil.isValidateContentType(contentType);
        Assert.assertTrue(returnVal);
    }

    @Test
    public void testIsValidContentTypeWithIncorrectContentType() {
        String contentType = "testContentType";
        boolean returnVal = MimeUtil.isValidateContentType(contentType);
        Assert.assertFalse(returnVal);
    }

    @Test
    public void testGetBaseTypeWithNullEntity() {
        BObject entity = TestUtils.getNullBObject();
        String returnVal = MimeUtil.getBaseType(entity);
        Assert.assertNull(returnVal);
    }

    @Test
    public void testGetContentTypeWithParametersWithNullEntity() {
        BObject entity = TestUtils.getNullBObject();
        String returnVal = MimeUtil.getContentTypeWithParameters(entity);
        Assert.assertNull(returnVal);
    }

    @Test
    public void testGenerateAsJSONWithBStreamingJsonInstance() {
        Object value = TestUtils.getNullBStreamingJson();
        BObject entity = TestUtils.getNullBObject();
        boolean returnVal = MimeUtil.generateAsJSON(value, entity);
        Assert.assertFalse(returnVal);
    }

    @Test
    public void testGenerateAsJSONWithNullEntity() {
        Object value = new Object();
        BObject entity = TestUtils.getNullBObject();
        boolean returnVal = MimeUtil.generateAsJSON(value, entity);
        Assert.assertFalse(returnVal);
    }

}
