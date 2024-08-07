// Copyright (c) 2024 WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/jballerina.java;
import ballerina/log;

class EventStreamWriter {
    stream<byte[], error?> eventStream;

    isolated function init(stream<byte[], error?> eventStream) {
        self.eventStream = eventStream;
    }

    isolated function writeEventStream() {
        do {
            while true {
                record {byte[] value;}? event = check self.eventStream.next();
                if event is () {
                    self.closeEventStream();
                    return;
                }
                check externWriteEventStreamBytesToOutputStream(self, event.value);
            }
        } on fail error err {
            log:printError("unable to write event stream to wire", err);
            self.closeEventStream();
            return;
        }
    }

    isolated function closeEventStream() {
        error? result = self.eventStream.close();
        if result is error {
            log:printError("unable to close the stream", result);
        }
    }
}

isolated function externWriteEventStreamBytesToOutputStream(EventStreamWriter eventStreamWriter, byte[] bytes)
returns error? = @java:Method {
    'class: "io.ballerina.stdlib.mime.nativeimpl.MimeEntityBody",
    name: "writeEventStreamBytesToOutputStream"
} external;
