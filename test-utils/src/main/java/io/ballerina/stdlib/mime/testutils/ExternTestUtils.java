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

package io.ballerina.stdlib.mime.testutils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.stdlib.io.channels.base.Channel;
import io.ballerina.stdlib.io.utils.IOConstants;
import io.ballerina.stdlib.mime.util.EntityBodyHandler;
import io.ballerina.stdlib.mime.util.MimeUtil;
import io.ballerina.stdlib.mime.util.MultipartDecoder;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.activation.MimeTypeParseException;

import static io.ballerina.stdlib.mime.util.MimeConstants.ENTITY;

/**
 * Contains utility functions used by mime test cases.
 *
 * @since 0.990.3
 */
public class ExternTestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ExternTestUtils.class);

    public static BObject createEntityObject() {
        return ValueCreator.createObjectValue(MimeUtil.getMimePackage(), ENTITY);
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
            return io.ballerina.runtime.api.utils.StringUtils.fromString(file.getAbsolutePath());
        } catch (IOException ex) {
            return ErrorCreator
                    .createError(StringUtils.fromString("Error occurred creating file: " + ex.getMessage()));
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
        Assert.assertEquals(StringUtils.getJsonString(jsonData), "{\"" + "bodyPart" + "\":\"" + "jsonPart" + "\"}");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(1));
        BXml xmlData = EntityBodyHandler.constructXmlDataSource(bodyPart);
        Assert.assertNotNull(xmlData);
        Assert.assertEquals(xmlData.stringValue(null), "<name>Ballerina xml file part</name>");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(2));
        BString textData = EntityBodyHandler.constructStringDataSource(bodyPart);
        Assert.assertNotNull(textData);
        Assert.assertEquals(textData.getValue(), "Ballerina text body part");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(3));
        BArray blobDataSource = EntityBodyHandler.constructBlobDataSource(bodyPart);
        Assert.assertNotNull(blobDataSource);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        blobDataSource.serialize(outStream);
        Assert.assertEquals(new String(outStream.toByteArray(), StandardCharsets.UTF_8), "Ballerina binary file part");
    }

    private ExternTestUtils() {}
}
