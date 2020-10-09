/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.mime.util;

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.api.BErrorCreator;
import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.BValueCreator;
import org.ballerinalang.jvm.api.values.BObject;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_MIXED;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;

/**
 * Contains utility functions used by mime test cases.
 *
 * @since 0.990.3
 */
public class ExternTestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ExternTestUtils.class);

    /**
     * From a given list of body parts get a ballerina value array.
     *
     * @param bodyParts List of body parts
     * @return BValueArray representing an array of entities
     */
    public static ArrayValue getArrayOfBodyParts(ArrayList<BObject> bodyParts) {
        BType typeOfBodyPart = bodyParts.get(0).getType();
        BObject[] result = bodyParts.toArray(new BObject[bodyParts.size()]);
        return new ArrayValueImpl(result, new org.ballerinalang.jvm.types.BArrayType(typeOfBodyPart));
    }

    /**
     * Get a text body part from a given text content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getTextBodyPart() {
        String textPayload = "Ballerina text body part";
        BObject bodyPart = createEntityObject();
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(textPayload));
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, TEXT_PLAIN);
        return bodyPart;
    }

    /**
     * Get a text body part as a file upload.
     *
     * @return A body part with text content in a file
     */
    public static BObject getTextFilePart() {
        try {
            File file = getTemporaryFile("test", ".txt", "Ballerina text as a file part");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, TEXT_PLAIN);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for json file part in getTextFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a text body part from a given text content and content transfer encoding.
     *
     * @param contentTransferEncoding Content transfer encoding value
     * @param message                 String that needs to be written to temp file
     * @return A ballerina struct that represent a body part
     */
    public static BObject getTextFilePartWithEncoding(String contentTransferEncoding, String message) {
        try {
            File file = getTemporaryFile("test", ".txt", message);
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, TEXT_PLAIN);
            HeaderUtil.setHeaderToEntity(bodyPart, MimeConstants.CONTENT_TRANSFER_ENCODING, contentTransferEncoding);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for json file part in getTextFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a json body part from a given json content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getJsonBodyPart() {
        String key = "bodyPart";
        String value = "jsonPart";
        String jsonContent = "{\"" + key + "\":\"" + value + "\"}";
        BObject bodyPart = createEntityObject();
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper(jsonContent);
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_JSON);
        return bodyPart;
    }

    /**
     * Get a json body part as a file upload.
     *
     * @return A body part with json content in a file
     */
    public static BObject getJsonFilePart() {
        try {
            File file = getTemporaryFile("test", ".json", "{'name':'wso2'}");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_JSON);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for json file part in getJsonFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a xml body part from a given xml content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getXmlBodyPart() {
        XMLValue xmlNode = XMLFactory.parse("<name>Ballerina</name>");
        BObject bodyPart = createEntityObject();
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                xmlNode.stringValue(null).getBytes(StandardCharsets.UTF_8)));
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(byteChannel));
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_XML);
        return bodyPart;
    }

    /**
     * Get a xml body part as a file upload.
     *
     * @return A body part with xml content in a file
     */
    public static BObject getXmlFilePart() {
        try {
            File file = getTemporaryFile("test", ".xml", "<name>Ballerina xml file part</name>");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_XML);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for xml file part in getXmlFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a binary body part from a given blob content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getBinaryBodyPart() {
        BObject bodyPart = createEntityObject();
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper("Ballerina binary part");
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, OCTET_STREAM);
        return bodyPart;
    }

    /**
     * Get a binary body part as a file upload.
     *
     * @return A body part with blob content in a file
     */
    public static BObject getBinaryFilePart() {
        try {
            File file = getTemporaryFile("test", ".tmp", "Ballerina binary file part");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, OCTET_STREAM);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for binary file part in getBinaryFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a multipart entity with four different body parts included in it.
     *
     * @return A ballerina entity with four body parts in it
     */
    public static BObject getMultipartEntity() {
        BObject multipartEntity = createEntityObject();
        ArrayList<BObject> bodyParts = getMultipleBodyParts();
        multipartEntity.addNativeData(BODY_PARTS, ExternTestUtils.getArrayOfBodyParts(bodyParts));
        return multipartEntity;
    }

    /**
     * Get a multipart entity with four other multipart entities, each containing four different other body parts.
     *
     * @return A nested multipart entity
     */
    public static BObject getNestedMultipartEntity() {
        BObject nestedMultipartEntity = createEntityObject();
        ArrayList<BObject> bodyParts = getEmptyBodyPartList();
        for (BObject bodyPart : bodyParts) {
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, MULTIPART_MIXED);
            bodyPart.addNativeData(BODY_PARTS, ExternTestUtils.getArrayOfBodyParts(getMultipleBodyParts()));
        }
        nestedMultipartEntity.addNativeData(BODY_PARTS, ExternTestUtils.getArrayOfBodyParts(bodyParts));
        return nestedMultipartEntity;
    }

    /**
     * Get a list of four different body parts.
     *
     * @return A list of different body parts
     */
    private static ArrayList<BObject> getMultipleBodyParts() {
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart());
        bodyParts.add(getXmlFilePart());
        bodyParts.add(getTextBodyPart());
        bodyParts.add(getBinaryFilePart());
        return bodyParts;
    }

    /**
     * Get an empty body part list.
     *
     * @return A list of empty body parts
     */
    private static ArrayList<BObject> getEmptyBodyPartList() {
        ArrayList<BObject> bodyParts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            bodyParts.add(createEntityObject());
        }
        return bodyParts;
    }

    public static BObject createEntityObject() {
        return BValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY);
    }


    public static BObject createMediaTypeObject() {
        return BValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
    }

    public static BObject getContentDispositionStruct() {
        return BValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, CONTENT_DISPOSITION_STRUCT);
    }

    public static File getTemporaryFile(String fileName, String fileType, String valueTobeWritten) throws IOException {
        File file = File.createTempFile(fileName, fileType);
        file.deleteOnExit();
        OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset());
        try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(valueTobeWritten);
            return file;
        }
    }

    public static Object createTemporaryFile(BString fileName, BString fileType, BString valueTobeWritten) {
        try {
            File file = getTemporaryFile(fileName.getValue(), fileType.getValue(), valueTobeWritten.getValue());
            return org.ballerinalang.jvm.api.BStringUtils.fromString(file.getAbsolutePath());
        } catch (IOException ex) {
            return BErrorCreator
                    .createError(BStringUtils.fromString("Error occurred creating file: " + ex.getMessage()));
        }
    }

    public static void assertGetBodyPartsAsChannel(BObject bodyChannel) {
        Channel channel = (Channel) bodyChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/mixed; boundary=e3a0b9ad7b4e7cdt",
                                                                        channel.getInputStream());
            Assert.assertEquals(mimeParts.size(), 4);
            BObject bodyPart = createEntityObject();
            validateBodyPartContent(mimeParts, bodyPart);
        } catch (MimeTypeParseException e) {
            LOG.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        } catch (IOException e) {
            LOG.error("Error occurred while decoding binary part", e.getMessage());
        }
    }

    /**
     * Validate that the decoded body part content matches with the encoded content.
     *
     * @param mimeParts List of decoded body parts
     * @param bodyPart  Ballerina body part
     * @throws IOException When an exception occurs during binary data decoding
     */
    public static void validateBodyPartContent(List<MIMEPart> mimeParts, BObject bodyPart)
            throws IOException {
        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(0));
        Object jsonData = EntityBodyHandler.constructJsonDataSource(bodyPart);
        Assert.assertNotNull(jsonData);
        Assert.assertEquals(BStringUtils.getJsonString(jsonData), "{\"" + "bodyPart" + "\":\"" + "jsonPart" + "\"}");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(1));
        XMLValue xmlData = EntityBodyHandler.constructXmlDataSource(bodyPart);
        Assert.assertNotNull(xmlData);
        Assert.assertEquals(xmlData.stringValue(null), "<name>Ballerina xml file part</name>");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(2));
        BString textData = EntityBodyHandler.constructStringDataSource(bodyPart);
        Assert.assertNotNull(textData);
        Assert.assertEquals(textData.getValue(), "Ballerina text body part");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(3));
        ArrayValue blobDataSource = EntityBodyHandler.constructBlobDataSource(bodyPart);
        Assert.assertNotNull(blobDataSource);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        blobDataSource.serialize(outStream);
        Assert.assertEquals(new String(outStream.toByteArray(), StandardCharsets.UTF_8), "Ballerina binary file part");
    }

    static void verifyMimeError(BValue returnValue, String errMsg) {
        Assert.assertEquals(((BError) returnValue).getMessage(), errMsg);
    }

    public static void assertJBytesWithBBytes(byte[] jBytes, byte[] bBytes) {
        for (int i = 0; i < jBytes.length; i++) {
            Assert.assertEquals(bBytes[i], jBytes[i], "Invalid byte value returned.");
        }
    }
}
