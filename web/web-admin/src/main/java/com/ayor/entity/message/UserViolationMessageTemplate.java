package com.ayor.entity.message;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "UserViolationMessageTemplate 数据模型")
public class UserViolationMessageTemplate {

    public static final String NICKNAME_VIOLATION = """
            您的用户名违反了社区规定，我们已经帮您清除了，请重新设置。
            """;

    public static final String BANNER_VIOLATION = """
            您的个人资料背景图违反了社区规定，我们已经帮您清除了，请重新上传。
            """;

    public static final String AVATAR_VIOLATION = """
            您的头像违反了社区规定，我们已经帮您清除了，请重新上传。
            """;

}
