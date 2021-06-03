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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PRIMARY_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUBTYPE_FIELD;
import static org.mockito.Mockito.when;

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

    @Test
    public void testGetNewMultipartDelimiter() {
        String returnVal = MimeUtil.getNewMultipartDelimiter();
        Assert.assertNotNull(returnVal);
    }

    @Test
    public void testIsNestedPartsAvailableWithoutBodyParts() {
        BObject bodyPart = Mockito.mock(BObject.class);
        BObject mediaType = Mockito.mock(BObject.class);
        BObject field = Mockito.mock(BObject.class);
        when(field.toString()).thenReturn("testField");
        when(mediaType.get(PRIMARY_TYPE_FIELD)).thenReturn(field);
        when(mediaType.get(SUBTYPE_FIELD)).thenReturn(field);
        when(bodyPart.get(MEDIA_TYPE_FIELD)).thenReturn(mediaType);
        boolean returnVal = MimeUtil.isNestedPartsAvailable(bodyPart);
        Assert.assertFalse(returnVal);
    }

    @Test
    public void testIsJsonCompatibleWithInCompatibleTag() {
        Type type = Mockito.mock(Type.class);
        when(type.getTag()).thenReturn(TypeTags.BYTE_TAG);
        boolean returnVal = MimeUtil.isJSONCompatible(type);
        Assert.assertFalse(returnVal);
    }

    @Test
    public void testIsJsonCompatibleWithArrayTag() {
        // When Element type is compatible
        ArrayType arrayType = Mockito.mock(ArrayType.class);
        Type type = Mockito.mock(Type.class);
        when(type.getTag()).thenReturn(TypeTags.INT_TAG);
        when(arrayType.getTag()).thenReturn(TypeTags.ARRAY_TAG);
        when(arrayType.getElementType()).thenReturn(type);
        boolean returnVal = MimeUtil.isJSONCompatible(arrayType);
        Assert.assertTrue(returnVal);

        // When Element type is not compatible
        when(type.getTag()).thenReturn(TypeTags.BYTE_TAG);
        when(arrayType.getTag()).thenReturn(TypeTags.ARRAY_TAG);
        when(arrayType.getElementType()).thenReturn(type);
        returnVal = MimeUtil.isJSONCompatible(arrayType);
        Assert.assertFalse(returnVal);
    }

}
