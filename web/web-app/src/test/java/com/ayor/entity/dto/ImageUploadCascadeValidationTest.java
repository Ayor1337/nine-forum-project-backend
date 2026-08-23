package com.ayor.entity.dto;

import com.ayor.entity.Base64Upload;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ImageUploadCascadeValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final Base64Upload invalidUpload = new Base64Upload("", "");

    @Test
    void shouldCascadeValidationThroughEveryNestedImageUploadDto() {
        ThreadDTO thread = new ThreadDTO("标题", "{}", 1, null, List.of(), List.of(invalidUpload));
        PostDTO post = new PostDTO("{}", 1, null, List.of(), List.of(invalidUpload));
        PostEditDTO postEdit = new PostEditDTO("{}", List.of(), List.of(invalidUpload));
        TopicDTO topic = new TopicDTO(null, "话题", invalidUpload, "描述", 1);
        UserProfileDTO profile = UserProfileDTO.builder().avatar(invalidUpload).build();

        assertFalse(validator.validate(thread).isEmpty());
        assertFalse(validator.validate(post).isEmpty());
        assertFalse(validator.validate(postEdit).isEmpty());
        assertFalse(validator.validate(topic).isEmpty());
        assertFalse(validator.validate(profile).isEmpty());
    }
}
