package fpt.qn.mes.common.idempotency.infrastructure.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;
import fpt.qn.mes.common.idempotency.application.service.IdempotencyService;
import fpt.qn.mes.common.idempotency.application.service.IdempotencyService.ClaimResult;
import fpt.qn.mes.common.idempotency.domain.entities.IdempotencyKey;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

// XÓA @Component Ở ĐÂY!
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";

    // Danh sách các method không làm thay đổi dữ liệu, bỏ qua filter này
    private static final Set<String> SAFE_METHODS = Set.of(
            HttpMethod.GET.name(), 
            HttpMethod.HEAD.name(), 
            HttpMethod.OPTIONS.name(), 
            HttpMethod.TRACE.name()
    );

    IdempotencyService idempotencyService;
    ObjectMapper objectMapper;

    // Bỏ qua GET, OPTIONS... để tối ưu hiệu năng
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SAFE_METHODS.contains(request.getMethod());
    }

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
        String requestMethod = wrappedRequest.getMethod(); // Của bạn thêm vào
        byte[] requestBody = wrappedRequest.getCachedBody();

        ClaimResult claimResult;
        try {
            claimResult = idempotencyService.claimOrRetrieve(
                    idempotencyKey, userId, clientIdentifier, requestPath, requestMethod, requestBody
            );
        } catch (AppException e) {
            writeErrorResponse(response, e.getStatus().value(), e.getErrorCode(), e.getMessage());
            return;
        }

        // Đã có người gọi Key này trước đó
        if (!claimResult.isNewClaim()) {
            IdempotencyKey cachedKey = claimResult.record();

            // CHẶN NGAY RACE CONDITION: Key tồn tại nhưng chưa có kết quả (đang xử lý)
            if (cachedKey.getResponseBody() == null) {
                writeErrorResponse(response, 409, ErrorCode.CONFLICT, "Request is currently being processed. Please wait.");
                return;
            }

            // Trả về kết quả cũ kèm chuẩn UTF-8
            response.setStatus(cachedKey.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            response.getWriter().write(cachedKey.getResponseBody());
            response.getWriter().flush();
            return;
        }

        UUID recordId = claimResult.record().getId();

        // New claim established -> proceed with request pipeline
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            int status = wrappedResponse.getStatus();
            byte[] responseBodyBytes = wrappedResponse.getContentAsByteArray();
            
            // LẤY CHUỖI RESPONSE CHUẨN UTF-8 TRÁNH LỖI FONT
            String responseBodyStr = new String(responseBodyBytes, StandardCharsets.UTF_8);

            if (status >= 200 && status < 300) {
                idempotencyService.recordSuccessResponse(recordId, status, responseBodyStr);
            } else {
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
        // KIỂM TRA ĐỂ TRÁNH LỖI CRASH "RESPONSE ALREADY COMMITTED"
        if (!response.isCommitted()) {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            ApiResponse<Void> apiResponse = ApiResponse.error(errorCode, message);
            objectMapper.writeValue(response.getOutputStream(), apiResponse);
        }
    }
}