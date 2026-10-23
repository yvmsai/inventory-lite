package com.inventorylite.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/** Writes problem-detail bodies from the security filter chain, which runs before Spring MVC. */
final class ProblemResponses {

    private ProblemResponses() {
    }

    static void write(HttpServletResponse response, HttpStatus status, String title, String detail,
                      String instance) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"about:blank","title":"%s","status":%d,"detail":"%s","instance":"%s"}"""
                .formatted(title, status.value(), detail, jsonEscape(instance)));
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
