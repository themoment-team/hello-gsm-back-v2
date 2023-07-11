package team.themoment.hellogsm.web.global.security;

import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import team.themoment.hellogsm.entity.domain.user.enums.Role;
import team.themoment.hellogsm.web.global.data.profile.ServerProfile;
import team.themoment.hellogsm.web.global.security.auth.AuthEnvironment;
import team.themoment.hellogsm.web.global.security.handler.CustomAccessDeniedHandler;
import team.themoment.hellogsm.web.global.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

/**
 * SecurityConfig <br>
 * Spring Security를 위한 설정 클래스입니다. <br>
 * inner class로 프로필별 설정을 관리합니다. - 아직 예정임
 *
 * @author 양시준
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthEnvironment authEnv;
    private static final String logoutUri = "/auth/v1/logout";
    private static final String oauth2LoginEndpointBaseUri = "/auth/v1/oauth2/authorization";
    private static final String oauth2LoginProcessingUri = "/auth/v1/oauth2/code/*";

    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Configuration
    @EnableWebSecurity
    @Profile({ServerProfile.LOCAL,ServerProfile.DEV})
    public class LocalSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .formLogin().disable()
                    .httpBasic().disable()
                    .headers().frameOptions().sameOrigin().and()
                    .cors().disable()
                    .csrf().disable();
            logout(http);
            oauth2Login(http);
            http.authorizeHttpRequests(
                    httpRequests -> httpRequests
                            .requestMatchers(toH2Console()).permitAll()
                            .requestMatchers(HttpMethod.GET, "/identity/v1/identity/me/send-code-test")
                            .hasAnyRole(
                                    Role.ROLE_UNAUTHENTICATED.getRole(),
                                    Role.ROLE_USER.getRole())
            );
            authorizeHttpRequests(http);
            exceptionHandling(http);
            return http.build();
        }
    }

    @Configuration
    @EnableWebSecurity
    @Profile(ServerProfile.PROD)
    public class ProdSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .formLogin().disable()
                    .httpBasic().disable()
                    .cors().configurationSource(corsConfigurationSource()).and()
                    .csrf().disable();
            logout(http);
            oauth2Login(http);
            authorizeHttpRequests(http);
            exceptionHandling(http);
            return http.build();
        }
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(authEnv.allowedOrigins());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void oauth2Login(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2Login ->
                oauth2Login
                        .authorizationEndpoint().baseUri(oauth2LoginEndpointBaseUri).and()
                        .loginProcessingUrl(oauth2LoginProcessingUri)
                        .successHandler(new SimpleUrlAuthenticationSuccessHandler(authEnv.redirectBaseUri()))
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler(authEnv.redirectLoginFailureUri()))

        );
    }

    private void logout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl(logoutUri)
                .logoutSuccessUrl(authEnv.redirectBaseUri())
        );
    }

    private void exceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(handling -> handling
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint));
    }

    private void authorizeHttpRequests(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(httpRequests -> httpRequests
                .requestMatchers("/csrf").permitAll()
                .requestMatchers("/auth/v1/**").permitAll()
                // user
                .requestMatchers(HttpMethod.GET, "/user/v1/user/me").hasAnyRole(
                        Role.ROLE_UNAUTHENTICATED.getRole(),
                        Role.ROLE_USER.getRole()
                )
                .requestMatchers(HttpMethod.GET, "/user/v1/user/*").hasAnyRole(
                        Role.ROLE_ADMIN.getRole()
                )
                // identity
                .requestMatchers(HttpMethod.GET, "/identity/v1/identity/me").hasAnyRole(
                        Role.ROLE_USER.getRole()
                )
                .requestMatchers(HttpMethod.PUT, "/identity/v1/identity/me").hasAnyRole(
                        Role.ROLE_USER.getRole()
                )
                .requestMatchers(HttpMethod.POST, "/identity/v1/identity/me").hasAnyRole(
                        Role.ROLE_UNAUTHENTICATED.getRole(),
                        Role.ROLE_USER.getRole(),
                        Role.ROLE_ADMIN.getRole()
                )
                .requestMatchers(
                        HttpMethod.POST,
                        "/identity/v1/identity/me/send-code",
                        "/identity/v1/identity/me/send-code-test",
                        "/identity/me/auth-code").hasAnyRole(
                        Role.ROLE_UNAUTHENTICATED.getRole(),
                        Role.ROLE_USER.getRole()
                )
                .requestMatchers(HttpMethod.GET, "/identity/v1/identity/*").hasAnyRole(
                        Role.ROLE_ADMIN.getRole()
                )
                // application
                .requestMatchers("/application/v1/application/me").hasAnyRole(
                        Role.ROLE_USER.getRole()
                )
                .requestMatchers(HttpMethod.PUT, "/application/v1/final-submit").hasAnyRole(
                        Role.ROLE_USER.getRole()
                )
                .requestMatchers(HttpMethod.POST, "/application/v1/image").hasAnyRole(
                        Role.ROLE_USER.getRole(),
                        Role.ROLE_ADMIN.getRole()
                )
                .requestMatchers(
                        HttpMethod.GET,
                        "/application/v1/ticket",
                        "/application/v1/application/all",
                        "/application/v1/application/*"
                ).hasAnyRole(
                        Role.ROLE_ADMIN.getRole()
                )
                .requestMatchers(HttpMethod.PUT, "/application/v1/status/*").hasAnyRole(
                        Role.ROLE_ADMIN.getRole()
                )
                .anyRequest().permitAll()
        );
    }

}
