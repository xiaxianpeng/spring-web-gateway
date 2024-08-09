package com.example.springwebgateway.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

//@Component
public class RequestCachingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isValid;
        // 检查请求体的签名
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        isValid = this.verifySignature(requestWrapper);
        // 如果签名无效，返回错误响应
        if (!isValid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid signature");
            return;
        }
        filterChain.doFilter(requestWrapper, response);
    }


    private boolean verifySignature(ContentCachingRequestWrapper request) {
        String appKey = request.getHeader("x-app-key");
        String timestamp = request.getHeader("x-timestamp");
        String nonce = request.getHeader("x-nonce");
        String clientSign = request.getHeader("x-sign");
        String secret = this.getSecretByAppKey(appKey);
        String body = getRequestBody(request);
        String serverSign = sign(appKey, timestamp, nonce, secret, body);
        if (!StringUtils.equals(clientSign, serverSign)) {
            return false;
        }
        return true;
    }

    private static String sign(String appKey,
                               String timestamp,
                               String nonce,
                               String secret,
                               String body) {
        String signature = String.join("#", appKey, timestamp, nonce, secret, body);
        return DigestUtils.md5DigestAsHex(signature.getBytes(StandardCharsets.UTF_8));
    }

    private static String getSecretByAppKey(String appKey) {
        return appKey;
    }

    private String getRequestBody(HttpServletRequest request) {

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return request.getQueryString();
        }
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (request instanceof MultipartHttpServletRequest) {
                return "";
            }
            try (BufferedReader reader = request.getReader()) {
                return IOUtils.toString(reader);
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}
