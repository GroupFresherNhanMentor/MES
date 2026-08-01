package fpt.qn.mes.auth.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthClockConfig {

    @Bean
    public Clock authClock() {
        return Clock.systemUTC();
    }
}
