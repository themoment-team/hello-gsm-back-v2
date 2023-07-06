package team.themoment.hellogsm.web.domain.common;

import org.springframework.restdocs.cookies.RequestCookiesSnippet;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;

public class ControllerTestUtil {
    public static RequestCookiesSnippet requestSessionCookie() {
        return requestCookies(cookieWithName("SESSION").description("사용자의 SESSION ID, 브라우저로 접근 시 자동 생성됩니다."));
    }

    public static String enumAsString(Class<? extends Enum<?>> enumClass) {
        Enum<?>[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants != null && enumConstants.length > 0) {
            return "[Enum] " + enumClass.getSimpleName();
        }
        return "";
    }
}
