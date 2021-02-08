// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/jballerina.java;

# Represents the type of the record which returned from the byteStream.next() call.
# 
# + value - The array of byte
type StreamEntry record {|
    byte[] value;
|};

# `ByteStream` used to initialize a stream of type byte[]. This `ByteStream` refers to the stream that embedded to
# the I/O byte channels.
class ByteStream {

    private Entity entity;
    private int arraySize;
    private boolean isClosed = false;

    # Initialize a `ByteStream` using a `mime:Entity`.
    #
    # + entity - The `mime:Entity` which contains the byte stream
    # + arraySize - The size of a byte array as an integer
    public isolated function init(Entity entity, int arraySize) {
        self.entity = entity;
        self.arraySize = arraySize;
    }

    # The next function reads and return the next `byte[]` of the related stream.
    #
    # + return - A `byte[]` when the stream is avaliable, null if the stream has reached the end or else an `io:Error`
    public isolated function next() returns record {|byte[] value;|}|io:Error? {
        return externGetStreamEntryRecord(self.entity, self.arraySize);
    }

    # Close the stream. The primary usage of this function is to close the stream without reaching the end.
    # If the stream reaches the end, the byteStream.next() will automatically close the stream.
    #
    # + return - Returns null when the closing was successful or an `io:Error`
    public isolated function close() returns io:Error? {
        if (!self.isClosed) {   
            var closeResult = externCloseInputStream(self.entity);
            if (closeResult is ()) {
                self.isClosed = true;
            }
            return closeResult;
        }
        return ();
    }
}

isolated function externGetStreamEntryRecord(Entity entity, int arraySize) returns record {|byte[] value;|}|io:Error? = 
@java:Method {
    'class: "org.ballerinalang.mime.nativeimpl.MimeEntityBody",
    name: "getStreamEntryRecord"
} external;

isolated function externCloseInputStream(Entity entity) returns io:Error? = @java:Method {
    'class: "org.ballerinalang.mime.nativeimpl.MimeEntityBody",
    name: "closeInputByteStream"
} external;
