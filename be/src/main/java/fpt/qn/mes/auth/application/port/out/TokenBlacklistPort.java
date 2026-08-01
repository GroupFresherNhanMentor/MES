package fpt.qn.mes.auth.application.port.out;

import java.time.Duration;

public interface TokenBlacklistPort {

    void blacklistToken(String tokenId, Duration ttl);

    boolean isBlacklisted(String tokenId);
}
