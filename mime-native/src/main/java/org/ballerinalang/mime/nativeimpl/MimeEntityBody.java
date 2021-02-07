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

package org.ballerinalang.mime.nativeimpl;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static org.ballerinalang.mime.nativeimpl.MimeDataSourceBuilder.getErrorMsg;
import static org.ballerinalang.mime.util.HeaderUtil.isMultipart;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_CONTENT_TYPE_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PARSER_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.STREAM_ENTRY_RECORD;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.MimeUtil.getContentTypeWithParameters;
import static org.ballerinalang.mime.util.MimeUtil.getMimePackage;
import static org.ballerinalang.mime.util.MimeUtil.getNewMultipartDelimiter;

/**
 * Utilities related to MIME entity body.
 *
 * @since 1.1.0
 */
public class MimeEntityBody {
    private static final Logger log = LoggerFactory.getLogger(MimeEntityBody.class);

    public static Object getBodyParts(BObject entityObj) {
        BArray partsArray;
        try {
            String baseType = HeaderUtil.getBaseType(entityObj);
            if (baseType != null && (baseType.toLowerCase(Locale.getDefault()).startsWith(MULTIPART_AS_PRIMARY_TYPE) ||
                    baseType.toLowerCase(Locale.getDefault()).startsWith(MESSAGE_AS_PRIMARY_TYPE))) {
                //Get the body parts from entity's multipart data field, if they've been already been decoded
                partsArray = EntityBodyHandler.getBodyPartArray(entityObj);
                if (partsArray == null || partsArray.size() < 1) {
                    Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
                    if (byteChannel != null) {
                        EntityBodyHandler.decodeEntityBody(entityObj, byteChannel);
                        //Check the body part availability for the second time, since the parts will be by this
                        // time populated from bytechannel
                        partsArray = EntityBodyHandler.getBodyPartArray(entityObj);
                        //Set byte channel that belongs to parent entity to null, once the message body parts have
                        // been decoded
                        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
                    }
                }
                return partsArray;
            } else {
                return MimeUtil.createError(PARSER_ERROR, "Entity body is not a type of " +
                        "composite media type. Received content-type : " + baseType);
            }
        } catch (Throwable err) {
            return MimeUtil.createError(PARSER_ERROR,
                                        "Error occurred while extracting body parts from entity: " + getErrorMsg(err));
        }
    }

    public static Object getBodyPartsAsChannel(BObject entityObj) {
        try {
            String contentType = getContentTypeWithParameters(entityObj);
            if (isMultipart(contentType)) {
                EntityBodyChannel entityBodyChannel = creatEntityBodyChannel(entityObj, contentType);
                BObject byteChannelObj = ValueCreator.createObjectValue(IOUtils.getIOPackage(),
                                                                        READABLE_BYTE_CHANNEL_STRUCT);
                byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new EntityWrapper(entityBodyChannel));
                return byteChannelObj;
            } else {
                return MimeUtil.createError(PARSER_ERROR, "Entity doesn't contain body parts");
            }
        } catch (Throwable err) {
            log.error("Error occurred while constructing a byte channel out of body parts", err);
            return MimeUtil.createError(PARSER_ERROR, "Error occurred while constructing a byte " +
                    "channel out of body parts : " + getErrorMsg(err));
        }
    }

    public static Object getBodyPartsAsStream(BObject entityObj) {
        try {
            String contentType = getContentTypeWithParameters(entityObj);
            if (isMultipart(contentType)) {
                EntityBodyChannel entityBodyChannel = creatEntityBodyChannel(entityObj, contentType);
                entityObj.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(entityBodyChannel));
                return null;
            } else {
                return MimeUtil.createError(PARSER_ERROR, "Entity doesn't contain body parts");
            }
        } catch (Throwable err) {
            log.error("Error occurred while constructing a byte stream out of body parts", err);
            return MimeUtil.createError(PARSER_ERROR, "Error occurred while constructing a byte " +
                    "stream out of body parts : " + getErrorMsg(err));
        }
    }

    private static EntityBodyChannel creatEntityBodyChannel(BObject entityObj, String contentType) throws IOException {
        String boundaryValue = HeaderUtil.extractBoundaryParameter(contentType);
        String multipartDataBoundary = boundaryValue != null ? boundaryValue : getNewMultipartDelimiter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MultipartDataSource multipartDataSource = new MultipartDataSource(entityObj, multipartDataBoundary);
        multipartDataSource.serialize(outputStream);
        EntityBodyChannel entityBodyChannel = new EntityBodyChannel(new ByteArrayInputStream(
                outputStream.toByteArray()));
        MimeUtil.closeOutputStream(outputStream);
        return entityBodyChannel;
    }

    public static Object getByteChannel(BObject entityObj) {
        BObject byteChannelObj;
        try {
            byteChannelObj = ValueCreator.createObjectValue(IOUtils.getIOPackage(), READABLE_BYTE_CHANNEL_STRUCT);
            Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
            if (byteChannel != null) {
                byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, byteChannel);
                return byteChannelObj;
            } else {
                if (EntityBodyHandler.getMessageDataSource(entityObj) != null) {
                    return MimeUtil.createError(PARSER_ERROR, "Byte channel is not available but " +
                            "payload can be obtain either as xml, json, string or byte[] " +
                            "type");
                } else if (EntityBodyHandler.getBodyPartArray(entityObj) != null && EntityBodyHandler.
                        getBodyPartArray(entityObj).size() != 0) {
                    return MimeUtil.createError(PARSER_ERROR,
                                                "Byte channel is not available since payload contains a set of body " +
                                                        "parts");
                } else {
                    return MimeUtil.createError(PARSER_ERROR, "Byte channel is not available as payload");
                }
            }
        } catch (Throwable err) {
            return MimeUtil.createError(PARSER_ERROR,
                                        "Error occurred while constructing byte channel from entity body : " +
                                                getErrorMsg(err));
        }
    }

    public static Object getByteStream(BObject entityObj) {
        BStream byteStream = EntityBodyHandler.getByteStream(entityObj);
        if (byteStream != null) {
            return byteStream;
        }
        Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
        if (byteChannel != null) {
            // Return value implies the absence of previously set byte stream, but to return new iterator to
            // retrieve the byte channel content.
            return null;
        } else {
            if (EntityBodyHandler.getMessageDataSource(entityObj) != null) {
                return MimeUtil.createError(PARSER_ERROR, "Byte stream is not available but " +
                        "payload can be obtain either as xml, json, string or byte[] type");
            } else if (EntityBodyHandler.getBodyPartArray(entityObj) != null && EntityBodyHandler.
                    getBodyPartArray(entityObj).size() != 0) {
                return MimeUtil.createError(PARSER_ERROR, "Byte stream is not available since payload contains a set" +
                        " of body parts");
            } else {
                return MimeUtil.createError(PARSER_ERROR, "Byte stream is not available as payload");
            }
        }
    }

    public static Object getStreamEntryRecord(BObject entityObj, long arraySize) {
        Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
        if (byteChannel == null) {
            return null;
        }
        byte[] bytes;
        int arraySizeInt = (int) arraySize;
        try {
            InputStream inputStream = byteChannel.getInputStream();
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[arraySizeInt];
                int readCount = inputStream.read(buffer, 0, arraySizeInt);
                if (readCount == -1) {
                    EntityBodyHandler.closeByteChannel(byteChannel);
                    entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
                    return null;
                }
                output.write(buffer, 0, readCount);
                bytes = output.toByteArray();
            }
        } catch (IOException ex) {
            return IOUtils.createError(IOConstants.ErrorCode.GenericError,
                                       "Error occurred while reading stream:" + ex.getMessage());
        }

        BMap<BString, Object> streamEntry = ValueCreator.createRecordValue(getMimePackage(), STREAM_ENTRY_RECORD);
        streamEntry.put(MimeConstants.FIELD_VALUE, ValueCreator.createArrayValue(bytes));
        return streamEntry;
    }

    public static Object closeInputByteStream(BObject entityObj) {
        Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
        if (byteChannel != null) {
            try {
                byteChannel.close();
                entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
            } catch (IOException e) {
                return IOUtils.createError(e);
            }
        }
        return null;
    }

    public static Object getMediaType(BString contentType) {
        try {
            BObject mediaType = ValueCreator.createObjectValue(getMimePackage(), MEDIA_TYPE);
            return MimeUtil.parseMediaType(mediaType, contentType.getValue());
        } catch (Throwable err) {
            return MimeUtil.createError(INVALID_CONTENT_TYPE_ERROR, getErrorMsg(err));
        }
    }

    public static void setBodyParts(BObject entityObj, BArray bodyParts, BString contentType) {
        entityObj.addNativeData(BODY_PARTS, bodyParts);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : MULTIPART_FORM_DATA);
    }

    public static void setByteArray(BObject entityObj, BArray payload, BString contentType) {
        EntityBodyHandler.addMessageDataSource(entityObj, payload);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : OCTET_STREAM);
    }

    public static void setByteChannel(BObject entityObj, BObject byteChannel,
                                      BString contentType) {
        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME));
        Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
        if (dataSource != null) { //Clear message data source when the user set a byte channel to entity
            entityObj.addNativeData(MESSAGE_DATA_SOURCE, null);
        }
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : OCTET_STREAM);
    }

    public static void setByteStream(BObject entityObj, BStream byteStream, BString contentType) {
        entityObj.addNativeData(ENTITY_BYTE_STREAM, byteStream);
        //Clear message data source/byteChannel when the user set a byte stream to entity
        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
        entityObj.addNativeData(MESSAGE_DATA_SOURCE, null);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : OCTET_STREAM);
    }

    public static void setJson(BObject entityObj, Object jsonContent, BString contentType) {
        EntityBodyHandler.addJsonMessageDataSource(entityObj, jsonContent);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : APPLICATION_JSON);
    }

    public static void setText(BObject entityObj, BString textContent, BString contentType) {
        EntityBodyHandler.addMessageDataSource(entityObj, textContent.getValue());
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : TEXT_PLAIN);
    }

    public static void setXml(BObject entityObj, BXml xmlContent, BString contentType) {
        EntityBodyHandler.addMessageDataSource(entityObj, xmlContent);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : APPLICATION_XML);
    }
}
