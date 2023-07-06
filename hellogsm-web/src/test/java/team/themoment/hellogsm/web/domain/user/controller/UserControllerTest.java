package team.themoment.hellogsm.web.domain.user.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import team.themoment.hellogsm.entity.domain.user.enums.Role;
import team.themoment.hellogsm.web.domain.common.ControllerTestUtil;
import team.themoment.hellogsm.web.domain.identity.controller.IdentityController;
import team.themoment.hellogsm.web.domain.user.dto.domain.UserDto;
import team.themoment.hellogsm.web.domain.user.service.UserByIdQuery;
import team.themoment.hellogsm.web.global.security.auth.AuthenticatedUserManager;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static team.themoment.hellogsm.web.domain.common.ControllerTestUtil.enumAsString;

@Tag("restDocsTest")
@WebMvcTest(controllers = UserController.class)
@ExtendWith(RestDocumentationExtension.class)
class UserControllerTest {

    private RestDocumentationResultHandler documentationHandler;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private UserByIdQuery userByIdQuery;

    @MockBean
    private AuthenticatedUserManager manager;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.documentationHandler = document("user/{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(this.documentationHandler)
                .build();
    }


    protected final FieldDescriptor[] userResponseFields = new FieldDescriptor[]{
            fieldWithPath("id").type(NUMBER).description("USER 식별자"),
            fieldWithPath("provider").type(STRING).description("OAuth2 제공자"),
            fieldWithPath("providerId").type(STRING).description("OAuth2 제공자의 회원 식별자"),
            fieldWithPath("role").type(enumAsString(Role.class)).description("USER 권한")
    };

    @Test
    @DisplayName("인증된 유저 정보로  USER 조회")
    void findByAuthenticated() throws Exception {
        Long id = 1L;
        UserDto user = new UserDto(id, "google", "1234567890", Role.ROLE_USER);
        Mockito.when(userByIdQuery.execute(any(Long.class))).thenReturn(user);
        Mockito.when(manager.getId()).thenReturn(id);

        this.mockMvc.perform(get("/user/v1/user/me")
                        .cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.provider").value(user.provider()))
                .andExpect(jsonPath("$.providerId").value(user.providerId()))
                .andExpect(jsonPath("$.role").value(user.role().name()))
                .andDo(this.documentationHandler.document(
                        ControllerTestUtil.requestSessionCookie(),
                        responseFields(userResponseFields)
                ));
    }

    @Test
    @DisplayName("USER ID로 USER 조회")
    void findByUserId() throws Exception {
        Long id = 1L;
        UserDto user = new UserDto(id, "google", "1234567890", Role.ROLE_USER);
        Mockito.when(userByIdQuery.execute(any(Long.class))).thenReturn(user);

        this.mockMvc.perform(get("/user/v1/user/{userId}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.provider").value(user.provider()))
                .andExpect(jsonPath("$.providerId").value(user.providerId()))
                .andExpect(jsonPath("$.role").value(user.role().name()))
                .andDo(this.documentationHandler.document(
                        pathParameters(parameterWithName("userId").description("조회하고자 하는 USER의 식별자")),
                        responseFields(userResponseFields)
                ));
    }
}
