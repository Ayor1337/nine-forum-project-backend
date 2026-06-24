package com.ayor.controller;

import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.dto.FeedbackHandleDTO;
import com.ayor.entity.dto.ReportHandleDTO;
import com.ayor.entity.dto.RoleDTO;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
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

class AdminControllerContractTest {

    // 测试角色控制器保持角色账号和权限路由
    @Test
    void roleControllerKeepsRoleAccountAndPermissionRoutes() throws NoSuchMethodException {
        assertThat(RoleController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/roles");

        Method updateRole = RoleController.class.getMethod("updateRole", Integer.class, RoleDTO.class);
        assertThat(updateRole.getAnnotation(PutMapping.class).value()).containsExactly("/{roleId}");
        assertPathVariable(updateRole, 0, "roleId");
        assertRequestBody(updateRole, 1);

        Method listAccounts = RoleController.class.getMethod("listRoleAccounts", Integer.class, Integer.class, Integer.class);
        assertThat(listAccounts.getAnnotation(GetMapping.class).value()).containsExactly("/{roleId}/accounts");
        assertPathVariable(listAccounts, 0, "roleId");
        assertRequestParam(listAccounts, 1, "page_num");
        assertRequestParam(listAccounts, 2, "page_size");

        Method addPermission = RoleController.class.getMethod("addPermissionToRole", Integer.class, com.ayor.entity.pojo.Permission.class);
        assertThat(addPermission.getAnnotation(PostMapping.class).value()).containsExactly("/{roleId}/permissions");
        assertPathVariable(addPermission, 0, "roleId");
        assertRequestBody(addPermission, 1);

        Method removePermission = RoleController.class.getMethod("removePermissionFromRole", Integer.class, String.class);
        assertThat(removePermission.getAnnotation(DeleteMapping.class).value()).containsExactly("/{roleId}/permissions/{permission}");
        assertPathVariable(removePermission, 0, "roleId");
        assertPathVariable(removePermission, 1, "permission");
    }

    // 测试账号控制器保持管理路由并校验
    @Test
    void accountControllerKeepsManagementRoutesAndValidation() throws NoSuchMethodException {
        assertThat(AccountController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/accounts");

        Method getAccounts = AccountController.class.getMethod(
                "getAccounts", String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        assertRequestParam(getAccounts, 0, "query");
        assertRequestParam(getAccounts, 1, "page_num");
        assertRequestParam(getAccounts, 2, "page_size");
        assertRequestParam(getAccounts, 3, "status");
        assertRequestParam(getAccounts, 4, "role_id");

        Method violation = AccountController.class.getMethod("violationProfile", Integer.class, String.class);
        assertThat(violation.getAnnotation(PostMapping.class).value()).containsExactly("/{accountId}/violations");
        assertPathVariable(violation, 0, "accountId");
        assertRequestParam(violation, 1, "type");

        Method update = AccountController.class.getMethod("updateAccount", Integer.class, AccountDTO.class);
        assertThat(update.getAnnotation(PutMapping.class).value()).containsExactly("/{accountId}");
        assertPathVariable(update, 0, "accountId");
        assertRequestBody(update, 1);
        assertThat(update.getParameters()[1].isAnnotationPresent(Valid.class)).isTrue();
    }

    // 测试举报控制器保持已认证举报处理契约
    @Test
    void reportControllerKeepsAuthenticatedReportHandlingContract() throws NoSuchMethodException {
        assertThat(ReportController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/reports");
        assertThat(ReportController.class.getAnnotation(PreAuthorize.class).value()).isEqualTo("isAuthenticated()");

        Method getReports = ReportController.class.getMethod(
                "getReports",
                Integer.class,
                Integer.class,
                com.ayor.type.ReportStatus.class,
                com.ayor.type.ReportTargetType.class,
                String.class,
                Integer.class,
                Integer.class);
        assertRequestParam(getReports, 0, "page_num");
        assertRequestParam(getReports, 1, "page_size");
        assertRequestParam(getReports, 2, "status");
        assertRequestParam(getReports, 3, "target_type");
        assertRequestParam(getReports, 4, "report_type");
        assertRequestParam(getReports, 5, "reporter_account_id");
        assertRequestParam(getReports, 6, "reported_account_id");

        Method handle = ReportController.class.getMethod("handleReport", Integer.class, ReportHandleDTO.class);
        assertThat(handle.getAnnotation(PutMapping.class).value()).containsExactly("/{reportId}/status");
        assertPathVariable(handle, 0, "reportId");
        assertRequestBody(handle, 1);
    }

    // 测试反馈控制器保持已认证反馈处理契约
    @Test
    void feedbackControllerKeepsAuthenticatedFeedbackHandlingContract() throws NoSuchMethodException {
        assertThat(FeedbackController.class.getAnnotation(RequestMapping.class).value()).containsExactly("/api/feedbacks");
        assertThat(FeedbackController.class.getAnnotation(PreAuthorize.class).value()).isEqualTo("isAuthenticated()");

        Method list = FeedbackController.class.getMethod(
                "getFeedbacks",
                Integer.class,
                Integer.class,
                com.ayor.type.FeedbackStatus.class,
                com.ayor.type.FeedbackType.class,
                Integer.class);
        assertRequestParam(list, 0, "page_num");
        assertRequestParam(list, 1, "page_size");
        assertRequestParam(list, 2, "status");
        assertRequestParam(list, 3, "type");
        assertRequestParam(list, 4, "account_id");

        Method handle = FeedbackController.class.getMethod("handleFeedback", Integer.class, FeedbackHandleDTO.class);
        assertThat(handle.getAnnotation(PutMapping.class).value()).containsExactly("/{feedbackId}/status");
        assertPathVariable(handle, 0, "feedbackId");
        assertRequestBody(handle, 1);
        assertThat(handle.getParameters()[1].isAnnotationPresent(Valid.class)).isTrue();
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
