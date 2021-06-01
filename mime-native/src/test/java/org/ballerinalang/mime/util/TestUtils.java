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

import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStreamingJson;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Strand;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * A test utils for mime utils unit test classes.
 */
public class TestUtils {

    static BObject getNullBObject() {
        return new BObject() {
            @Override
            public Object call(Strand strand, String s, Object... objects) {
                return null;
            }

            @Override
            public BFuture start(Strand strand, String s, Object... objects) {
                return null;
            }

            @Override
            public Object copy(Map<Object, Object> map) {
                return null;
            }

            @Override
            public Object frozenCopy(Map<Object, Object> map) {
                return null;
            }

            @Override
            public String stringValue(BLink bLink) {
                return null;
            }

            @Override
            public String expressionStringValue(BLink bLink) {
                return null;
            }

            @Override
            public ObjectType getType() {
                return null;
            }

            @Override
            public Object get(BString bString) {
                return null;
            }

            @Override
            public long getIntValue(BString bString) {
                return 0;
            }

            @Override
            public double getFloatValue(BString bString) {
                return 0;
            }

            @Override
            public BString getStringValue(BString bString) {
                return null;
            }

            @Override
            public boolean getBooleanValue(BString bString) {
                return false;
            }

            @Override
            public BMap getMapValue(BString bString) {
                return null;
            }

            @Override
            public BObject getObjectValue(BString bString) {
                return null;
            }

            @Override
            public BArray getArrayValue(BString bString) {
                return null;
            }

            @Override
            public void addNativeData(String s, Object o) {

            }

            @Override
            public Object getNativeData(String s) {
                return null;
            }

            @Override
            public HashMap<String, Object> getNativeData() {
                return null;
            }

            @Override
            public void set(BString bString, Object o) {

            }
        };
    }

    static BStreamingJson getNullBStreamingJson() {
        return new BStreamingJson() {
            @Override
            public void serialize(Writer writer) {

            }

            @Override
            public Object get(long l) {
                return null;
            }

            @Override
            public Object getRefValue(long l) {
                return null;
            }

            @Override
            public Object fillAndGetRefValue(long l) {
                return null;
            }

            @Override
            public long getInt(long l) {
                return 0;
            }

            @Override
            public boolean getBoolean(long l) {
                return false;
            }

            @Override
            public byte getByte(long l) {
                return 0;
            }

            @Override
            public double getFloat(long l) {
                return 0;
            }

            @Override
            public String getString(long l) {
                return null;
            }

            @Override
            public BString getBString(long l) {
                return null;
            }

            @Override
            public void add(long l, Object o) {

            }

            @Override
            public void add(long l, long l1) {

            }

            @Override
            public void add(long l, boolean b) {

            }

            @Override
            public void add(long l, byte b) {

            }

            @Override
            public void add(long l, double v) {

            }

            @Override
            public void add(long l, String s) {

            }

            @Override
            public void add(long l, BString bString) {

            }

            @Override
            public void append(Object o) {

            }

            @Override
            public Object reverse() {
                return null;
            }

            @Override
            public Object shift() {
                return null;
            }

            @Override
            public Object shift(long l) {
                return null;
            }

            @Override
            public void unshift(Object[] objects) {

            }

            @Override
            public Object[] getValues() {
                return new Object[0];
            }

            @Override
            public byte[] getBytes() {
                return new byte[0];
            }

            @Override
            public String[] getStringArray() {
                return new String[0];
            }

            @Override
            public long[] getIntArray() {
                return new long[0];
            }

            @Override
            public boolean[] getBooleanArray() {
                return new boolean[0];
            }

            @Override
            public byte[] getByteArray() {
                return new byte[0];
            }

            @Override
            public double[] getFloatArray() {
                return new double[0];
            }

            @Override
            public Type getElementType() {
                return null;
            }

            @Override
            public Type getIteratorNextReturnType() {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public BArray slice(long l, long l1) {
                return null;
            }

            @Override
            public void setLength(long l) {

            }

            @Override
            public long getLength() {
                return 0;
            }

            @Override
            public BIterator<?> getIterator() {
                return null;
            }

            @Override
            public Object copy(Map<Object, Object> map) {
                return null;
            }

            @Override
            public Object frozenCopy(Map<Object, Object> map) {
                return null;
            }

            @Override
            public String stringValue(BLink bLink) {
                return null;
            }

            @Override
            public String expressionStringValue(BLink bLink) {
                return null;
            }

            @Override
            public Type getType() {
                return null;
            }
        };
    }

}
