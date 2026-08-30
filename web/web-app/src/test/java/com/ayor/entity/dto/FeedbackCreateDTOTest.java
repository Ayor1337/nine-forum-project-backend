package com.ayor.entity.dto;

import com.ayor.type.FeedbackType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackCreateDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    // 测试拒绝缺失类型并空白内容
    @Test
    void rejectsMissingTypeAndBlankContent() {
        FeedbackCreateDTO dto = new FeedbackCreateDTO();
        dto.setContent("   ");

        assertThat(validator.validate(dto)).hasSize(3);
    }

    // 测试接受有效反馈
    @Test
    void acceptsValidFeedback() {
        FeedbackCreateDTO dto = new FeedbackCreateDTO();
        dto.setType(FeedbackType.SUGGESTION);
        dto.setContent("希望增加夜间模式切换功能");

        assertThat(validator.validate(dto)).isEmpty();
    }
}
