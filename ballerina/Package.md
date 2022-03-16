## Package overview

This package provides a set of APIs to work with messages, which follow the Multipurpose Internet Mail Extensions
(MIME) specification as specified in the [RFC 2045 standard](https://www.ietf.org/rfc/rfc2045.txt).

```
Entity refers to the header fields and the content of a message or a part of the body in a multipart entity. 
```

### Supported multipart types

The package supports `multipart/form-data`, `multipart/mixed`, `multipart/alternative`, `multipart/related`, and
`multipart/parallel` as multipart content types.

### Modify and retrieve the data in an entity

This package provides functions to set and get an entity body from different kinds of message types such as XML, text,
JSON, byte[], and body parts. Headers can be modified through functions such as `addHeader()`, `setHeader()`,
`removeHeader()`, etc.

### Handling large files

The entity object method `setFileAsEntityBody()` can be used to set large files as the entity body and
is able to read it as a stream using the `getByteStream()` function.

## Report issues

To report bugs, request new features, start new discussions, view project boards, etc., go to the [Ballerina standard library parent repository](https://github.com/ballerina-platform/ballerina-standard-library).

## Useful links

- Chat live with us via our [Slack channel](https://ballerina.io/community/slack/).
- Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.