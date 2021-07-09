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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.OutputStream;
import java.util.Map;

/**
 * A unit test class for Mime module MultipartDataSource class functions.
 */
public class MultipartDataSourceTest {

    Environment env = Mockito.mock(Environment.class);
    BObject entityStruct = Mockito.mock(BObject.class);
    String boundaryString = "e3a0b9ad7b4e7cdt";

    MultipartDataSource multipartDataSource = new MultipartDataSource(env, entityStruct, boundaryString);

    @Test
    public void testStringValue() {
        BLink parent = Mockito.mock(BLink.class);
        String returnVal = multipartDataSource.stringValue(parent);
        Assert.assertNull(returnVal);
    }

    @Test
    public void testExpressionStringValue() {
        BLink parent = Mockito.mock(BLink.class);
        String returnVal = multipartDataSource.expressionStringValue(parent);
        Assert.assertNull(returnVal);
    }

    @Test
    public void testGetType() {
        Type returnVal = multipartDataSource.getType();
        Assert.assertNull(returnVal);
    }

    @Test
    public void testCopy() {
        Map<Object, Object> refs = Mockito.mock(Map.class);
        Object returnVal = multipartDataSource.copy(refs);
        Assert.assertNull(returnVal);
    }

    @Test (expectedExceptions = UnsupportedOperationException.class)
    public void testFrozenCopy() {
        Map<Object, Object> refs = Mockito.mock(Map.class);
        multipartDataSource.frozenCopy(refs);
    }

    @Test
    public void testSerializeWithOutChildParts() {
        OutputStream outputStream = Mockito.mock(OutputStream.class);
        multipartDataSource.serialize(outputStream);
    }

}
