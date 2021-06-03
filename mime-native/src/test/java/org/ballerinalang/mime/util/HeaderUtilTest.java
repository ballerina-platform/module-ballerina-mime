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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.activation.MimeTypeParseException;

/**
 * A unit test class for Mime module HeaderUtil class functions.
 */
public class HeaderUtilTest {

    @Test
    public void testGetParamMapWithOutSemiColon() {
        String headerValue = "application/json";
        BMap<BString, Object> returnVal = HeaderUtil.getParamMap(headerValue);
        MapType stringMapType = TypeCreator.createMapType(PredefinedTypes.TYPE_STRING);
        BMap<BString, Object> actual = ValueCreator.createMapValue(stringMapType);
        Assert.assertEquals(returnVal, actual);
    }

    @Test
    public void testGetBaseTypeWithNullEntityStruct() throws MimeTypeParseException {
        BObject entityStruct = Mockito.mock(BObject.class);
        String returnVal = HeaderUtil.getBaseType(entityStruct);
        Assert.assertNull(returnVal);
    }

}
