package team.themoment.hellogsm.web.global.security.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;


/**
 * 일정 환경 설정 정보를 관리하기 위한 클래스입니다.
 *
 * @author 양시준
 * @since 1.0.0
 */

@ConfigurationProperties(prefix = "auth")
public final class AuthEnvironment {
    private final String redirectBaseUri;
    private final List<String> allowedOrigins;

    /**
     *
     */
    public AuthEnvironment(
            String redirectBaseUri,
            String allowedOrigins
    ) {
        this.redirectBaseUri = redirectBaseUri;
        this.allowedOrigins = List.of(allowedOrigins.split(","));
    }

    public String redirectBaseUri() {
        return redirectBaseUri;
    }

    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AuthEnvironment) obj;
        return Objects.equals(this.redirectBaseUri, that.redirectBaseUri) &&
                Objects.equals(this.allowedOrigins, that.allowedOrigins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redirectBaseUri, allowedOrigins);
    }

    @Override
    public String toString() {
        return "AuthEnvironment[" +
                "redirectBaseUri=" + redirectBaseUri + ", " +
                "allowedOrigins=" + allowedOrigins + ']';
    }

}
