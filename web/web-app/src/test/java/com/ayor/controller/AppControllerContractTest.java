package com.ayor.controller;

import com.ayor.entity.dto.PasskeyAuthenticationFinishDTO;
import com.ayor.entity.dto.PasskeyRegistrationFinishDTO;
import com.ayor.entity.dto.StickerByUrlDTO;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AppControllerContractTest {

    // 测试验证控制器暴露注册验证契约
    @Test
    void authorizeControllerExposesRegistrationVerificationContract() throws NoSuchMethodException {
        assertThat(AuthorizeController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/auth");
        Method verify = AuthorizeController.class.getMethod("verify", String.class, String.class);

        assertThat(verify.getAnnotation(GetMapping.class).value()).containsExactly("/register-verifications");
        assertRequestParam(verify, 0, "email");
        assertRequestParam(verify, 1, "token");
        assertThat(AuthorizeController.class.getMethod("registerVerify", com.ayor.entity.dto.RegDTO.class)
                .getParameters()[0].isAnnotationPresent(Valid.class)).isTrue();
        assertThat(AuthorizeController.class.getMethod("register", com.ayor.entity.dto.AccountDTO.class)
                .getParameters()[0].isAnnotationPresent(Valid.class)).isTrue();
    }

    // 测试Passkey控制器保持公开路由名称并校验
    @Test
    void passkeyControllerKeepsPublicRouteNamesAndValidation() throws NoSuchMethodException {
        assertThat(PasskeyController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/passkeys");

        assertThat(PasskeyController.class.getMethod("createRegistrationOptions")
                .getAnnotation(PostMapping.class).value()).containsExactly("/registration/options");
        Method register = PasskeyController.class.getMethod("registerCredential", PasskeyRegistrationFinishDTO.class);
        assertThat(register.getAnnotation(PostMapping.class).value()).containsExactly("/registrations");
        assertRequestBody(register, 0);
        assertThat(register.getParameters()[0].isAnnotationPresent(Valid.class)).isTrue();

        Method delete = PasskeyController.class.getMethod("deleteCredential", Long.class);
        assertThat(delete.getAnnotation(DeleteMapping.class).value()).containsExactly("/{credential_id}");
        assertPathVariable(delete, 0, "credential_id");

        Method authenticate = PasskeyController.class.getMethod(
                "authenticate", PasskeyAuthenticationFinishDTO.class, jakarta.servlet.http.HttpServletRequest.class);
        assertThat(authenticate.getAnnotation(PostMapping.class).value()).containsExactly("/authentications");
        assertRequestBody(authenticate, 0);
        assertThat(authenticate.getParameters()[0].isAnnotationPresent(Valid.class)).isTrue();
    }

    // 测试会话控制器保持消息路由参数
    @Test
    void conversationControllerKeepsMessagingRouteParameters() throws NoSuchMethodException {
        Method newConversation = ConversationController.class.getMethod("newConversation", String.class);
        assertThat(newConversation.getAnnotation(PostMapping.class).value()).isEmpty();
        assertRequestParam(newConversation, 0, "username");

        Method sendMessage = ConversationController.class.getMethod(
                "sendMessage", Integer.class, com.ayor.entity.dto.ConversationMessageDTO.class);
        assertThat(sendMessage.getAnnotation(PostMapping.class).value()).containsExactly("/{conversation_id}/messages");
        assertPathVariable(sendMessage, 0, "conversation_id");
        assertRequestBody(sendMessage, 1);

        Method clearUnread = ConversationController.class.getMethod("clearUnreadMessageCount", Integer.class, Integer.class);
        assertThat(clearUnread.getAnnotation(DeleteMapping.class).value()).containsExactly("/{conversation_id}/unread-messages");
        assertPathVariable(clearUnread, 0, "conversation_id");
        assertRequestParam(clearUnread, 1, "from_user_id");

        Method recall = ConversationController.class.getMethod("recallMessage", Integer.class, Integer.class);
        assertThat(recall.getAnnotation(DeleteMapping.class).value()).containsExactly("/{conversation_id}/messages/{message_id}/recall");
        assertPathVariable(recall, 0, "conversation_id");
        assertPathVariable(recall, 1, "message_id");

        Method pin = ConversationController.class.getMethod(
                "pinConversation",
                Integer.class,
                com.ayor.entity.dto.ConversationPinDTO.class);
        assertThat(pin.getAnnotation(PutMapping.class).value()).containsExactly("/{conversation_id}/pin");
        assertPathVariable(pin, 0, "conversation_id");
        assertRequestBody(pin, 1);
    }

    // 测试收藏并点赞控制器保持公开路由名称
    @Test
    void collectAndLikeControllersKeepPublicRouteNames() throws NoSuchMethodException {
        Method collect = CollectController.class.getMethod("collectThread", Integer.class);
        assertThat(collect.getAnnotation(PostMapping.class).value()).containsExactly("/threads/{thread_id}/collections");
        assertPathVariable(collect, 0, "thread_id");

        Method collected = CollectController.class.getMethod("getCollects", Integer.class, Integer.class, Integer.class);
        assertThat(collected.getAnnotation(GetMapping.class).value()).containsExactly("/users/{user_id}/collected-threads");
        assertPathVariable(collected, 0, "user_id");
        assertRequestParam(collected, 1, "page");
        assertRequestParam(collected, 2, "page_size");

        Method like = LikeController.class.getMethod("likeThread", Integer.class);
        assertThat(like.getAnnotation(PostMapping.class).value()).containsExactly("/threads/{thread_id}/likes");
        assertPathVariable(like, 0, "thread_id");

        Method likes = LikeController.class.getMethod("getLikes", Integer.class, Integer.class, Integer.class);
        assertThat(likes.getAnnotation(GetMapping.class).value()).containsExactly("/users/{user_id}/liked-threads");
        assertPathVariable(likes, 0, "user_id");
        assertRequestParam(likes, 1, "page");
        assertRequestParam(likes, 2, "page_size");
    }

    // 测试贴纸控制器保持校验并资源 ID 路由
    @Test
    void stickerControllerKeepsValidationAndAssetIdRoutes() throws NoSuchMethodException {
        assertThat(StickerController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/stickers");

        Method addByUrl = StickerController.class.getMethod("addStickerByUrl", StickerByUrlDTO.class);
        assertThat(addByUrl.getAnnotation(PostMapping.class).value()).containsExactly("/by-url");
        assertRequestBody(addByUrl, 0);
        assertThat(addByUrl.getParameters()[0].isAnnotationPresent(Valid.class)).isTrue();

        Method addSticker = StickerController.class.getMethod("addSticker", Integer.class);
        assertThat(addSticker.getAnnotation(PostMapping.class).value()).containsExactly("/{assetId}");
        assertPathVariable(addSticker, 0, "assetId");

        Method getStickers = StickerController.class.getMethod("getStickers", Integer.class, Integer.class);
        assertRequestParam(getStickers, 0, "page_num");
        assertRequestParam(getStickers, 1, "page_size");
    }

    private void assertPathVariable(Method method, int index, String name) {
        PathVariable annotation = method.getParameters()[index].getAnnotation(PathVariable.class);
        assertThat(annotation).isNotNull();
        assertThat(firstNonBlank(annotation.value(), annotation.name())).isEqualTo(name);
    }

    private void assertRequestParam(Method method, int index, String name) {
        RequestParam annotation = method.getParameters()[index].getAnnotation(RequestParam.class);
        assertThat(annotation).isNotNull();
        assertThat(firstNonBlank(annotation.value(), annotation.name())).isEqualTo(name);
    }

    private void assertRequestBody(Method method, int index) {
        Parameter parameter = method.getParameters()[index];
        assertThat(Arrays.stream(parameter.getAnnotations()).map(Annotation::annotationType))
                .contains(RequestBody.class);
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
