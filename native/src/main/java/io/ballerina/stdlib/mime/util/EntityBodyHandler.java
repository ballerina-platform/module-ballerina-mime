/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.stdlib.mime.util;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.stdlib.io.channels.base.Channel;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static io.ballerina.stdlib.mime.util.MimeConstants.BODY_PARTS;
import static io.ballerina.stdlib.mime.util.MimeConstants.BYTE_STREAM_NEXT_FUNC;
import static io.ballerina.stdlib.mime.util.MimeConstants.CHARSET;
import static io.ballerina.stdlib.mime.util.MimeConstants.CONTENT_TYPE;
import static io.ballerina.stdlib.mime.util.MimeConstants.ENTITY;
import static io.ballerina.stdlib.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static io.ballerina.stdlib.mime.util.MimeConstants.ENTITY_BYTE_STREAM;
import static io.ballerina.stdlib.mime.util.MimeConstants.FIELD_VALUE;
import static io.ballerina.stdlib.mime.util.MimeConstants.FIRST_BODY_PART_INDEX;
import static io.ballerina.stdlib.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static io.ballerina.stdlib.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static io.ballerina.stdlib.mime.util.MimeConstants.TEXT_EVENT_STREAM;

/**
 * Entity body related operations are included here.
 *
 * @since 0.963.0
 */
public class EntityBodyHandler {

    private static final Logger log = LoggerFactory.getLogger(EntityBodyHandler.class);
    private static final Type MIME_ENTITY_TYPE =
            TypeUtils.getType(ValueCreator.createObjectValue(MimeUtil.getMimePackage(), ENTITY));
    private static final ArrayType mimeEntityArrayType = TypeCreator.createArrayType(MIME_ENTITY_TYPE);
    public static final String OUTPUT_STREAM = "output_stream_object";
    public static final String WRITE_EVENT_STREAM_METHOD = "writeEventStream";
    public static final String EVENT_STREAM_WRITER_OBJECT = "EventStreamWriter";

    /**
     * Get a byte channel for a given text data.
     *
     * @param textPayload Text data that needs to be wrapped in a byte channel
     * @return EntityBodyChannel which represent the given text
     */
    public static EntityWrapper getEntityWrapper(String textPayload) {
        return new EntityWrapper(new EntityBodyChannel(new ByteArrayInputStream(
                textPayload.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Get the message data source associated with a given entity.
     *
     * @param entityObj Represent a ballerina entity
     * @return MessageDataSource which represent the entity body in memory
     */
    public static Object getMessageDataSource(BObject entityObj) {
        return entityObj.getNativeData(MESSAGE_DATA_SOURCE);
    }

    /**
     * Since JSON is a union of multiple data types. There is no specific data source for JSON. Hence use this method to
     * add JSON data source which tracks the JSON type using a flag.
     *
     * @param entityObj         Represent the ballerina entity
     * @param messageDataSource which represent the entity body in memory
     */
    public static void addJsonMessageDataSource(BObject entityObj, Object messageDataSource) {
        setParseJsonAndDataSource(entityObj, messageDataSource, true);
    }

    /**
     * Associate a given message data source with a given entity.
     *
     * @param entityObj      Represent the ballerina entity
     * @param messageDataSource which represent the entity body in memory
     */
    public static void addMessageDataSource(BObject entityObj, Object messageDataSource) {
        setParseJsonAndDataSource(entityObj, messageDataSource, false);
    }

    private static void setParseJsonAndDataSource(BObject entityObj, Object messageDataSource, boolean json) {
        /* specifies whether the type of the datasource is json. This is necessary because json is a union of
         * different data types and is not a single data type.*/
        entityObj.addNativeData(MimeConstants.PARSE_AS_JSON, json);
        entityObj.addNativeData(MESSAGE_DATA_SOURCE, messageDataSource);
    }

    /**
     * Construct BlobDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return Data source for binary data which is kept in memory
     * @throws IOException In case an error occurred while creating blob data source
     */
    public static BArray constructBlobDataSource(BObject entityObj) throws IOException {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            return ValueCreator.createArrayValue(new byte[0]);
        }
        try {
            return constructBlobDataSource(byteChannel.getInputStream());
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct BlobDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param inputStream Represent the input stream
     * @return Data source for binary data which is kept in memory
     */
    public static BArray constructBlobDataSource(InputStream inputStream) {
        byte[] byteData;
        try {
            byteData = MimeUtil.getByteArray(inputStream);
        } catch (IOException ex) {
            throw ErrorCreator.createError(
                    StringUtils.fromString("Error occurred while reading input stream :" + ex.getMessage()));
        }
        return ValueCreator.createArrayValue(byteData);
    }

    /**
     * Construct JsonDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BJSON data source which is kept in memory
     */
    public static Object constructJsonDataSource(BObject entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            throw MimeUtil.createError(MimeConstants.NO_CONTENT_ERROR, "empty JSON document");
        }
        try {
            return constructJsonDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString(e.getMessage()));
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct JsonDataSource from the given input stream.
     *
     * @param entity      Represent an entity object
     * @param inputStream Represent the input stream
     * @return BJSON data source which is kept in memory
     */
    public static Object constructJsonDataSource(BObject entity, InputStream inputStream) {
        Object jsonData;
        String contentTypeValue = EntityHeaderHandler.getHeaderValue(entity, CONTENT_TYPE);
        if (MimeUtil.isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (MimeUtil.isNotNullAndEmpty(charsetValue)) {
                jsonData = JsonUtils.parse(inputStream, charsetValue);
            } else {
                jsonData = JsonUtils.parse(inputStream);
            }
        } else {
            jsonData = JsonUtils.parse(inputStream);
        }
        return jsonData;
    }

    /**
     * Construct XML data source from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BXml data source which is kept in memory
     */
    public static BXml constructXmlDataSource(BObject entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            throw MimeUtil.createError(MimeConstants.NO_CONTENT_ERROR, "Empty xml payload");
        }
        try {
            return constructXmlDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString(e.getMessage()));
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct XML data source from the given input stream.
     *
     * @param entityObj Represent an entity object
     * @param inputStream  Represent the input stream
     * @return BXml data source which is kept in memory
     */
    public static BXml constructXmlDataSource(BObject entityObj, InputStream inputStream) {
        BXml xmlContent;
        String contentTypeValue = EntityHeaderHandler.getHeaderValue(entityObj, CONTENT_TYPE);
        if (MimeUtil.isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (MimeUtil.isNotNullAndEmpty(charsetValue)) {
                xmlContent = XmlUtils.parse(inputStream, charsetValue);
            } else {
                xmlContent = XmlUtils.parse(inputStream);
            }
        } else {
            xmlContent = XmlUtils.parse(inputStream);
        }
        return xmlContent;
    }

    /**
     * Construct StringDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    public static BString constructStringDataSource(BObject entityObj) {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            throw MimeUtil.createError(MimeConstants.NO_CONTENT_ERROR, "String payload is null");
        }
        try {
            return constructStringDataSource(entityObj, byteChannel.getInputStream());
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString(e.getMessage()));
        } finally {
            closeByteChannel(byteChannel);
        }
    }

    /**
     * Construct StringDataSource from the given input stream.
     *
     * @param entity      Represent an entity object
     * @param inputStream Represent the input stream
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    public static BString constructStringDataSource(BObject entity, InputStream inputStream) {
        BString textContent;
        String contentTypeValue = EntityHeaderHandler.getHeaderValue(entity, CONTENT_TYPE);
        if (MimeUtil.isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (MimeUtil.isNotNullAndEmpty(charsetValue)) {
                textContent = StringUtils.getStringFromInputStream(inputStream, charsetValue);
            } else {
                textContent = StringUtils.getStringFromInputStream(inputStream);
            }
        } else {
            textContent = StringUtils.getStringFromInputStream(inputStream);
        }
        return textContent;
    }

    /**
     * Check whether the entity body is present. Entity body can either be a byte channel/stream, fully constructed
     * message data source or a set of body parts.
     *
     * @param entityObj Represent an 'Entity'
     * @return a boolean indicating entity body availability
     */
    public static boolean checkEntityBodyAvailability(BObject entityObj) {
        return entityObj.getNativeData(ENTITY_BYTE_CHANNEL) != null || getMessageDataSource(entityObj) != null
                || entityObj.getNativeData(BODY_PARTS) != null || entityObj.getNativeData(ENTITY_BYTE_STREAM) != null;
    }

    /**
     * Check whether the streaming is required as data source should be constructed using byte channel if entity
     * contains body parts or byte channel.
     *
     * @param entity Represent an 'Entity'
     * @return a boolean indicating the streaming requirement
     */
    public static boolean isStreamingRequired(BObject entity) {
        return entity.getNativeData(ENTITY_BYTE_CHANNEL) != null || entity.getNativeData(BODY_PARTS) != null;
    }

    /**
     * Set ballerina body parts to it's top level entity.
     *
     * @param entity    Represent top level message's entity
     * @param bodyParts Represent ballerina body parts
     */
    static void setPartsToTopLevelEntity(BObject entity, ArrayList<BObject> bodyParts) {
        if (!bodyParts.isEmpty()) {
            ObjectType typeOfBodyPart =
                    (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(bodyParts.get(FIRST_BODY_PART_INDEX)));
            BObject[] result = bodyParts.toArray(new BObject[bodyParts.size()]);
            BArray partsArray = (BArray) ValueCreator
                    .createArrayValue(result, TypeCreator.createArrayType(typeOfBodyPart));
            entity.addNativeData(BODY_PARTS, partsArray);
        }
    }

    /**
     * Populate ballerina body parts with actual body content. Based on the memory threshhold body part's inputstream
     * can either come from memory or from a temp file maintained by mimepull library.
     *
     * @param bodyPart Represent ballerina body part
     * @param mimePart Represent decoded mime part
     */
    public static void populateBodyContent(BObject bodyPart, MIMEPart mimePart) {
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new MimeEntityWrapper(new EntityBodyChannel(mimePart.readOnce()),
                mimePart));
    }

    /**
     * Write byte channel stream directly into outputstream without converting it to a data source.
     *
     * @param entityObj        Represent a ballerina entity
     * @param messageOutputStream Represent the outputstream that the message should be written to
     * @throws IOException When an error occurs while writing inputstream to outputstream
     */
    public static void writeByteChannelToOutputStream(BObject entityObj, OutputStream messageOutputStream)
            throws IOException {
        Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
        if (byteChannel != null) {
            MimeUtil.writeInputToOutputStream(byteChannel.getInputStream(), messageOutputStream);
            byteChannel.close();
            //Set the byte channel to null, once it is consumed
            entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
        }
    }

    /**
     * Write byte stream directly to the output-stream without converting it to a data source.
     *
     * @param env    the environment of the resource invoked
     * @param entity       Represent a ballerina entity
     * @param outputStream Represent the output-stream that the message should be written to
     */
    public static void writeByteStreamToOutputStream(Environment env, BObject entity, OutputStream outputStream) {
        BStream byteStream = EntityBodyHandler.getByteStream(entity);
        if (byteStream != null) {
            BObject iteratorObj = byteStream.getIteratorObj();
            writeContent(env, entity, outputStream, iteratorObj);
        }
    }

    private static void writeContent(Environment env, BObject entity, OutputStream outputStream,
                                     BObject iteratorObj) {
        try {
            Object result = env.getRuntime().callMethod(iteratorObj, BYTE_STREAM_NEXT_FUNC, null);
            handleContentResult(env, entity, outputStream, result, iteratorObj);
        } catch (BError error) {
            handleContentPanic(error);
        } catch (Throwable throwable) {
            handleContentPanic(ErrorCreator.createError(throwable));
        }
    }

    public static void handleContentResult(Environment env, BObject entity, OutputStream outputStream, Object result,
                                           BObject iteratorObj) {
        if (result == null) {
            entity.addNativeData(ENTITY_BYTE_STREAM, null);
            return;
        }
        if (result instanceof BError error) {
            entity.addNativeData(ENTITY_BYTE_STREAM, null);
            handleContentPanic(error);
        }
        try {
            writeContentPart((BMap) result, outputStream);
        } catch (Exception e) {
            throw ErrorCreator.createError(StringUtils.fromString("Error occurred while writing the stream content: " +
                    MimeUtil.removeJavaExceptionPrefix(e.getMessage())));
        }
        writeContent(env, entity, outputStream, iteratorObj);
    }

    public static void handleContentPanic(BError bError) {
        throw ErrorCreator.createError(StringUtils.fromString("Error occurred while streaming content: " +
                bError.getMessage()));
    }

    /**
     * Write event-stream directly to the output-stream without converting it to a data source.
     *
     * @param env          the environment of the resource invoked
     * @param entity       Represent a ballerina entity
     * @param outputStream Represent the output-stream that the message should be written to
     */
    public static void writeEventStreamToOutputStream(Environment env, BObject entity, OutputStream outputStream) {
        BStream eventByteStream = EntityBodyHandler.getEventStream(entity);
        if (eventByteStream != null) {
            BObject eventStreamWriter = ValueCreator.createObjectValue(MimeUtil.getMimePackage(),
                    EVENT_STREAM_WRITER_OBJECT, eventByteStream);
            eventStreamWriter.addNativeData(ENTITY, entity);
            eventStreamWriter.addNativeData(OUTPUT_STREAM, outputStream);
            writeEvent(env, eventStreamWriter);
        }
    }

    private static void writeEvent(Environment env, BObject eventStreamWriter) {
        try {
            handleEventResult(eventStreamWriter, env.getRuntime().callMethod(eventStreamWriter,
                    WRITE_EVENT_STREAM_METHOD, null));
        } catch (BError error) {
            handleEventPanic(eventStreamWriter, error);
        } catch (Throwable throwable) {
            handleEventPanic(eventStreamWriter, ErrorCreator.createError(throwable));
        }
    }

    public static void handleEventResult(BObject eventStreamWriter, Object result) {
        BObject entity = (BObject) eventStreamWriter.getNativeData(ENTITY);
        OutputStream outputStream = (OutputStream) eventStreamWriter.getNativeData(OUTPUT_STREAM);
        if (result == null) {
            entity.addNativeData(ENTITY_BYTE_STREAM, null);
            EntityBodyHandler.closeMessageOutputStream(outputStream);
            return;
        }
        if (result instanceof BError error) {
            entity.addNativeData(ENTITY_BYTE_STREAM, null);
            throw error;
        }
    }

    private static void handleEventPanic(BObject eventStreamWriter, BError bError) {
        OutputStream outputStream = (OutputStream) eventStreamWriter.getNativeData(OUTPUT_STREAM);
        EntityBodyHandler.closeMessageOutputStream(outputStream);
        throw ErrorCreator.createError(StringUtils.fromString("Error occurred while streaming content: " +
                bError.getMessage()));
    }

    private static void closeMessageOutputStream(OutputStream messageOutputStream) {
        try {
            if (messageOutputStream != null) {
                messageOutputStream.close();
            }
        } catch (Exception e) {
            log.error("Couldn't close message output stream", e);
        }
    }

    private static void writeContentPart(BMap part, OutputStream outputStream) {
        BArray arrayValue = part.getArrayValue(FIELD_VALUE);
        writeContentPart(arrayValue.getBytes(), outputStream);
    }

    private static void writeContentPart(byte[] bytes, OutputStream outputStream) {
        try (ByteArrayInputStream str = new ByteArrayInputStream(bytes)) {
            MimeUtil.writeInputToOutputStream(str, outputStream);
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString(
                    "Error occurred while writing content parts to output stream: " + e.getMessage()));
        }
    }

    public static Object writeEventStreamBytesToOutputStream(BObject eventStreamWriter, byte[] bytes) {
        OutputStream outputStream = (OutputStream) eventStreamWriter.getNativeData(OUTPUT_STREAM);
        try {
            writeContentPart(bytes, outputStream);
        } catch (Exception e) {
            return ErrorCreator.createError(StringUtils.fromString(MimeUtil.removeJavaExceptionPrefix(e.getMessage())));
        }
        return null;
    }

    /**
     * Decode a given entity body to get a set of child parts and set them to parent entity's multipart data field.
     *
     * @param entityObj   Parent entity that the nested parts reside
     * @param byteChannel Represent ballerina specific byte channel
     * @throws IOException When an error occurs while getting inputstream
     */
    public static void decodeEntityBody(BObject entityObj, Channel byteChannel) throws IOException {
        String contentType = MimeUtil.getContentTypeWithParameters(entityObj);
        if (!MimeUtil.isNotNullAndEmpty(contentType) || !contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)) {
            return;
        }
        try {
            MultipartDecoder.parseBody(entityObj, contentType, byteChannel.getInputStream());
        } catch (IOException e) {
            throw new IOException("Unable to get a byte channel input stream to decode entity body", e);
        }
    }

    /**
     * Extract body parts from a given entity.
     *
     * @param entityObj Represent a ballerina entity
     * @return An array of body parts
     */
    public static BArray getBodyPartArray(BObject entityObj) {
        return entityObj.getNativeData(BODY_PARTS) != null ? (BArray) entityObj.getNativeData(BODY_PARTS)
                : (BArray) ValueCreator.createArrayValue(mimeEntityArrayType, 0);
    }

    public static Channel getByteChannel(BObject entityObj) {
        return entityObj.getNativeData(ENTITY_BYTE_CHANNEL) != null ? (Channel) entityObj.getNativeData
                (ENTITY_BYTE_CHANNEL) : null;
    }

    public static BStream getByteStream(BObject entityObj) {
        return entityObj.getNativeData(ENTITY_BYTE_STREAM) != null ? (BStream) entityObj.getNativeData
                (ENTITY_BYTE_STREAM) : null;
    }

    /**
     * Obtains the byte stream if the content type is text/event-stream.
     *
     * @param entityObj Represent a ballerina entity
     * @return A Ballerina byte stream
     */
    public static BStream getEventStream(BObject entityObj) {
        String contentType = MimeUtil.getContentTypeWithParameters(entityObj);
        return contentType.startsWith(TEXT_EVENT_STREAM) ? getByteStream(entityObj) : null;
    }

    public static void closeByteChannel(Channel byteChannel) {
        try {
            byteChannel.close();
        } catch (IOException e) {
            log.error("Error occurred while closing byte channel", e);
        }
    }

    private EntityBodyHandler() {}
}
