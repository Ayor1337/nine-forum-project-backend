package com.ayor.controller.exception;

import com.ayor.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidateControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SampleController())
            .setControllerAdvice(new ValidateController())
            .build();

    @Test
    void shouldReturnParamErrorWhenRequestParamTypeMismatch() throws Exception {
        mockMvc.perform(get("/sample").param("page", "not-a-number"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(203))
                .andExpect(jsonPath("$.message").value("请求参数内容有误"));
    }

    @RestController
    static class SampleController {

        @GetMapping("/sample")
        Result<Integer> sample(@RequestParam("page") Integer page) {
            return Result.ok(page);
        }
    }
}
