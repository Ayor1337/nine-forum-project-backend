package com.ayor.web;

import com.ayor.entity.ImageUploadLimits;
import com.ayor.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Enforces the JSON request-body limit before business handlers run.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestBodySizeLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_METHODS = Set.of("POST", "PUT", "PATCH");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!LIMITED_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getContentLengthLong() > ImageUploadLimits.MAX_REQUEST_BODY_BYTES) {
            writeTooLargeResponse(response);
            return;
        }

        try {
            filterChain.doFilter(new LimitedRequestWrapper(request), response);
        } catch (ServletException | IOException exception) {
            if (containsTooLargeException(exception) && !response.isCommitted()) {
                response.resetBuffer();
                writeTooLargeResponse(response);
                return;
            }
            throw exception;
        }
    }

    private boolean containsTooLargeException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof RequestBodyTooLargeException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void writeTooLargeResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Result.fail(413, "请求体过大").toJSONString());
    }

    private static final class LimitedRequestWrapper extends HttpServletRequestWrapper {

        private ServletInputStream inputStream;

        private LimitedRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (inputStream == null) {
                inputStream = new LimitedServletInputStream(super.getInputStream());
            }
            return inputStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
    }

    private static final class LimitedServletInputStream extends ServletInputStream {

        private final ServletInputStream delegate;
        private long bytesRead;

        private LimitedServletInputStream(ServletInputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value >= 0) {
                recordRead(1);
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            long remainingThroughBoundary = ImageUploadLimits.MAX_REQUEST_BODY_BYTES - bytesRead + 1L;
            int boundedLength = (int) Math.min(length, Math.max(1L, remainingThroughBoundary));
            int count = delegate.read(buffer, offset, boundedLength);
            if (count > 0) {
                recordRead(count);
            }
            return count;
        }

        private void recordRead(int count) throws RequestBodyTooLargeException {
            bytesRead += count;
            if (bytesRead > ImageUploadLimits.MAX_REQUEST_BODY_BYTES) {
                throw new RequestBodyTooLargeException();
            }
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }
    }
}
