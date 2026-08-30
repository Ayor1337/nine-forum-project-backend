package com.ayor.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Base64UploadTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectBlankFieldsAndOversizedBase64Text() {
        Base64Upload blank = new Base64Upload(" ", " ");
        Base64Upload oversized = new Base64Upload(
                "A".repeat(ImageUploadLimits.MAX_BASE64_TEXT_CHARS + 1), "image.png");

        assertEquals(2, validator.validate(blank).size());
        assertTrue(validator.validate(oversized).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("base64")));
    }

    @Test
    void shouldAcceptBoundarySizedBase64Text() {
        Base64Upload upload = new Base64Upload(
                "A".repeat(ImageUploadLimits.MAX_BASE64_TEXT_CHARS), "image.png");

        assertTrue(validator.validate(upload).isEmpty());
    }
}
