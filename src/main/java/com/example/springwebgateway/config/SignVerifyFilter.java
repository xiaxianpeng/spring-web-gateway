package com.example.springwebgateway.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.springwebgateway.service.VerifyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class SignVerifyFilter extends OncePerRequestFilter {

    @Resource
    private VerifyService verifyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;

            // 如果IP无效，返回错误响应
            boolean ipVerify = verifyService.verifyIp(request.getRemoteAddr());
            if (!ipVerify) {
                writeResponse(response, "Invalid IP");
                return;
            }

            // 如果时间戳无效，返回错误响应
            if (!this.verifyTimestamp(request.getHeader("x-timestamp"))) {
                writeResponse(response, "Invalid timestamp");
                return;
            }
            // 如果签名无效，返回错误响应
            if (!this.verifySignature(cachingRequest)) {
                writeResponse(response, "Invalid signature");
                return;
            }

            // todo 多次请求
            // 继续处理请求
            filterChain.doFilter(cachingRequest, response);
        }
    }

    private static void writeResponse(HttpServletResponse response, String content) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(content);
    }

    private boolean verifyTimestamp(String timestamp) {
        if (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) > 1000 * 60 * 60) {
            return false;
        }
        return true;
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

    private static String getRequestBody(ContentCachingRequestWrapper request) {
        // 对于GET请求，返回查询字符串
        if (HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
            return request.getQueryString();
        }
        // 对于POST请求，检查是否为Multipart请求，以及是否有缓存的请求体内容
        if (HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())) {
            if (request instanceof MultipartHttpServletRequest) {
                // Multipart请求可能包含文件上传，不应该读取请求体作为字符串
                return "";
            } else {
                // 获取缓存的请求体
                byte[] content = request.getContentAsByteArray();
                if (content.length > 0) {
                    return new String(content, StandardCharsets.UTF_8);
                }
            }
        }
        // 如果请求方法不是GET或POST，或者没有请求内容，则返回空字符串
        return "";
    }
}
