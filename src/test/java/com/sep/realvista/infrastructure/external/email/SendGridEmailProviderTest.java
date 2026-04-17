package com.sep.realvista.infrastructure.external.email;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendGridEmailProviderTest {

    @Mock
    private SendGrid sendGrid;

    private SendGridEmailProvider provider;

    private static final String FROM = "noreply@realvista.com";
    private static final String TO = "user@example.com";
    private static final String SUBJECT = "Test Subject";

    @BeforeEach
    void setUp() {
        provider = new SendGridEmailProvider("test-api-key");
        ReflectionTestUtils.setField(provider, "sendGrid", sendGrid);
    }

    @Test
    void sendSimple_shouldSucceedOnAcceptedResponse() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        provider.sendSimple(FROM, TO, SUBJECT, "Hello World");
    }

    @Test
    void sendHtml_shouldSucceedOnAcceptedResponse() throws IOException {
        Response response = new Response();
        response.setStatusCode(202);
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        provider.sendHtml(FROM, TO, SUBJECT, "<h1>Hello</h1>");
    }

    @Test
    void sendSimple_shouldThrowOnErrorStatusCode() throws IOException {
        Response response = new Response();
        response.setStatusCode(400);
        response.setBody("{\"errors\":[{\"message\":\"Bad Request\"}]}");
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        assertThrows(RuntimeException.class, () -> provider.sendSimple(FROM, TO, SUBJECT, "Hello"));
    }

    @Test
    void sendHtml_shouldThrowOnIOException() throws IOException {
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("Network error"));

        assertThrows(RuntimeException.class, () -> provider.sendHtml(FROM, TO, SUBJECT, "<h1>Hello</h1>"));
    }
}
