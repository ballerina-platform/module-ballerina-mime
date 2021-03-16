// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Ballerina MIME Error Types
# Defines the common error type for the module
public type Error distinct error;

# Represents an `EncodeError` with the message and the cause.
public type EncodeError distinct Error;

# Represents a `DecodeError` with the message and the cause.
public type DecodeError distinct Error;

# Represents a `GenericMimeError` with the message and the cause.
public type GenericMimeError distinct Error;

# Represents a `SetHeaderError` with the message and the cause.
public type SetHeaderError distinct Error;

# Represents a `InvalidHeaderValueError` error with the message and the cause.
public type InvalidHeaderValueError distinct Error;

# Represents a `InvalidHeaderParamError` error with the message and the cause.
public type InvalidHeaderParamError distinct Error;

# Represents a `InvalidContentLengthError` error with the message and the cause.
public type InvalidContentLengthError distinct Error;

# Represents a `HeaderNotFoundError` error with the message and the cause.
public type HeaderNotFoundError distinct Error;

# Represents a `InvalidHeaderOperationError` error with the message and the cause.
public type InvalidHeaderOperationError distinct Error;

# Represents a `SerializationError` error with the message and the cause.
public type SerializationError distinct Error;

# Represents a `ParserError` with the message and the cause.
public type ParserError distinct Error;

# Represents an `InvalidContentTypeError` with the message and the cause.
public type InvalidContentTypeError distinct Error;

# Represents a `HeaderUnavailableError` with the message and the cause.
public type HeaderUnavailableError distinct Error;

# Represents an `IdleTimeoutTriggeredError` with the message and the cause.
public type IdleTimeoutTriggeredError distinct Error;

# Represents a `NoContentError` with the message and the cause.
public type NoContentError distinct Error;

# Constructs an `EncodeError` with the given details.
#
# + detail - Error details
# + return - An `EncodeError` with the given details set to the message
public isolated function prepareEncodingErrorWithDetail(string detail) returns EncodeError {
    return error EncodeError(detail);
}

# Constructs a `DecodeError` with the given details.
#
# + detail - Error details
# + return - `DecodeError` with the given details set to the message
public isolated function prepareDecodingErrorWithDetail(string detail) returns DecodeError {
    return error DecodeError(detail);
}
