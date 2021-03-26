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

import ballerina/io;
import ballerina/jballerina.java;
import ballerina/lang.'string as strings;
import ballerina/log;
import ballerina/test;

const string TEMP_DIR = "build/";

function getMediaTypeTestObj() returns MediaType {
    MediaType mediaType = new;
    mediaType.primaryType = "application";
    mediaType.subType = "my-custom-type+json";
    return mediaType;
}

function getDispositionTestObj() returns ContentDisposition {
    ContentDisposition disposition = new;
    disposition.fileName = "test_file.xml";
    disposition.disposition = "inline";
    disposition.name = "test";
    return disposition;
}

//Test 'getMediaType' function in ballerina/mime package
@test:Config {}
public function testGetMediaTypeFunction() {
    MediaType|error returnVal = getMediaType("multipart/form-data; boundary=032a1ab685934650abbe059cb45d6ff3");
    if returnVal is MediaType {
        test:assertEquals(returnVal.primaryType, "multipart", msg = "Found unexpected output");
        test:assertEquals(returnVal.subType, "form-data", msg = "Found unexpected output");
        test:assertEquals(returnVal.suffix, "", msg = "Found unexpected output");
        test:assertEquals(returnVal.parameters.get("boundary"), "032a1ab685934650abbe059cb45d6ff3",
                          msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Test whether an error is returned while constructing MediaType object with an incorrect content type value
@test:Config {}
public function getMediaTypeWithIncorrectContentType() {
    MediaType|error returnVal = getMediaType("testContentType");
    if returnVal is error {
        test:assertEquals(returnVal.message(), "error InvalidContentTypeError (\"Unable to find a sub type.\")",
                            msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Test 'getBaseType' function in ballerina/mime package
@test:Config {}
public function testGetBaseTypeOnMediaTypeFunc() {
    MediaType mediaType = new;
    mediaType.primaryType = "application";
    mediaType.subType = "test+xml";
    string baseType = mediaType.getBaseType();
    test:assertEquals(baseType, "application/test+xml", msg = "Found unexpected output");
}

//"Test 'MediaType.toString()' function in ballerina/mime package
@test:Config {}
public function testToStringOnMediaTypeFunc() {
    MediaType mediaType = new;
    mediaType.primaryType = "application";
    mediaType.subType = "test+xml";
    mediaType.parameters = {"charset": "utf-8"};
    string result = mediaType.toString();
    test:assertEquals(result, "application/test+xml; charset=utf-8", msg = "Found unexpected output");
}

//Test 'getContentDispositionObject' function in ballerina/mime package
@test:Config {}
public function testGetContentDispositionObject() {
    ContentDisposition cDisposition = getContentDispositionObject("form-data; name=filepart; filename=file-01.txt");
    test:assertEquals(cDisposition.fileName, "file-01.txt", msg = "Found unexpected output");
    test:assertEquals(cDisposition.name, "filepart", msg = "Found unexpected output");
    test:assertEquals(cDisposition.disposition, "form-data", msg = "Found unexpected output");
    test:assertTrue(cDisposition.parameters.length() == 0, msg = "Found unexpected output");
}

@test:Config {}
public function testToStringOnContentDisposition() {
    ContentDisposition cDisposition = new;
    cDisposition.fileName = "file-01.txt";
    cDisposition.disposition = "form-data";
    cDisposition.name = "test";
    string result = cDisposition.toString();
    test:assertEquals(result, "form-data;name=\"test\";filename=\"file-01.txt\"", msg = "Found unexpected output");
}

//Set json data to entity and get the content back from entity as json
@test:Config {}
public function testGetAndSetJsonFunc() {
    Entity entity = new;
    entity.setJson({"code":"123"});
    var result = entity.getJson();
    if result is map<json> {
        test:assertEquals(result.code, "123", msg = "Found unexpected output");
    }
}

//Test whether the json content can be retrieved properly when it is called multiple times
@test:Config {}
public function testGetJsonMoreThanOnce() {
    Entity entity = new;
    entity.setJson({"code":"123"});
    json|error returnContent1 = entity.getJson();
    json|error returnContent2 = entity.getJson();
    json|error returnContent3 = entity.getJson();

    json content1 = {};
    json content2 = {};
    json content3 = {};

    if (returnContent1 is json) {
        content1 = returnContent1;
    } else {
        log:printError("error in returnContent1", 'error = returnContent1);
    }

    if (returnContent2 is json) {
        content2 = returnContent2;
    } else {
        log:printError("error in returnContent2", 'error = returnContent2);
    }

    if (returnContent3 is json) {
        content3 = returnContent3;
    } else {
        log:printError("error in returnContent3", 'error = returnContent3);
    }

    json returnContent = { concatContent: [content1, content2, content3] };
    json expected = {concatContent:[{code:"123"}, {code:"123"}, {code:"123"}]};
    test:assertEquals(returnContent, expected, msg = "Found unexpected output");
}

//Set xml data to entity and get the content back from entity as xml
@test:Config {}
public function testGetAndSetXml() {
    Entity entity = new;
    xml xmlContent = xml `<name>ballerina</name>`;
    entity.setXml(xmlContent);
    assertXmlPayload(entity.getXml(), xmlContent);
}

//Test whether the xml content can be retrieved properly when it is called multiple times
@test:Config {}
public function testGetXmlMoreThanOnce() {
    Entity entity = new;
    xml xmlContent = xml `<name>ballerina</name>`;
    entity.setXml(xmlContent);
    xml|error returnContent1 = entity.getXml();
    xml|error returnContent2 = entity.getXml();
    xml|error returnContent3 = entity.getXml();

    xml content1;
    xml content2;
    xml content3;

    if (returnContent1 is xml) {
        content1 = returnContent1;
    } else {
        panic returnContent1;
    }

    if (returnContent2 is xml) {
        content2 = returnContent2;
    } else {
        panic returnContent2;
    }

    if (returnContent3 is xml) {
        content3 = returnContent3;
    } else {
        panic returnContent3;
    }

    xml returnContent = content1 + content2 + content3;
    xml expected = xmlContent + xmlContent + xmlContent;
    test:assertEquals(returnContent, expected, msg = "Found unexpected output");
}

//Set text data to entity and get the content back from entity as text
@test:Config {}
public function testGetAndSetText() {
    Entity entity = new;
    string textContent = "Hello Ballerina !";
    entity.setText(textContent);
    test:assertEquals(entity.getText(), textContent, msg = "Found unexpected output");
}

//Test whether the text content can be retrieved properly when it is called multiple times
@test:Config {}
public function testGetTextMoreThanOnce() {
    Entity entity = new;
    string textContent = "Hello Ballerina !";
    entity.setText(textContent);
    string|error returnContent1 = entity.getText();
    string|error returnContent2 = entity.getText();
    string|error returnContent3 = entity.getText();

    string content1 = "";
    string content2 = "";
    string content3 = "";

    if (returnContent1 is string) {
        content1 = returnContent1;
    } else {
        log:printError("error in returnContent1", 'error = returnContent1);
    }

    if (returnContent2 is string) {
        content2 = returnContent2;
    } else {
        log:printError("error in returnContent2", 'error = returnContent2);
    }

    if (returnContent3 is string) {
        content3 = returnContent3;
    } else {
        log:printError("error in returnContent3", 'error = returnContent3);
    }

    string returnContent = content1 + content2 + content3;
    string expected = textContent + textContent + textContent;
    test:assertEquals(returnContent, expected, msg = "Found unexpected output");
}

//Set byte array data to entity and get the content back from entity as a byte array
@test:Config {}
public function testGetAndSetByteArray() {
    string content = "ballerina";
    Entity entity = new;
    entity.setByteArray(content.toBytes());
    assertByteArray(entity.getByteArray(), content);
}

//Test whether the byte array content can be retrieved properly when it is called multiple times
@test:Config {}
public function testGetByteArrayMoreThanOnce() {
    Entity entity = new;
    string content = "ballerina";
    entity.setByteArray(content.toBytes());
    byte[]|error returnContent1 = entity.getByteArray();
    byte[]|error returnContent2 = entity.getByteArray();
    byte[]|error returnContent3 = entity.getByteArray();

    byte[] content1 = [];
    byte[] content2 = [];
    byte[] content3 = [];

    if (returnContent1 is byte[]) {
        content1 = returnContent1;
    } else {
        log:printError("error in returnContent1", 'error = returnContent1);
    }

    if (returnContent2 is byte[]) {
        content2 = returnContent2;
    } else {
        log:printError("error in returnContent2", 'error = returnContent2);
    }

    if (returnContent3 is byte[]) {
        content3 = returnContent3;
    } else {
        log:printError("error in returnContent3", 'error = returnContent3);
    }

    var name = strings:fromBytes(content1);
    if (name is string) {
        test:assertEquals(name, content, msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }

    name = strings:fromBytes(content2);
    if (name is string) {
        test:assertEquals(name, content, msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }

    name = strings:fromBytes(content3);
    if (name is string) {
        test:assertEquals(name, content, msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Set file as entity body and get the content back as a byte array
@test:Config {}
public function testSetFileAsEntityBody() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    Entity entity = new;
    entity.setFileAsEntityBody(fileLocation);
    assertByteArray(entity.getByteArray(), content);
}


//Set byte channel as entity body and get the content back as a byte array. API setByteChannel() is hidden but have 
//used internally
@test:Config {}
public function testSetByteChannel() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);
    assertByteArray(entity.getByteArray(), content);
}

//Set byte channel as entity body and get that channel back. (to test internal channel functionality)
@test:Config {}
public function testGetByteChannel() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);
    var result = entity.getByteChannel();
    if (result is io:ReadableByteChannel) {
        io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(result, "utf-8");
        var returnValue = characterChannel.read(30);
        if (returnValue is string) {
            test:assertEquals(returnValue, content, msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected output type");
        }
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Set byte channel as entity body and get the content as byte stream
@test:Config {}
public function testSetByteChannelAndGetByteStream() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);

    var str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, content, msg = "Found unexpected output");
            
            record {|byte[] value;|}|io:Error? arr2 = str.next();
            test:assertTrue(arr2 is (), msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

@test:Config {}
public function testSetByteChannelAndGetByteStreamOf8ByteSizedArray() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);

    var str = entity.getByteStream(arraySize = 8);
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, "Hello Ba", msg = "Found unexpected output");
            record {|byte[] value;|}|io:Error? arr2 = str.next();
            if (arr2 is record {|byte[] value;|}) {
                name = checkpanic strings:fromBytes(arr2.value);
                test:assertEquals(name, "llerina!", msg = "Found unexpected output");
                record {|byte[] value;|}|io:Error? arr3 = str.next();
                test:assertTrue(arr3 is (), msg = "Found unexpected output");
            } else {
                test:assertFail(msg = "Found unexpected arr2 output type");
            }
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

@test:Config {}
public function testSetByteChannelAndGetByteStreamOf10ByteSizedArray() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);

    var str = entity.getByteStream(arraySize = 10);
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, "Hello Ball", msg = "Found unexpected output");
            record {|byte[] value;|}|io:Error? arr2 = str.next();
            if (arr2 is record {|byte[] value;|}) {
                name = checkpanic strings:fromBytes(arr2.value);
                test:assertEquals(name, "erina!", msg = "Found unexpected output");
                record {|byte[] value;|}|io:Error? arr3 = str.next();
                test:assertTrue(arr3 is (), msg = "Found unexpected output");
            } else {
                test:assertFail(msg = "Found unexpected arr2 output type");
            }
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}


//Set entity body as a byte channel get the content back as a string (to test internal channel functionality)
@test:Config {}
public function testSetEntityBodyMultipleTimes() {
    string content = "File Content";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setText("Hello Ballerina!");
    entity.setByteChannel(byteChannel);
    var result = entity.getByteChannel();
    if (result is io:ReadableByteChannel) {
        io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(result, "utf-8");
        var returnValue = characterChannel.read(30);
        if (returnValue is string) {
            test:assertEquals(returnValue, content, msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected output type");
        }
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

@test:Config {}
public function testSetEntityBodyMultipleTimesInDifferentTypes() returns error? {
    string fileContent = "File Content";
    string resourcePath = TEMP_DIR + "byteContent1.txt";
    check io:fileWriteString(resourcePath, fileContent);
    stream<byte[], io:Error> byteStream = check io:fileReadBlocksAsStream(resourcePath);
    Entity entity = new;
    entity.setText("Hello Ballerina!");
    entity.setByteStream(byteStream);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, fileContent, msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

//An EntityError should be returned from 'getByteChannel()', in case the payload is in data source form 
// (to test internal channel functionality)
@test:Config {}
public function testByteChannelWhenPayloadInDataSource() {
    Entity entity = new;
    entity.setJson({code:123});
    var result = entity.getByteChannel();
    if (result is error) {
        test:assertEquals(result.message(), "Byte channel is not available but payload can be obtain either as " +
                          "xml, json, string or byte[] type", msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}


//An EntityError should be returned from 'getByteStream()', in case the payload is in data source form
@test:Config {}
public function testByteStreamWhenPayloadInDataSource() {
    Entity entity = new;
    entity.setJson({code:123});
    var result = entity.getByteStream();
    if (result is error) {
        test:assertEquals(result.message(), "Byte stream is not available but payload can be obtain either as " +
                          "xml, json, string or byte[] type", msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Once the byte channel is consumed by the user, check whether the content retrieved as a text data source is empty
//(to test internal channel functionality)
@test:Config {}
public function testGetTextDataSource() {
    string content = "{'code':'123'}";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);
    entity.setHeader("content-type", "text/plain");
    //Consume byte channel externally
    var result = entity.getByteChannel();
    if (result is io:ReadableByteChannel) {
        consumeChannel(result);
    } else {
        log:printError("error in reading byte channel", 'error = result);
    }
    assertTextPayload(entity.getText(), "");
}

//Once the byte stream is consumed by the user, check whether the content retrieved as a text data source is empty
@test:Config {}
public function testGetTextDataSourceOncetheStreamIsConsumed() {
    string content = "{'code':'123'}";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);
    entity.setHeader("content-type", "text/plain");
    //Consume byte stream externally
    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        consumeStream(str);
    } else {
        log:printError("error in reading byte stream", 'error = str);
    }
    var payload = entity.getText();
    if payload is error {
        test:assertTrue(strings:includes(payload.toString(), "String payload is null"), 
                msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}


//Once the byte channel is consumed, check whether the content retrieved as a json data source return an error
//(to test internal channel funcassertTextPayloadtionality)
@test:Config {}
public function testGetJsonDataSource() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);
    entity.setHeader("content-type", "application/json");
    //Consume byte channel externally
    var result = entity.getByteChannel();
    if (result is io:ReadableByteChannel) {
        consumeChannel(result);
    } else {
        log:printError("error in reading byte channel", 'error = result);
    }

    var payload = entity.getJson();
    if payload is json {
        test:assertEquals(payload, "", msg = "Found unexpected output");
    } else {
        test:assertTrue(strings:includes(payload.message(),
            "Error occurred while extracting json data from entity"), msg = "Found unexpected output");
        var err = payload.cause();
        if (err is error) {
            test:assertTrue(strings:includes(err.message(), "empty JSON document"),
                msg = "Found unexpected output");
        }
    }
}

//Once the byte stream is consumed, check whether the content retrieved as a json data source return an error
@test:Config {}
public function testGetJsonDataSourceOnceTheByteStreamIsConsumed() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);
    entity.setHeader("content-type", "application/json");
    //Consume byte stream externally
    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        consumeStream(str);
    } else {
        log:printError("error in reading byte stream", 'error = str);
    }

    var payload = entity.getJson();
    if payload is json {
        test:assertTrue(payload is (), msg = "Found unexpected output");
    } else {
        test:assertTrue(strings:includes(payload.message(),
            "Error occurred while extracting json data from entity"), msg = "Found unexpected output");
        var err = payload.cause();
        if (err is error) {
            test:assertTrue(strings:includes(err.message(), "empty JSON document"),
                msg = "Found unexpected output");
        }
    }
}

@test:Config {}
public function testSetByteStreamAndGetJson() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    stream<io:Block, io:Error> blockStream = checkpanic byteChannel.blockStream(8196);
    Entity entity = new;
    entity.setByteStream(blockStream);
    entity.setHeader("content-type", "application/json");

    var payload = entity.getJson();
    if payload is json {
        test:assertFail(msg = "Found unexpected output type");
    } else {
        test:assertTrue(strings:includes(payload.message(),
            "Error occurred while extracting json data from entity"), msg = "Found unexpected output");
        var err = payload.cause();
        if (err is error) {
            test:assertTrue(strings:includes(err.message(), "empty JSON document"),
                msg = "Found unexpected output");
        }
    }
}

@test:Config {}
public function testSetByteStreamAndGetText() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    stream<io:Block, io:Error> blockStream = checkpanic byteChannel.blockStream(8196);
    Entity entity = new;
    entity.setByteStream(blockStream);
    entity.setHeader("content-type", "text/plain");

    var payload = entity.getText();
    if payload is string {
        test:assertFail(msg = "Found unexpected output type");
    } else {
        test:assertTrue(strings:includes(payload.message(),
            "Error occurred while extracting text data from entity"), msg = "Found unexpected output");
        var err = payload.cause();
        if (err is error) {
            test:assertTrue(strings:includes(err.message(), "String payload is null"),
                msg = "Found unexpected output");
        }
    }
}

@test:Config {}
public function testSetByteStreamAndGetXml() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    stream<io:Block, io:Error> blockStream = checkpanic byteChannel.blockStream(8196);
    Entity entity = new;
    entity.setByteStream(blockStream);
    entity.setHeader("content-type", "application/xml");

    var payload = entity.getXml();
    if payload is xml {
        test:assertFail(msg = "Found unexpected output type");
    } else {
        test:assertTrue(strings:includes(payload.message(),
            "Error occurred while extracting xml data from entity"), msg = "Found unexpected output");
        var err = payload.cause();
        if (err is error) {
            test:assertTrue(strings:includes(err.message(), "Empty xml payload"),
                msg = "Found unexpected output");
        }
    }
}

@test:Config {}
public function testGetXmlWithSuffix() {
    Entity entity = new;
    xml xmlContent = xml `<name>ballerina</name>`;
    entity.setHeader("content-type", "application/3gpdash-qoe-report+xml");
    entity.setXml(xmlContent);
    assertXmlPayload(entity.getXml(), xmlContent);
}

//Get xml content from entity that has a non compatible xml content-type
@test:Config {}
public function testGetXmlWithNonCompatibleMediaType() {
    Entity entity = new;
    xml xmlContent = xml `<name>ballerina</name>`;
    entity.setXml(xmlContent);
    entity.setHeader("content-type", "application/3gpdash-qoe-report");
    assertXmlPayload(entity.getXml(), xmlContent);
}

@test:Config {}
public function testGetJsonWithSuffix() {
    Entity entity = new;
    json jsonContent = {code:123};
    entity.setHeader("content-type", "application/yang-patch+json");
    entity.setJson(jsonContent);
    assertJsonPayload(entity.getJson(), jsonContent);
}

@test:Config {}
public function testGetJsonWithNonCompatibleMediaType() {
    Entity entity = new;
    json jsonContent = {code:123};
    entity.setJson(jsonContent);
    entity.setHeader("content-type", "application/whoispp-query");
    entity.setJson(jsonContent);
    assertJsonPayload(entity.getJson(), jsonContent);
}

@test:Config {}
public function testGetTextContentWithNonCompatibleMediaType() {
    Entity entity = new;
    string textContent = "Hello Ballerina!";
    entity.setText(textContent);
    entity.setHeader("content-type", "model/vnd.parasolid.transmit");
    assertTextPayload(entity.getText(), textContent);
}

@test:Config {}
public function testSetBodyAndGetText() {
    Entity entity = new;
    string entityBody = "Hello Ballerina!";
    entity.setBody(entityBody);
    assertTextPayload(entity.getText(), entityBody);
}

@test:Config {}
public function testSetBodyAndGetXml() {
    Entity entity = new;
    xml entityBody = xml `<name>ballerina</name>`;
    entity.setBody(entityBody);
    assertXmlPayload(entity.getXml(), entityBody);
}

@test:Config {}
public function testSetBodyAndGetJson() {
    Entity entity = new;
    json entityBody = {code:123};
    entity.setBody(entityBody);
    assertJsonPayload(entity.getJson(), entityBody);
}

@test:Config {}
public function testSetBodyAndGetByteArray() {
    string content = "ballerina";
    Entity entity = new;
    entity.setBody(content.toBytes());
    assertByteArray(entity.getByteArray(), content);
}

// SetBody() does not accept io:ReadableByteChannel. Keeping the test until the I/O revamp
// @test:Config {enable:false}
// public function testSetBodyAndGetByteChannel() {
//     string content = "Hello Ballerina!";
//     string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
//     io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
//     Entity entity = new;
//     entity.setBody(<@untainted>byteChannel);
//     var result = entity.getByteChannel();
//     if (result is io:ReadableByteChannel) {
//         io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(result, "utf-8");
//         var returnValue = characterChannel.read(30);
//         if (returnValue is string) {
//             test:assertEquals(returnValue, content, msg = "Found unexpected output");
//         } else {
//             test:assertFail(msg = "Found unexpected output type");
//         }
//     } else {
//         test:assertFail(msg = "Found unexpected output type");
//     }
// }

@test:Config {}
public function testSetBodyAndGetByteStream() returns error? {
    string content = "Hello Ballerina!";
    string resourcePath = TEMP_DIR + "byteContent2.txt";
    check io:fileWriteString(resourcePath, content);
    stream<byte[], io:Error> byteStream = check io:fileReadBlocksAsStream(resourcePath);
    Entity entity = new;
    entity.setBody(byteStream);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, content, msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

@test:Config {}
public function testSetMediaTypeToEntity() {
    Entity entity = new;
    MediaType mediaType = getMediaTypeTestObj();
    checkpanic entity.setContentType(mediaType.toString());
    test:assertEquals(entity.getContentType(), "application/my-custom-type+json", msg = "Found unexpected output");
}

@test:Config {}
public function testSetMediaTypeAndGetValueAsHeader() {
    Entity entity = new;
    MediaType mediaType = getMediaTypeTestObj();
    checkpanic entity.setContentType(mediaType.toString());
    test:assertEquals(entity.getHeader(CONTENT_TYPE), "application/my-custom-type+json",
                      msg = "Found unexpected output");
}

@test:Config {}
public function testSetHeaderAndGetMediaType() {
    Entity entity = new;
    entity.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
    test:assertEquals(entity.getContentType(), "text/plain; charset=UTF-8", msg = "Found unexpected output");
}

@test:Config {}
public function testSetContentDispositionToEntity() {
    Entity entity = new;
    entity.setContentDisposition(getDispositionTestObj());
    ContentDisposition disposition = entity.getContentDisposition();
    test:assertEquals(disposition.toString(), "inline;name=\"test\";filename=\"test_file.xml\"",
                      msg = "Found unexpected output");
}

@test:Config {}
public function testSetContentDispositionAndGetValueAsHeader() {
    Entity entity = new;
    entity.setContentDisposition(getDispositionTestObj());
    test:assertEquals(entity.getHeader(CONTENT_DISPOSITION), "inline;name=\"test\";filename=\"test_file.xml\"",
                      msg = "Found unexpected output");
}

@test:Config {}
public function testSetHeaderAndGetContentDisposition() {
    Entity entity = new;
    entity.setHeader(CONTENT_DISPOSITION, "inline;name=\"test\";filename=\"test_file.xml\"");
    ContentDisposition receivedDisposition = entity.getContentDisposition();
    test:assertEquals(receivedDisposition.toString(), "inline;name=\"test\";filename=\"test_file.xml\"",
                      msg = "Found unexpected output");
}

@test:Config {}
public function testSetContentLengthToEntity() {
    Entity entity = new;
    entity.setContentLength(45555);
    int length = checkpanic entity.getContentLength();
    test:assertEquals(length, 45555, msg = "Found unexpected output");
}

@test:Config {}
public function testSetContentLengthAndGetValueAsHeader() {
    Entity entity = new;
    entity.setContentLength(45555);
    test:assertEquals(entity.getHeader(CONTENT_LENGTH), "45555", msg = "Found unexpected output");
}

@test:Config {}
public function testSetContentIdToEntity() {
    Entity entity = new;
    entity.setContentId("test-id");
    test:assertEquals(entity.getContentId(), "test-id", msg = "Found unexpected output");
}

@test:Config {}
public function testSetContentIdAndGetValueAsHeader() {
    Entity entity = new;
    entity.setContentId("test-id");
    test:assertEquals(entity.getHeader(CONTENT_ID), "test-id", msg = "Found unexpected output");
}

//API setByteChannel() is hidden but have used internally
@test:Config {}
public function testGetAnyStreamAsString() {
    string content = "{'code':'123'}";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel, "application/json");
    assertTextPayload(entity.getText(), content);
}

//Once an entity body has been constructed as json, get the body as a byte[]. API setByteChannel() is hidden but 
//have used internally
@test:Config {}
public function testByteArrayWithContentType() {
    string content = "{'code':'123'}";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel, "application/json");
    //First time the json will be constructed from the byte channel
    json firstTime = checkpanic entity.getJson();
    //Then get the body as byte[]
    assertByteArray(entity.getByteArray(), "{\"code\":\"123\"}");
}


//Once the entity body is constructed as json and a charset value is included in the content-type, get body as a byte[]
//Due to windows test failure : https://github.com/ballerina-platform/module-ballerina-mime/issues/19
@test:Config {enable:false}
public function testByteArrayWithCharset() {
    string content = "{\"test\":\"菜鸟驿站\"}";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel, "application/json; charset=utf8");
    //First time the json will be constructed from the byte channel
    json firstTime = checkpanic entity.getJson();
    //Then get the body as byte[]
    assertByteArray(entity.getByteArray(), content);
}

//Test whether the body parts in a multipart entity can be retrieved as a byte channel
@test:Config {}
public function testGetBodyPartsAsChannel() {
    //Create a body part with json content.
    Entity bodyPart1 = new;
    bodyPart1.setJson({ "bodyPart": "jsonPart" });

    //Create another body part with a xml file.
    Entity bodyPart2 = new;
    bodyPart2.setFileAsEntityBody("tests/resources/datafiles/file.xml", TEXT_XML);

    //Create a text body part.
    Entity bodyPart3 = new;
    bodyPart3.setText("Ballerina text body part");

    //Create another body part with a text file.
    Entity bodyPart4 = new;
    bodyPart4.setFileAsEntityBody("tests/resources/datafiles/test.tmp");

    //Create an array to hold all the body parts.
    Entity[] bodyParts = [bodyPart1, bodyPart2, bodyPart3, bodyPart4];
    Entity multipartEntity = new;
    string contentType = MULTIPART_MIXED + "; boundary=e3a0b9ad7b4e7cdt";
    multipartEntity.setBodyParts(bodyParts, contentType);

    //return multipartEntity.getBodyPartsAsChannel();
    assertGetBodyPartsAsChannel(checkpanic multipartEntity.getBodyPartsAsChannel());
}

@test:Config {}
public function testGetBodyPartsAsStream() {
    //Create a body part with json content.
    Entity bodyPart1 = new;
    bodyPart1.setJson({ "bodyPart": "jsonPart" });

    //Create another body part with a xml file.
    Entity bodyPart2 = new;
    bodyPart2.setFileAsEntityBody("tests/resources/datafiles/file.xml", TEXT_XML);

    //Create a text body part.
    Entity bodyPart3 = new;
    bodyPart3.setText("Ballerina text body part");

    //Create another body part with a text file.
    Entity bodyPart4 = new;
    bodyPart4.setFileAsEntityBody("tests/resources/datafiles/test.tmp");

    //Create an array to hold all the body parts.
    Entity[] bodyParts = [bodyPart1, bodyPart2, bodyPart3, bodyPart4];
    Entity multipartEntity = new;
    string contentType = MULTIPART_MIXED + "; boundary=e3a0b9ad7b4e7cdt";
    multipartEntity.setBodyParts(bodyParts, contentType);

    stream<byte[], io:Error>|ParserError str = multipartEntity.getBodyPartsAsStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertTrue(strings:includes(name, "{\"bodyPart\":\"jsonPart\"}"), msg = "Found unexpected output");
            test:assertTrue(strings:includes(name, "<name>Ballerina xml file part</name>"), msg = "Found unexpected output");
            test:assertTrue(strings:includes(name, "Ballerina text body part"), msg = "Found unexpected output");
            test:assertTrue(strings:includes(name, "Ballerina binary file part"), msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

//Test whether an error is returned when trying to extract body parts from entity that has discrete media type content
@test:Config {}
public function getBodyPartsFromDiscreteTypeEntity() {
    Entity entity = new;
    entity.setJson({ "bodyPart": "jsonPart" });
    var result = entity.getBodyParts();
    if (result is error) {
        test:assertEquals(result.message(), "Entity body is not a type of composite media type. " +
                          "Received content-type : application/json", msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Test whether an error returned when trying convert body parts as byte channel when actual content is not
// composite media type
@test:Config {}
public function getChannelFromParts() {
    Entity entity = new;
    entity.setJson({ "bodyPart": "jsonPart" });
    var result = entity.getBodyPartsAsChannel();
    if (result is error) {
        test:assertEquals(result.message(), "Entity doesn't contain body parts", msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

@test:Config {}
public function getStreamFromParts() {
    Entity entity = new;
    entity.setJson({ "bodyPart": "jsonPart" });
    var result = entity.getBodyPartsAsStream();
    if (result is error) {
        test:assertEquals(result.message(), "Entity doesn't contain body parts", msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Test whether an error is returned when trying to retrieve a byte channel from a multipart entity
@test:Config {}
public function getChannelFromMultipartEntity() {
    Entity bodyPart1 = new;
    bodyPart1.setJson({ "bodyPart": "jsonPart" });

    Entity bodyPart2 = new;
    bodyPart2.setText("Ballerina text body part");

    Entity[] bodyParts = [bodyPart1, bodyPart2];
    Entity multipartEntity = new;
    multipartEntity.setBodyParts(bodyParts);

    var result = multipartEntity.getByteChannel();
    if (result is error) {
        test:assertEquals(result.message(), "Byte channel is not available since payload contains a set of body parts",
                          msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

//Test whether the string body is retrieved from the cache. API setByteChannel() is hidden but have used internally
@test:Config {}
public function getAnyStreamAsStringFromCache() {
    string content = "{'code':'123'}";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel, "application/json");
    string returnContent;
    returnContent = checkpanic entity.getText();
    //String body should be retrieved from the cache the second time this is called
    returnContent = returnContent + checkpanic entity.getText();
    test:assertEquals(returnContent, "{'code':'123'}{'code':'123'}", msg = "Found unexpected output");
}

@test:Config {}
public function testSeStreamAndGetStream() returns error? {
    string[] expectedContent = ["ballerina", "language"];
    stream<byte[], io:Error> byteStream = new(new ByteStreamFromStringsGenerator(expectedContent));
    Entity entity = new;
    entity.setByteStream(byteStream);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, "ballerina", msg = "Found unexpected output");
            record {|byte[] value;|}|io:Error? arr2 = str.next();
            if (arr2 is record {|byte[] value;|}) {
                name = checkpanic strings:fromBytes(arr2.value);
                test:assertEquals(name, "language", msg = "Found unexpected output");
                record {|byte[] value;|}|io:Error? arr3 = str.next();
                test:assertTrue(arr3 is (), msg = "Found unexpected output");
            } else {
                test:assertFail(msg = "Found unexpected arr2 output type");
            }
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

//TODO check Stream.close(), I think we need to use custom stream here rather the default
@test:Config {enable:false}
public function testStreamClose() returns error? {
    string content = "ballerinalanguage";
    string resourcePath = TEMP_DIR + "byteContent4.txt";
    check io:fileWriteString(resourcePath, content);
    stream<byte[], io:Error> byteStream = check io:fileReadBlocksAsStream(resourcePath);
    Entity entity = new;
    entity.setByteStream(byteStream);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, "ballerina", msg = "Found unexpected output");
            io:Error? arr2 = str.close();
            test:assertTrue(arr2 is (), msg = "Found unexpected output");
            record {|byte[] value;|}|io:Error? arr3 = str.next();
            // test:assertTrue(arr3 is (), msg = "Found unexpected output");
            if (arr3 is io:Error) {
                test:assertEquals(arr3.message(), "dwudb", msg = "Found unexpected str output type");
            } else if (arr3 is ()){
                io:println("is null");
                // test:assertEquals(arr3.message(), "dwudb", msg = "Found unexpected str output type");
            } else {
                io:println("is record");
                name = checkpanic strings:fromBytes(arr3.value);
                test:assertEquals(name, "ballerina", msg = "Found unexpected output");
            }
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

//TODO check stream.close()
@test:Config {enable:false}
public function testStreamMultipleClose() returns error? {
    string content = "ballerina";
    string resourcePath = TEMP_DIR + "byteContent3.txt";
    check io:fileWriteString(resourcePath, content);
    stream<byte[], io:Error> byteStream = check io:fileReadBlocksAsStream(resourcePath);
    Entity entity = new;
    entity.setByteStream(byteStream);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, "ballerina", msg = "Found unexpected output");
            io:Error? arr2 = str.close();
            test:assertTrue(arr2 is (), msg = "Found unexpected output");
            record {|byte[] value;|}|io:Error? arr3 = str.next();
            test:assertTrue(arr3 is (), msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

@test:Config {}
public function testStreamCloseWithSetByteChannel() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        io:Error? arr1 = str.close();
        test:assertTrue(arr1 is (), msg = "Found unexpected output");
        record {|byte[] value;|}|io:Error? arr2 = str.next();
        test:assertTrue(arr2 is (), msg = "Found unexpected output");
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

@test:Config {}
public function testStreamMultipleCloseWithSetByteChannel() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, content, msg = "Found unexpected output");
            io:Error? arr2 = str.close();
            test:assertTrue(arr2 is (), msg = "Found unexpected output");
            record {|byte[] value;|}|io:Error? arr3 = str.next();
            test:assertTrue(arr3 is (), msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

//Set byte channel as entity body and get the content as byte stream
@test:Config {}
public function testSetByteStreamFromChannelAndGetByteStream() {
    string content = "Hello Ballerina!";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    stream<io:Block, io:Error> blockStream = checkpanic byteChannel.blockStream(8196);
    Entity entity = new;
    entity.setByteStream(blockStream);

    var str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        record {|byte[] value;|}|io:Error? arr1 = str.next();
        if (arr1 is record {|byte[] value;|}) {
            string name = checkpanic strings:fromBytes(arr1.value);
            test:assertEquals(name, content, msg = "Found unexpected output");
            record {|byte[] value;|}|io:Error? arr2 = str.next();
            test:assertTrue(arr2 is (), msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected arr1 output type");
        }
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

@test:Config {}
public function testSeStreamAndForeachTheStream() returns error? {
    string[] expectedContent = ["ballerina", "language"];
    stream<byte[], io:Error> byteStream = new(new ByteStreamFromStringsGenerator(expectedContent));
    Entity entity = new;
    entity.setByteStream(byteStream);

    stream<byte[], io:Error>|ParserError str = entity.getByteStream();
    if (str is stream<byte[], io:Error>) {
        int i = 0;
        error? e = str.forEach(function (byte[] student) {
            string name = checkpanic strings:fromBytes(student);
            test:assertEquals(name, expectedContent[i], msg = "Found unexpected output");
            i = i + 1;
        });
    } else {
       test:assertFail(msg = "Found unexpected str output type" + str.message());
    }
}

//Test whether the xml content can be constructed properly once the body has been retrieved as a byte array first
//API setByteChannel() is hidden but have used internally
@test:Config {}
public function testXmlWithByteArrayContent() {
    xml content = xml `<name>Ballerina xml content</name>`;
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", "<name>Ballerina xml content</name>");
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel, "application/xml; charset=utf8");
    byte[] binaryPayload = checkpanic entity.getByteArray();
    assertXmlPayload(entity.getXml(), content);
}

//Test whether an error is returned when trying to construct body parts from an invalid channel
//API setByteChannel() is hidden but have used internally
@test:Config {}
public function getPartsFromInvalidChannel() {
    string content = "test file";
    string fileLocation = checkpanic createTemporaryFile("testFile", ".tmp", content);
    io:ReadableByteChannel byteChannel = checkpanic io:openReadableFile(fileLocation);
    Entity entity = new;
    entity.setByteChannel(byteChannel, "multipart/form-data");
    var result = entity.getBodyParts();
    if (result is error) {
        test:assertEquals(result.message(), "Error occurred while extracting body parts from entity: Missing start " +
                          "boundary", msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

function assertByteArray(byte[]|error returnResult, string expectValue) {
    if returnResult is byte[] {
        var value = strings:fromBytes(returnResult);
        if (value is string) {
            test:assertEquals(value, expectValue, msg = "Found unexpected output");
        } else {
            test:assertFail(msg = "Found unexpected output type");
        }
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

function consumeChannel(io:ReadableByteChannel byteChannel) {
    byte[]|error result = byteChannel.read(1000000);
}

function consumeStream(stream<byte[], io:Error> byteStream) {
    record {|byte[] value;|}|io:Error? arr = byteStream.next();
    if (arr is record {|byte[] value;|}) {
        consumeStream(byteStream);
    } else if (arr is io:Error) {
        test:assertFail(msg = "Found unexpected arr1 output type");
        return;
    } else {
        return;
    }
}

function assertXmlPayload(xml|error payload, xml expectValue) {
    if payload is xml {
        test:assertEquals(payload, expectValue, msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

function assertTextPayload(string|error payload, string expectValue) {
    if payload is string {
        test:assertEquals(payload, expectValue, msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

function assertJsonPayload(json|error payload, json expectValue) {
    if payload is json {
        test:assertEquals(payload, expectValue, msg = "Found unexpected output");
    } else {
        test:assertFail(msg = "Found unexpected output type");
    }
}

public function createTemporaryFile(string fileName, string fileType, string valueTobeWritten) returns string|error
= @java:Method {
    'class: "org.ballerinalang.mime.util.ExternTestUtils"
} external;

public function assertGetBodyPartsAsChannel(io:ReadableByteChannel bodyChannel) = @java:Method {
    'class: "org.ballerinalang.mime.util.ExternTestUtils"
} external;
