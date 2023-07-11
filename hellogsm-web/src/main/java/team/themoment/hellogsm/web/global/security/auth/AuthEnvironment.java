package team.themoment.hellogsm.web.global.security.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


/**
 * 일정 환경 설정 정보를 관리하기 위한 클래스입니다.
 *
 * @author 양시준
 * @since 1.0.0
 */

@ConfigurationProperties(prefix = "auth")
public record AuthEnvironment(
        String redirectBaseUri,
        String redirectLoginFailureUri,
        List<String> allowedOrigins
) {
}
