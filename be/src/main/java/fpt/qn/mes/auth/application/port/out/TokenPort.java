package fpt.qn.mes.auth.application.port.out;

public interface TokenPort {

    String generateAccessToken(String username, String role);

    String generateRefreshToken(String username);

    boolean isRefreshToken(String token);

    String extractUsername(String token);
}
