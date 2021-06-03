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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.channels.WritableByteChannel;

/**
 * A unit test class for Mime module EntityWrapper class functions.
 */
public class EntityWrapperTest {

    @Test
    public void testTransfer() {
        EntityWrapper entityWrapper = Mockito.mock(EntityWrapper.class);
        int position = 0;
        int count = 10;
        WritableByteChannel dstChannel = Mockito.mock(WritableByteChannel.class);
        entityWrapper.transfer(position, count, dstChannel);
    }

    @Test
    public void testGetChannel() {
        EntityWrapper entityWrapper = Mockito.mock(EntityWrapper.class);
        entityWrapper.getChannel();
    }

    @Test
    public void testRemaining() {
        EntityWrapper entityWrapper = Mockito.mock(EntityWrapper.class);
        boolean returnValue = entityWrapper.remaining();
        Assert.assertFalse(returnValue);
    }

}
