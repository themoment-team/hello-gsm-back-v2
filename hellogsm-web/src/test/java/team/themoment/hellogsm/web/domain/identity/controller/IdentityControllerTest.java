package team.themoment.hellogsm.web.domain.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import team.themoment.hellogsm.entity.domain.application.enums.Gender;
import team.themoment.hellogsm.web.domain.common.ControllerTestUtil;
import team.themoment.hellogsm.web.domain.identity.dto.domain.IdentityDto;
import team.themoment.hellogsm.web.domain.identity.dto.request.IdentityReqDto;
import team.themoment.hellogsm.web.domain.identity.service.CreateIdentityService;
import team.themoment.hellogsm.web.domain.identity.service.IdentityQuery;
import team.themoment.hellogsm.web.domain.identity.service.ModifyIdentityService;
import team.themoment.hellogsm.web.global.security.auth.AuthenticatedUserManager;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
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
import static team.themoment.hellogsm.web.domain.common.ControllerTestUtil.requestSessionCookie;

@WebMvcTest(controllers = IdentityController.class)
@ExtendWith(RestDocumentationExtension.class)
class IdentityControllerTest {
    private RestDocumentationResultHandler documentationHandler;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthenticatedUserManager manager;
    @MockBean
    private CreateIdentityService createIdentityService;
    @MockBean
    private ModifyIdentityService modifyIdentityService;
    @MockBean
    private IdentityQuery identityQuery;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.documentationHandler = document("identity/identity/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(this.documentationHandler)
                .build();
    }

    protected final FieldDescriptor[] identityResponseFields = new FieldDescriptor[]{
            fieldWithPath("id").type(NUMBER).description("IDENTITY 식별자"),
            fieldWithPath("name").type(STRING).description("USER의 이름"),
            fieldWithPath("phoneNumber").type(STRING).description("USER의 휴대전화 번호"),
            fieldWithPath("birth").type(STRING).description("USER의 생년월일"),
            fieldWithPath("gender").type(enumAsString(Gender.class)).description("USER의 성별"),
            fieldWithPath("userId").type(NUMBER).description("USER 식별자")
    };

    Long userId = 1L;
    IdentityDto identity = new IdentityDto(
            1L,
            "홍길동",
            "01012345678",
            LocalDate.of(2007,01,01),
            Gender.MALE,
            userId
    );

    private ResultActions identityDtoPerform(MockHttpServletRequestBuilder builder) throws Exception {
        return this.mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(identity.id()))
                .andExpect(jsonPath("$.name").value(identity.name()))
                .andExpect(jsonPath("$.phoneNumber").value(identity.phoneNumber()))
                .andExpect(jsonPath("$.birth").value(identity.birth().toString()))
                .andExpect(jsonPath("$.gender").value(identity.gender().name()))
                .andExpect(jsonPath("$.userId").value(identity.userId()));
    }

    @Test
    @DisplayName("SESSION 정보로 IDENTITY 조회")
    void findByAuthenticated() throws Exception {
        Mockito.when(identityQuery.execute(any(Long.class))).thenReturn(identity);
        identityDtoPerform(get("/identity/v1/identity/me")
                .cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andDo(this.documentationHandler.document(
                        requestSessionCookie(),
                        responseFields(identityResponseFields)
                ));
    }

    @Test
    @DisplayName("USER ID로 IDENTITY 조회")
    void findByUserId() throws Exception {
        Mockito.when(identityQuery.execute(any(Long.class))).thenReturn(identity);
        Mockito.when(manager.getId()).thenReturn(userId);
        identityDtoPerform(get("/identity/v1/identity/{userId}", userId))
                .andDo(this.documentationHandler.document(
                        pathParameters(parameterWithName("userId").description("조회하고자 하는 USER의 식별자")),
                        responseFields(identityResponseFields)
                ));
    }

    @Test
    @DisplayName("SESSION 정보로 IDENTITY 생성")
    void createByAuthenticated() throws Exception {
        IdentityReqDto request = new IdentityReqDto(
                "123456",
                "홍길동",
                "01012345678",
                Gender.MALE.name(),
                LocalDate.of(2007,01,01)
        );
        Mockito.when(createIdentityService.execute(any(IdentityReqDto.class), any(Long.class))).thenReturn(identity);
        Mockito.when(manager.getId()).thenReturn(userId);
        this.mockMvc.perform(post("/identity/v1/identity/me", userId, MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isSeeOther())
                .andDo(this.documentationHandler.document(
                        requestSessionCookie()
                ));
    }

    @Test
    @DisplayName("SESSION 정보로 IDENTITY 수정")
    void modifyByAuthenticated() throws Exception {
        IdentityReqDto request = new IdentityReqDto(
                "123456",
                "홍길동",
                "01012345678",
                Gender.MALE.name(),
                LocalDate.of(2007,01,01)
        );
        Mockito.when(modifyIdentityService.execute(any(IdentityReqDto.class), any(Long.class))).thenReturn(identity);
        Mockito.when(manager.getId()).thenReturn(userId);
        this.mockMvc.perform(post("/identity/v1/identity/me", userId, MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isSeeOther())
                .andDo(this.documentationHandler.document(
                        requestSessionCookie()
                ));
    }
}
