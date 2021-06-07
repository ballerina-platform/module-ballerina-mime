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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStreamingJson;
import io.ballerina.runtime.api.values.BString;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.DEFAULT_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.DEFAULT_SUB_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PARAMETER_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PRIMARY_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUBTYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUFFIX_FIELD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
        BObject entity = mock(BObject.class);
        String returnVal = MimeUtil.getBaseType(entity);
        Assert.assertNull(returnVal);
    }

    @Test
    public void testGetContentTypeWithParametersWithNullEntity() {
        BObject entity = mock(BObject.class);
        String returnVal = MimeUtil.getContentTypeWithParameters(entity);
        Assert.assertNull(returnVal);
    }

    @Test
    public void testGenerateAsJSONWithBStreamingJsonInstance() {
        Object value = mock(BStreamingJson.class);
        BObject entity = mock(BObject.class);
        boolean returnVal = MimeUtil.generateAsJSON(value, entity);
        Assert.assertFalse(returnVal);
    }

    @Test
    public void testGenerateAsJSONWithNullEntity() {
        Object value = mock(Object.class);
        BObject entity = mock(BObject.class);
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
        BObject bodyPart = mock(BObject.class);
        BObject mediaType = mock(BObject.class);
        BObject field = mock(BObject.class);
        when(field.toString()).thenReturn("testField");
        when(mediaType.get(PRIMARY_TYPE_FIELD)).thenReturn(field);
        when(mediaType.get(SUBTYPE_FIELD)).thenReturn(field);
        when(bodyPart.get(MEDIA_TYPE_FIELD)).thenReturn(mediaType);
        boolean returnVal = MimeUtil.isNestedPartsAvailable(bodyPart);
        Assert.assertFalse(returnVal);
    }

    @Test
    public void testIsJsonCompatibleWithInCompatibleTag() {
        Type type = mock(Type.class);
        when(type.getTag()).thenReturn(TypeTags.BYTE_TAG);
        boolean returnVal = MimeUtil.isJSONCompatible(type);
        Assert.assertFalse(returnVal);
    }

    @Test
    public void testIsJsonCompatibleWithArrayTag() {
        // When Element type is compatible
        ArrayType arrayType = mock(ArrayType.class);
        Type type = mock(Type.class);
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

    @Test
    public void testGetContentDisposition() {
        BObject entity =  mock(BObject.class);
        BObject contentDispositionField =  mock(BObject.class);
        when(entity.get(CONTENT_DISPOSITION_FIELD)).thenReturn(contentDispositionField);
        String returnVal = MimeUtil.getContentDisposition(entity);
        Assert.assertEquals(returnVal, "");

        when(contentDispositionField.get(DISPOSITION_FIELD)).thenReturn("disposition");
        returnVal = MimeUtil.getContentDisposition(entity);
        Assert.assertEquals(returnVal, "disposition");
    }

    @Test
    public void testGetContentDispositionWithMultipartFormData() {
        BObject entity = mock(BObject.class);
        BObject contentDispositionField = mock(BObject.class);
        when(entity.get(CONTENT_DISPOSITION_FIELD)).thenReturn(contentDispositionField);
        BObject mediaTypeField = mock(BObject.class);
        when(entity.get(MEDIA_TYPE_FIELD)).thenReturn(mediaTypeField);
        when(mediaTypeField.get(PRIMARY_TYPE_FIELD)).thenReturn("multipart");
        when(mediaTypeField.get(SUBTYPE_FIELD)).thenReturn("form-data");
        String returnVal = MimeUtil.getContentDisposition(entity);
        Assert.assertEquals(returnVal, "form-data");
    }

    @Test
    public void testSetContentTypeWithNullObjects() {
        BObject mediaType = mock(BObject.class);
        BObject entityStruct = mock(BObject.class);
        String contentType = null;
        BMap<BString, Object> parameterMap =
                io.ballerina.runtime.api.creators.ValueCreator.createMapValue(
                        TypeCreator.createMapType(PredefinedTypes.TYPE_STRING));
        BString suffix, primaryType, subType;
        primaryType = suffix = subType = PredefinedTypes.TYPE_STRING.getZeroValue();
        MimeUtil.setContentType(mediaType, entityStruct, contentType);
        verify(mediaType, times(1)).set(PRIMARY_TYPE_FIELD, primaryType);
        verify(mediaType, times(1)).set(SUBTYPE_FIELD, suffix);
        verify(mediaType, times(1)).set(SUFFIX_FIELD, subType);
        verify(mediaType, times(1)).set(PARAMETER_MAP_FIELD, parameterMap);
        verify(mediaType, times(1)).set(PRIMARY_TYPE_FIELD, DEFAULT_PRIMARY_TYPE);
        verify(mediaType, times(1)).set(SUBTYPE_FIELD, DEFAULT_SUB_TYPE);
    }

}
