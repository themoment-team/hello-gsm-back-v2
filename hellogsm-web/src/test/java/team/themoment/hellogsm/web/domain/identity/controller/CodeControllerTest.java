package team.themoment.hellogsm.web.domain.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static team.themoment.hellogsm.web.domain.common.ControllerTestUtil.requestSessionCookie;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.http.Cookie;
import team.themoment.hellogsm.web.domain.common.ControllerTestUtil;
import team.themoment.hellogsm.web.domain.identity.dto.request.AuthenticateCodeReqDto;
import team.themoment.hellogsm.web.domain.identity.dto.request.GenerateCodeReqDto;
import team.themoment.hellogsm.web.domain.identity.service.AuthenticateCodeService;
import team.themoment.hellogsm.web.domain.identity.service.GenerateCodeService;
import team.themoment.hellogsm.web.global.security.auth.AuthenticatedUserManager;

@WebMvcTest(controllers = CodeController.class)
@ExtendWith(RestDocumentationExtension.class)
class CodeControllerTest {
    private RestDocumentationResultHandler documentationHandler;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthenticatedUserManager manager;
    @MockBean
    private GenerateCodeService generateCodeService;
    @MockBean
    private AuthenticateCodeService authenticateCodeService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.documentationHandler = document("identity/code/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(this.documentationHandler)
                .build();
    }

    @Test
    @DisplayName("SESSION 정보로 인증 CODE 생성")
    void sendCode() throws Exception {
        Long userId = 1L;
        GenerateCodeReqDto request = new GenerateCodeReqDto("0101234678");
        String generatedCode = "123456";
        Mockito.when(generateCodeService.execute(any(Long.class), any(GenerateCodeReqDto.class))).thenReturn(generatedCode);
        Mockito.when(manager.getId()).thenReturn(userId);

        this.mockMvc.perform(post("/identity/v1/identity/me/send-code", userId, MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestSessionCookie()
                ));
    }

    @Test
    @DisplayName("[테스트 용] SESSION 정보로 인증 CODE 생성 및 반환")
    void sendCodeTest() throws Exception {
        Long userId = 1L;
        GenerateCodeReqDto request = new GenerateCodeReqDto("0101234678");
        String generatedCode = "123456";
        Mockito.when(generateCodeService.execute(any(Long.class), any(GenerateCodeReqDto.class))).thenReturn(generatedCode);
        Mockito.when(manager.getId()).thenReturn(userId);

        this.mockMvc.perform(post("/identity/v1/identity/me/send-code-test", userId, MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestSessionCookie()
                ));
    }

    @Test
    @DisplayName("SESSION 정보로 인증 CODE 인증")
    void authCode() throws Exception {
        Long userId = 1L;
        String generatedCode = "123456";
        AuthenticateCodeReqDto request = new AuthenticateCodeReqDto(generatedCode);
        doNothing().when(authenticateCodeService).execute(any(Long.class), any(AuthenticateCodeReqDto.class));
        Mockito.when(manager.getId()).thenReturn(userId);

        this.mockMvc.perform(post("/identity/v1/identity/me/auth-code", userId, MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        requestSessionCookie()
                ));
    }
}
