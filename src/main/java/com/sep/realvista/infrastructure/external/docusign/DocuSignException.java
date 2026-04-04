package com.sep.realvista.infrastructure.external.docusign;

/**
 * Runtime exception thrown when a DocuSign API operation fails.
 */
public class DocuSignException extends RuntimeException {

    public DocuSignException(String message) {
        super(message);
    }

    public DocuSignException(String message, Throwable cause) {
        super(message, cause);
    }
}
