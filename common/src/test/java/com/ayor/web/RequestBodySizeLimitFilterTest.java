package com.ayor.web;

import com.ayor.entity.ImageUploadLimits;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestBodySizeLimitFilterTest {

    private final RequestBodySizeLimitFilter filter = new RequestBodySizeLimitFilter();

    @Test
    void shouldRejectDeclaredOversizedBodyWithoutEnteringChain() throws Exception {
        HttpServletRequest request = declaredLengthRequest(
                "POST", ImageUploadLimits.MAX_REQUEST_BODY_BYTES + 1L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();

        filter.doFilter(request, response, (req, resp) -> invoked.set(true));

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":413"));
        assertFalse(invoked.get());
    }

    @Test
    void shouldRejectUnknownLengthBodyOnFirstBytePastLimit() throws Exception {
        HttpServletRequest request = unknownLengthRequest("PATCH", ImageUploadLimits.MAX_REQUEST_BODY_BYTES + 1L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean completed = new AtomicBoolean();

        filter.doFilter(request, response, (req, resp) -> {
            req.getInputStream().readAllBytes();
            completed.set(true);
        });

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("请求体过大"));
        assertFalse(completed.get());
    }

    @Test
    void shouldRejectBodyThatExceedsItsForgedSmallerContentLength() throws Exception {
        HttpServletRequest request = bodyRequest(
                "POST", 1L, ImageUploadLimits.MAX_REQUEST_BODY_BYTES + 1L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean completed = new AtomicBoolean();

        filter.doFilter(request, response, (req, resp) -> {
            req.getInputStream().readAllBytes();
            completed.set(true);
        });

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":413"));
        assertFalse(completed.get());
    }

    @Test
    void shouldAllowBodyAtExactBoundary() throws Exception {
        HttpServletRequest request = unknownLengthRequest("PUT", ImageUploadLimits.MAX_REQUEST_BODY_BYTES);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean completed = new AtomicBoolean();

        filter.doFilter(request, response, (req, resp) -> {
            assertEquals(ImageUploadLimits.MAX_REQUEST_BODY_BYTES, req.getInputStream().readAllBytes().length);
            completed.set(true);
        });

        assertEquals(200, response.getStatus());
        assertTrue(completed.get());
    }

    @Test
    void shouldNotLimitReadOnlyRequest() throws Exception {
        HttpServletRequest request = declaredLengthRequest(
                "GET", ImageUploadLimits.MAX_REQUEST_BODY_BYTES + 1L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();

        filter.doFilter(request, response, (req, resp) -> invoked.set(true));

        assertEquals(200, response.getStatus());
        assertTrue(invoked.get());
    }

    private HttpServletRequest unknownLengthRequest(String method, long byteCount) {
        return bodyRequest(method, -1L, byteCount);
    }

    private HttpServletRequest bodyRequest(String method, long declaredByteCount, long actualByteCount) {
        MockHttpServletRequest base = new MockHttpServletRequest(method, "/api/images");
        return new HttpServletRequestWrapper(base) {
            @Override
            public long getContentLengthLong() {
                return declaredByteCount;
            }

            @Override
            public int getContentLength() {
                return declaredByteCount < 0 || declaredByteCount > Integer.MAX_VALUE
                        ? -1
                        : (int) declaredByteCount;
            }

            @Override
            public ServletInputStream getInputStream() {
                return new RepeatingServletInputStream(actualByteCount);
            }
        };
    }

    private HttpServletRequest declaredLengthRequest(String method, long byteCount) {
        MockHttpServletRequest base = new MockHttpServletRequest(method, "/api/images");
        return new HttpServletRequestWrapper(base) {
            @Override
            public long getContentLengthLong() {
                return byteCount;
            }
        };
    }

    private static final class RepeatingServletInputStream extends ServletInputStream {

        private long remaining;

        private RepeatingServletInputStream(long remaining) {
            this.remaining = remaining;
        }

        @Override
        public int read() {
            if (remaining == 0) {
                return -1;
            }
            remaining--;
            return 'x';
        }

        @Override
        public int read(byte[] buffer, int offset, int length) {
            if (remaining == 0) {
                return -1;
            }
            int count = (int) Math.min(length, remaining);
            java.util.Arrays.fill(buffer, offset, offset + count, (byte) 'x');
            remaining -= count;
            return count;
        }

        @Override
        public boolean isFinished() {
            return remaining == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }
    }
}
