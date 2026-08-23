package com.ayor.web;

import com.ayor.entity.Base64Upload;
import com.ayor.image.ImageProcessingBusyException;
import com.ayor.image.ImageValidationException;
import com.ayor.result.Result;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImageUploadExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SampleController())
            .setControllerAdvice(new ImageUploadExceptionHandler())
            .build();

    @Test
    void shouldMapImageValidationToHttp400AndBusinessCode203() throws Exception {
        mockMvc.perform(post("/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(203))
                .andExpect(jsonPath("$.message").value("图片总像素不能超过16777216"));
    }

    @Test
    void shouldMapBusyProcessingToHttp429() throws Exception {
        mockMvc.perform(post("/busy"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429));
    }

    @Test
    void shouldMapBase64BeanValidationToUnifiedHttp400Response() throws Exception {
        mockMvc.perform(post("/bean-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"base64\":\"\",\"fileName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(203));
    }

    @RestController
    static class SampleController {

        @PostMapping("/validation")
        Result<Void> validation() {
            throw new ImageValidationException("图片总像素不能超过16777216");
        }

        @PostMapping("/busy")
        Result<Void> busy() {
            throw new ImageProcessingBusyException();
        }

        @PostMapping("/bean-validation")
        Result<Void> beanValidation(@RequestBody @Valid Base64Upload upload) {
            return Result.ok();
        }
    }
}
