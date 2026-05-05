package com.ayor.filter;

import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.result.Result;
import com.ayor.type.AccountStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MuteActionFilter extends OncePerRequestFilter {

    private static final List<RequestMatcher> MUTED_BLOCKED_MATCHERS = List.of(
            new AntPathRequestMatcher("/api/threads", "POST"),
            new AntPathRequestMatcher("/api/threads/*/posts", "POST"),
            new AntPathRequestMatcher("/api/topics/*/chat-messages", "POST"),
            new AntPathRequestMatcher("/api/conversations", "POST"),
            new AntPathRequestMatcher("/api/conversations/*/messages", "POST")
    );

    private final AccountMapper accountMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!requiresMuteCheck(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        Integer accountId = currentAccountId();
        if (accountId == null) {
            filterChain.doFilter(request, response);
            return;
        }
        Account account = accountMapper.getAccountById(accountId);
        if (account != null && AccountStatus.fromCode(account.getStatus()) == AccountStatus.MUTED) {
            writeFailure(response, 403, "账号已被禁言");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiresMuteCheck(HttpServletRequest request) {
        for (RequestMatcher matcher : MUTED_BLOCKED_MATCHERS) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    private Integer currentAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            return null;
        }
        return Integer.parseInt(userDetails.getUsername());
    }

    private void writeFailure(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(Result.fail(code, message).toJSONString());
    }
}
