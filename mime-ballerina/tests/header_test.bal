// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;

//Test whether the correct http header value is returned when the header exist as requested
@test:Config {}
public function testGetHeaderAsIs() {
    string headerName = "Content-Type";
    string headerValue = "application/json";
    string headerNameToBeUsedForRetrieval = "Content-Type";
    string returnVal = testAddHeader(headerName, headerValue, headerNameToBeUsedForRetrieval);
    test:assertEquals(returnVal, headerValue, msg = "Found unexpected output");
}

//Test whether the empty http header value is returned when the header exist
// @test:Config {}
// public function testGetEmptyHeaderValue() {
//     string headerName = "X-Empty-Header";
//     string headerValue = "";
//     string headerNameToBeUsedForRetrieval = "X-Empty-Header";
//     string returnVal = testAddHeader(headerName, headerValue, headerNameToBeUsedForRetrieval);
//     test:assertEquals(returnVal, headerValue, msg = "Found unexpected output");
// }

//Test whether the case is ignored when dealing with http headers
@test:Config {}
public function testCaseInsensitivityOfHeaders() {
    string headerName = "content-type";
    string headerValue = "application/json";
    string headerNameToBeUsedForRetrieval = "ConTeNT-TYpE";
    string returnVal = testAddHeader(headerName, headerValue, headerNameToBeUsedForRetrieval);
    test:assertEquals(returnVal, headerValue, msg = "Found unexpected output");
}

//Test adding multiple headers to entity
@test:Config {}
public function testAddingMultipleHeadersToEntity() {
    Entity entity = new;
    entity.addHeader("header1", "value1");
    entity.addHeader("header2", "value2");
    entity.addHeader("header3", "value3");

    test:assertEquals(entity.getHeader("header1"), "value1", msg = "Found unexpected output");
    test:assertEquals(entity.getHeader("header2"), "value2", msg = "Found unexpected output");
    test:assertEquals(entity.getHeader("header3"), "value3", msg = "Found unexpected output");
}

//Test adding multiple values to same header
@test:Config {}
public function testAddingMultipleValuesToSameHeaderKey() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    var headers = entity.getHeaders("header1");
    if (headers is string[]) {
        test:assertEquals(headers[0], "value1", msg = "Found unexpected output");
        test:assertEquals(headers[1], "value2", msg = "Found unexpected output");
        test:assertEquals(headers[2], "value3", msg = "Found unexpected output");
    }
    var header = entity.getHeader("header2");
    if (header is string) {
        test:assertEquals(header, "value3", msg = "Found unexpected output");
    }
}

//Test set header function
@test:Config {}
public function testSetHeaderWitheHeaders() {
    Entity entity = new;
    entity.setHeader("HeADEr2", "totally different value");
    var headers = entity.getHeaders("header2");
    if (headers is string[]) {
        test:assertEquals(headers[0], "totally different value", msg = "Found unexpected output");
    }
    var header = entity.getHeader("header2");
    if (header is string) {
        test:assertEquals(header, "totally different value", msg = "Found unexpected output");
    }
}

//Test set header after add header
@test:Config {}
public function testSetHeaderAfterAddheader() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    var headers = entity.getHeaders("header1");
    if (headers is string[]) {
        test:assertEquals(headers[0], "value1", msg = "Found unexpected output");
        test:assertEquals(headers[1], "value2", msg = "Found unexpected output");
        test:assertEquals(headers[2], "value3", msg = "Found unexpected output");
    }
    var header = entity.getHeader("header2");
    if (header is string) {
        test:assertEquals(header, "totally different value", msg = "Found unexpected output");
    }
}

//Test add header after set header
@test:Config {}
public function testAddHeaderAfterTheSetHeader() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.setHeader("HeADEr2", "totally different value");
    entity.addHeader("headeR2", "value4");
    var headers = entity.getHeaders("header2");
    if (headers is string[]) {
        test:assertEquals(headers[0], "totally different value", msg = "Found unexpected output");
        test:assertEquals(headers[1], "value4", msg = "Found unexpected output");
    }
    var header = entity.getHeader("header2");
    if (header is string) {
        test:assertEquals(header, "totally different value", msg = "Found unexpected output");
    }
}

//Test remove header function
@test:Config {}
public function testRemoveHeaderError() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    entity.removeHeader("HEADER1");
    entity.removeHeader("NONE_EXISTENCE_HEADER");
    var result = entity.getHeaders("header1");
    if (result is error) {
        test:assertEquals(result.message(), "Http header does not exist", msg = "Found unexpected output");
    }
}

@test:Config {}
public function testRemoveAllHeaderError() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.setHeader("HeADEr2", "totally different value");
    entity.removeAllHeaders();
    var result = entity.getHeaders("header1");
    if (result is error) {
        test:assertEquals(result.message(), "Http header does not exist", msg = "Found unexpected output");
    }
}

//Test getting a value out of a non existence header
@test:Config {}
public function testForNonExistenceHeader() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    var result = entity.getHeader("header");
    if (result is error) {
        test:assertEquals(result.message(), "Http header does not exist", msg = "Found unexpected output");
    }
}

//Test getting all header names
@test:Config {}
public function testGetAllHeaderNames() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.addHeader("HeADEr2", "totally different value");
    entity.addHeader("HEADER3", "testVal");
    var names = entity.getHeaderNames();
    test:assertEquals(names[0], "heAder1", msg = "Found unexpected output");
    test:assertEquals(names[1], "hEader2", msg = "Found unexpected output");
    test:assertEquals(names[2], "HEADER3", msg = "Found unexpected output");
}

//Test manipulating return headers
@test:Config {}
public function testManipulatingReturnHeaders() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    entity.addHeader("header1", "value2");
    entity.addHeader("header1", "value3");
    entity.addHeader("hEader2", "value3");
    entity.addHeader("headeR2", "value4");
    entity.addHeader("HeADEr2", "totally different value");
    entity.addHeader("HEADER3", "testVal");
    string[] headerNames = entity.getHeaderNames();
    foreach var header in headerNames {
        entity.removeHeader(<@untainted string> header);
    }
    string[] headers = entity.getHeaderNames();
    test:assertTrue(headers.length() == 0, msg = "Found unexpected output");
}

//Test has header function
@test:Config {}
public function testHasHeaderFunction() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    test:assertTrue(entity.hasHeader("header1"), msg = "Found unexpected output");
}

//Test has header function for a non-existence header
@test:Config {}
public function testHasHeaderForNonExistence() {
    Entity entity = new;
    entity.addHeader("heAder1", "value1");
    test:assertFalse(entity.hasHeader("header2"), msg = "Found unexpected output");
}

//Test headers with a newly created entity
@test:Config {}
public function testHeaderInNewEntity() {
    Entity entity = new;
    test:assertFalse(entity.hasHeader("header2"), msg = "Newly created entity can't have any headers");
    test:assertTrue(entity.getHeaderNames().length() == 0, msg = "HeaderNames for newly created entity are empty");
}

function testAddHeader(string headerName, string headerValue, string headerNameToBeUsedForRetrieval) returns
        @tainted string {
    Entity entity = new;
    entity.addHeader(headerName, headerValue);
    var value = entity.getHeader(headerNameToBeUsedForRetrieval);
    if (value is string) {
        return value;
    }
    return "Header not found";
}
