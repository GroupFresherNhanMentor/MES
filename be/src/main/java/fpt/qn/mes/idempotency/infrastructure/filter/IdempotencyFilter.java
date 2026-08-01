package fpt.qn.mes.idempotency.infrastructure.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;
import fpt.qn.mes.idempotency.application.service.IdempotencyService;
import fpt.qn.mes.idempotency.application.service.IdempotencyService.ClaimResult;
import fpt.qn.mes.idempotency.domain.entities.IdempotencyKey;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";

    IdempotencyService idempotencyService;
    ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader(HEADER_IDEMPOTENCY_KEY);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        UUID userId = resolveUserId();
        String clientIdentifier = resolveClientIdentifier(wrappedRequest, userId);
        String requestPath = wrappedRequest.getRequestURI();
        String requestMethod = wrappedRequest.getMethod();
        byte[] requestBody = wrappedRequest.getCachedBody();

        log.info("request path: {}", requestPath);
        log.info("requestMethod: {}", requestMethod);


        ClaimResult claimResult;
        try {
            claimResult = idempotencyService.claimOrRetrieve(
                    idempotencyKey, userId, clientIdentifier, requestPath, requestMethod, requestBody
            );
        } catch (AppException e) {
            writeErrorResponse(response, e.getStatus().value(), e.getErrorCode(), e.getMessage());
            return;
        }

        if (!claimResult.isNewClaim()) {
            IdempotencyKey cachedKey = claimResult.record();
            response.setStatus(cachedKey.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            if (cachedKey.getResponseBody() != null) {
                response.getWriter().write(cachedKey.getResponseBody());
            }
            return;
        }

        UUID recordId = claimResult.record().getId();

        // New claim established -> proceed with request pipeline
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            int status = wrappedResponse.getStatus();
            byte[] responseBodyBytes = wrappedResponse.getContentAsByteArray();
            String responseBodyStr = new String(responseBodyBytes, response.getCharacterEncoding());

            if (status >= 200 && status < 300) {
                // Record successful 2xx response for future idempotency replay
                idempotencyService.recordSuccessResponse(recordId, status, responseBodyStr);
            } else {
                // Non-2xx response -> release claim lock to permit retries
                idempotencyService.releaseLockOnFailure(recordId);
            }

            wrappedResponse.copyBodyToResponse();
        } catch (Exception e) {
            idempotencyService.releaseLockOnFailure(recordId);
            throw e;
        }
    }

    private UUID resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    private String resolveClientIdentifier(HttpServletRequest request, UUID userId) {
        if (userId != null) {
            return "authenticated";
        }
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return remoteAddr + ":" + (userAgent != null ? userAgent : "unknown");
    }

    private void writeErrorResponse(HttpServletResponse response, int status, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}