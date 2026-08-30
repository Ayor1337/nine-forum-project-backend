package com.ayor.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationVerificationPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void defaultsMatchApprovedRegistrationVerificationPolicy() {
        RegistrationVerificationProperties properties = new RegistrationVerificationProperties();

        assertThat(properties.getIdempotencySeconds()).isEqualTo(60);
        assertThat(properties.getEmailLimit()).isEqualTo(3);
        assertThat(properties.getEmailWindowSeconds()).isEqualTo(900);
        assertThat(properties.getIpLimit()).isEqualTo(10);
        assertThat(properties.getIpWindowSeconds()).isEqualTo(900);
        assertThat(properties.getGlobalLimit()).isEqualTo(100);
        assertThat(properties.getGlobalWindowSeconds()).isEqualTo(600);
        assertThat(properties.getGlobalAlertPercent()).isEqualTo(80);
        assertThat(validator.validate(properties)).isEmpty();
    }

    @Test
    void rejectsNonPositiveLimitsAndInvalidAlertPercent() {
        RegistrationVerificationProperties properties = new RegistrationVerificationProperties();
        properties.setEmailLimit(0);
        properties.setGlobalAlertPercent(101);

        assertThat(validator.validate(properties)).hasSize(2);
    }
}
