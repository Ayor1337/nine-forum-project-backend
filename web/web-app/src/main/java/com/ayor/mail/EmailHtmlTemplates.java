package com.ayor.mail;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class EmailHtmlTemplates {

    private static final String REGISTER_VERIFY_TEMPLATE = load("templates/email/register-verify.html");
    private static final String VERIFY_RESULT_TEMPLATE = load("templates/email/verify-result.html");

    public String registerVerify(String verifyUrl) {
        return REGISTER_VERIFY_TEMPLATE.replace("{{verifyUrl}}", verifyUrl);
    }

    public String verifyResult(boolean success, String title, String description) {
        String statusColor = success ? "#27a644" : "#e5484d";
        String statusBorder = success ? "#27a644" : "#e5484d";
        String statusLabel = success ? "验证通过" : "验证未通过";
        String closeScript = success ? """
                <script>
                    setTimeout(function () { window.close(); }, 3000);
                </script>
                """ : "";
        String footer = success
                ? "3 秒后将自动关闭此页面，你也可以直接关掉标签页。"
                : "请回到注册页重新发送验证邮件。";
        return render(VERIFY_RESULT_TEMPLATE, Map.of(
                "title", title,
                "description", description,
                "statusColor", statusColor,
                "statusBorder", statusBorder,
                "statusLabel", statusLabel,
                "closeScript", closeScript,
                "footer", footer
        ));
    }

    private static String render(String template, Map<String, String> values) {
        String html = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
    }

    private static String load(String path) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("无法加载邮件模板: " + path, exception);
        }
    }
}
