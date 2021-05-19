## Overview

This module provides a set of APIs to work with messages, which follow the Multipurpose Internet Mail Extensions 
(MIME) specification as specified in the [RFC 2045 standard](https://www.ietf.org/rfc/rfc2045.txt).

> Entity refers to the header fields and the content of a message or a part of the body in a multipart entity. 

### Supported multipart types

The module supports `multipart/form-data`, `multipart/mixed`, `multipart/alternative`, `multipart/related`, and 
`multipart/parallel` as multipart content types.

### Modify and retrieve the data in an entity

This module provides functions to set and get an entity body from different kinds of message types such as XML, text, 
JSON, byte[], and body parts. Headers can be modified through functions such as `addHeader()`, `setHeader()`, 
`removeHeader()`, etc. 

### Handling large files

The entity object method `setFileAsEntityBody()` can be used to set large files as the entity body and 
is able to read it as a stream using the `getByteStream()` function.
