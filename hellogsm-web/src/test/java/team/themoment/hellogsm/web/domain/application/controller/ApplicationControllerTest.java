package team.themoment.hellogsm.web.domain.application.controller;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cookies.RequestCookiesSnippet;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.hellogsm.entity.domain.application.entity.admission.DesiredMajor;
import team.themoment.hellogsm.entity.domain.application.enums.*;
import team.themoment.hellogsm.web.domain.application.dto.domain.*;
import team.themoment.hellogsm.web.domain.application.dto.request.ApplicationReqDto;
import team.themoment.hellogsm.web.domain.application.dto.response.ApplicationListDto;
import team.themoment.hellogsm.web.domain.application.dto.response.ApplicationListInfoDto;
import team.themoment.hellogsm.web.domain.application.dto.response.ApplicationsDto;
import team.themoment.hellogsm.web.domain.application.dto.request.ApplicationStatusReqDto;
import team.themoment.hellogsm.web.domain.application.dto.response.SingleApplicationRes;
import team.themoment.hellogsm.web.domain.application.dto.response.TicketResDto;
import team.themoment.hellogsm.web.domain.application.service.*;
import team.themoment.hellogsm.web.domain.common.ControllerTestUtil;
import team.themoment.hellogsm.web.global.security.auth.AuthenticatedUserManager;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static team.themoment.hellogsm.web.domain.common.ControllerTestUtil.enumAsString;
import static team.themoment.hellogsm.web.domain.common.ControllerTestUtil.requestSessionCookie;

@Tag("restDocsTest")
@WebMvcTest(controllers = ApplicationController.class)
@ExtendWith(RestDocumentationExtension.class)
class ApplicationControllerTest {

    private RestDocumentationResultHandler documentationHandler;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticatedUserManager manager;
    @MockBean
    private CreateApplicationService createApplicationService;
    @MockBean
    private ModifyApplicationService modifyApplicationService;
    @MockBean
    private QuerySingleApplicationService querySingleApplicationService;
    @MockBean
    private ApplicationListQuery applicationListQuery;
    @MockBean
    private ModifyApplicationStatusService modifyApplicationStatusService;
    @MockBean
    private DeleteApplicationService deleteApplicationService;
    @MockBean
    private QueryTicketsService queryTicketsService;
    @MockBean
    private ImageSaveService imageSaveService;
    @MockBean
    private FinalSubmissionService finalSubmissionService;


    protected final FieldDescriptor[] gedResponseFields = new FieldDescriptor[]{
            fieldWithPath("admissionGrade.totalScore").type(NUMBER).description("총 점수"),
            fieldWithPath("admissionGrade.percentileRank").type(NUMBER).description("백분율 점수"),
            fieldWithPath("admissionGrade.gedTotalScore").type(NUMBER).description("검정고시 점수"),
            fieldWithPath("admissionGrade.gedMaxScore").type(NUMBER).description("검정고시 최대 점수")
    };

    protected final FieldDescriptor[] generalResponseFields = new FieldDescriptor[]{
            fieldWithPath("admissionGrade.totalScore").type(NUMBER).description("총 점수"),
            fieldWithPath("admissionGrade.percentileRank").type(NUMBER).description("백분율 점수"),
            fieldWithPath("admissionGrade.grade1Semester1Score").type(NUMBER).description("1학년 1학기 점수"),
            fieldWithPath("admissionGrade.grade1Semester2Score").type(NUMBER).description("1학년 2학기 점수"),
            fieldWithPath("admissionGrade.grade2Semester1Score").type(NUMBER).description("2학년 1학기 점수"),
            fieldWithPath("admissionGrade.grade2Semester2Score").type(NUMBER).description("2학년 2학기 점수"),
            fieldWithPath("admissionGrade.grade3Semester1Score").type(NUMBER).description("3학년 1학기 점수"),
            fieldWithPath("admissionGrade.artisticScore").type(NUMBER).description("예체능 점수"),
            fieldWithPath("admissionGrade.curricularSubtotalScore").type(NUMBER).description("교과 성적 소계"),
            fieldWithPath("admissionGrade.attendanceScore").type(NUMBER).description("출석 점수"),
            fieldWithPath("admissionGrade.volunteerScore").type(NUMBER).description("봉사 점수"),
            fieldWithPath("admissionGrade.extracurricularSubtotalScore").type(NUMBER).description("비교과 성적 소개")
    };

    protected final FieldDescriptor[] applicationCommonResponseFields = new FieldDescriptor[]{
            fieldWithPath("id").type(NUMBER).description("Application 식별자"),

            fieldWithPath("admissionInfo.applicantName").type(STRING).description("지원자 이름"),
            fieldWithPath("admissionInfo.applicantGender").type(STRING).description("지원자 성별"),
            fieldWithPath("admissionInfo.applicantBirth").type(STRING).description("지원자 생년월일"),
            fieldWithPath("admissionInfo.address").type(STRING).description("지원자 주소"),
            fieldWithPath("admissionInfo.detailAddress").type(STRING).description("지원자 상세 주소"),
            fieldWithPath("admissionInfo.graduation").type(enumAsString(GraduationStatus.class)).description("지원자 졸업 여부"),
            fieldWithPath("admissionInfo.telephone").type(STRING).description("지원자 집전화"),
            fieldWithPath("admissionInfo.applicantPhoneNumber").type(STRING).description("지원자 전화 번호"),
            fieldWithPath("admissionInfo.guardianName").type(STRING).description("보호자 이름"),
            fieldWithPath("admissionInfo.relationWithApplicant").type(STRING).description("지원자와의 관계"),
            fieldWithPath("admissionInfo.guardianPhoneNumber").type(STRING).description("보호자 전화 번호"),
            fieldWithPath("admissionInfo.teacherName").type(STRING).description("지원자의 선생님 이름"),
            fieldWithPath("admissionInfo.teacherPhoneNumber").type(STRING).description("지원자의 선생님 전화 번호"),
            fieldWithPath("admissionInfo.schoolName").type(STRING).description("지원자 중학교 이름"),
            fieldWithPath("admissionInfo.schoolLocation").type(STRING).description("지원자 학교 주소"),
            fieldWithPath("admissionInfo.applicantImageUri").type(STRING).description("지원자 증명사진"),
            fieldWithPath("admissionInfo.desiredMajor.firstDesiredMajor").type(enumAsString(Major.class)).description("지원자 1지망 학과"),
            fieldWithPath("admissionInfo.desiredMajor.secondDesiredMajor").type(enumAsString(Major.class)).description("지원자 2지망 학과"),
            fieldWithPath("admissionInfo.desiredMajor.thirdDesiredMajor").type(enumAsString(Major.class)).description("지원자 3지망 학과"),
            fieldWithPath("admissionInfo.screening").type(enumAsString(Screening.class)).description("지원 전형"),

            fieldWithPath("middleSchoolGrade").type(STRING).description("중학교 점수가 json 문자열 형태로 되어있음"),

            fieldWithPath("admissionStatus.isFinalSubmitted").type(BOOLEAN).description("최종 제출 여부"),
            fieldWithPath("admissionStatus.isPrintsArrived").type(BOOLEAN).description("서류 도착 여부"),
            fieldWithPath("admissionStatus.firstEvaluation").type(enumAsString(EvaluationStatus.class)).description("첫 번째 시험 평가 결과"),
            fieldWithPath("admissionStatus.secondEvaluation").type(enumAsString(EvaluationStatus.class)).description("두 번째 시험 평가 결과"),
            fieldWithPath("admissionStatus.screeningSubmittedAt").type(enumAsString(Screening.class)).description("최종제출 시 전형 상태").optional(),
            fieldWithPath("admissionStatus.screeningFirstEvaluationAt").type(enumAsString(Screening.class)).description("1차 평가 이후 전형 상태").optional(),
            fieldWithPath("admissionStatus.screeningSecondEvaluationAt").type(enumAsString(Screening.class)).description("2차 평가 이후 전형 상태").optional(),
            fieldWithPath("admissionStatus.registrationNumber").type(NUMBER).description("접수 번호").optional(),
            fieldWithPath("admissionStatus.secondScore").type(NUMBER).description("2차 점수").optional(),
            fieldWithPath("admissionStatus.finalMajor").type(enumAsString(Major.class)).description("최종 학과").optional()
    };

    protected final FieldDescriptor[] createRequestFields = new FieldDescriptor[]{
            fieldWithPath("applicantImageUri").type(STRING).description("지원자 증명사진"),
            fieldWithPath("address").type(STRING).description("지원자 집주소"),
            fieldWithPath("detailAddress").type(STRING).description("지원자 상세주소"),
            fieldWithPath("graduation").type(STRING).description("지원자 중학교 졸업 상태"),
            fieldWithPath("telephone").type(STRING).description("지원자 집전화 번호"),
            fieldWithPath("guardianName").type(STRING).description("지원자의 보호자 이름"),
            fieldWithPath("relationWithApplicant").type(STRING).description("지원자와 보호자의 관계"),
            fieldWithPath("guardianPhoneNumber").type(STRING).description("보호자 전화번호"),
            fieldWithPath("teacherName").type(STRING).description("지원자 선생님 이름"),
            fieldWithPath("teacherPhoneNumber").type(STRING).description("지원자 선생님 전화번호"),
            fieldWithPath("firstDesiredMajor").type(STRING).description("1지망 학과"),
            fieldWithPath("secondDesiredMajor").type(STRING).description("2지망 학과"),
            fieldWithPath("thirdDesiredMajor").type(STRING).description("3지망 학과"),
            fieldWithPath("middleSchoolGrade").type(STRING).description("중학교 성적 json 형태로"),
            fieldWithPath("schoolName").type(STRING).description("지원자 학교 이름"),
            fieldWithPath("schoolLocation").type(STRING).description("지원자 학교 위치"),
            fieldWithPath("screening").type(STRING).description("지원 전형")
    };


    ApplicationReqDto applicationReqDto = new ApplicationReqDto(
            "https://naver.com",
            "광주소프트웨어마이스터중학교",
            "이세상 어딘가",
            "GED",
            "01012341234",
            "길동이",
            "부",
            "01012341234",
            "쌤쌤",
            "01012341234",
            "SW",
            "AI",
            "IOT",
            "{\"curriculumScoreSubtotal\":100,\"nonCurriculumScoreSubtotal\":100,\"rankPercentage\":0,\"scoreTotal\":261}",
            "최형우학교",
            "이세상어딘가",
            "GENERAL"
    );

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.documentationHandler = document("application/{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(this.documentationHandler)
                .build();
    }

    SingleApplicationRes createSingleApplicationRes(SuperGrade admissionGrade) {
        String graduation;
        if (admissionGrade instanceof GedAdmissionGradeDto) graduation = "GED";
        else graduation = "CANDIDATE";

        return new SingleApplicationRes(
                1L,
                new AdmissionInfoDto(
                        "human",
                        "MALE",
                        "2023-06-30",
                        "광주광역시 광산구 송정동 상무대로 312",
                        "이세상 어딘가",
                        graduation,
                        "01012341234",
                        "01012341234",
                        "홍길동",
                        "모",
                        "01012341234",
                        "홍길동",
                        "01012341234",
                        "광소마중",
                        "광주 송정동 광소마중",
                        "https://hellogsm.com",
                        DesiredMajor.builder().firstDesiredMajor(Major.SW).secondDesiredMajor(Major.AI).thirdDesiredMajor(Major.IOT).build(),
                        Screening.GENERAL
                ),
                "{\"curriculumScoreSubtotal\":100,\"nonCurriculumScoreSubtotal\":100,\"rankPercentage\":0,\"scoreTotal\":261}",
                admissionGrade,
                new AdmissionStatusDto(
                        false,
                        false,
                        EvaluationStatus.NOT_YET,
                        EvaluationStatus.NOT_YET,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ));
    }

    private ResultActions applicationDtoPerform(MockHttpServletRequestBuilder builder, SingleApplicationRes singleApplicationRes) throws Exception {
        return this.mockMvc.perform(builder.cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(singleApplicationRes.id()))
                .andExpect(jsonPath("$.admissionInfo.applicantName").value(singleApplicationRes.admissionInfo().applicantName()))
                .andExpect(jsonPath("$.admissionInfo.applicantGender").value(singleApplicationRes.admissionInfo().applicantGender()))
                .andExpect(jsonPath("$.admissionInfo.applicantBirth").value(singleApplicationRes.admissionInfo().applicantBirth()))
                .andExpect(jsonPath("$.admissionInfo.address").value(singleApplicationRes.admissionInfo().address()))
                .andExpect(jsonPath("$.admissionInfo.detailAddress").value(singleApplicationRes.admissionInfo().detailAddress()))
                .andExpect(jsonPath("$.admissionInfo.graduation").value(singleApplicationRes.admissionInfo().graduation()))
                .andExpect(jsonPath("$.admissionInfo.telephone").value(singleApplicationRes.admissionInfo().telephone()))
                .andExpect(jsonPath("$.admissionInfo.applicantPhoneNumber").value(singleApplicationRes.admissionInfo().applicantPhoneNumber()))
                .andExpect(jsonPath("$.admissionInfo.guardianName").value(singleApplicationRes.admissionInfo().guardianName()))
                .andExpect(jsonPath("$.admissionInfo.relationWithApplicant").value(singleApplicationRes.admissionInfo().relationWithApplicant()))
                .andExpect(jsonPath("$.admissionInfo.guardianPhoneNumber").value(singleApplicationRes.admissionInfo().guardianPhoneNumber()))
                .andExpect(jsonPath("$.admissionInfo.teacherName").value(singleApplicationRes.admissionInfo().teacherName()))
                .andExpect(jsonPath("$.admissionInfo.teacherPhoneNumber").value(singleApplicationRes.admissionInfo().teacherPhoneNumber()))
                .andExpect(jsonPath("$.admissionInfo.schoolName").value(singleApplicationRes.admissionInfo().schoolName()))
                .andExpect(jsonPath("$.admissionInfo.schoolLocation").value(singleApplicationRes.admissionInfo().schoolLocation()))
                .andExpect(jsonPath("$.admissionInfo.applicantImageUri").value(singleApplicationRes.admissionInfo().applicantImageUri()))

                .andExpect(jsonPath("$.admissionStatus.isFinalSubmitted").value(singleApplicationRes.admissionStatus().isFinalSubmitted()))
                .andExpect(jsonPath("$.admissionStatus.isPrintsArrived").value(singleApplicationRes.admissionStatus().isPrintsArrived()))
                .andExpect(jsonPath("$.admissionStatus.firstEvaluation").value(singleApplicationRes.admissionStatus().firstEvaluation().toString()))
                .andExpect(jsonPath("$.admissionStatus.secondEvaluation").value(singleApplicationRes.admissionStatus().secondEvaluation().toString()))
                .andExpect(jsonPath("$.admissionStatus.screeningSubmittedAt").value(singleApplicationRes.admissionStatus().screeningSubmittedAt()))
                .andExpect(jsonPath("$.admissionStatus.screeningFirstEvaluationAt").value(singleApplicationRes.admissionStatus().screeningFirstEvaluationAt()))
                .andExpect(jsonPath("$.admissionStatus.screeningSecondEvaluationAt").value(singleApplicationRes.admissionStatus().screeningSecondEvaluationAt()))
                .andExpect(jsonPath("$.admissionStatus.registrationNumber").value(singleApplicationRes.admissionStatus().registrationNumber()))
                .andExpect(jsonPath("$.admissionStatus.secondScore").value(singleApplicationRes.admissionStatus().secondScore()))
                .andExpect(jsonPath("$.admissionStatus.finalMajor").value(singleApplicationRes.admissionStatus().finalMajor()))

                .andExpect(jsonPath("$.middleSchoolGrade").value(singleApplicationRes.middleSchoolGrade()));
    }

    @Test
    @DisplayName("USER ID로 원서 단일 조회 (검정고시)")
    void GedReadOne() throws Exception {
        SingleApplicationRes singleApplicationRes = createSingleApplicationRes(new GedAdmissionGradeDto(
                BigDecimal.valueOf(261),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100)
        ));

        Mockito.when(querySingleApplicationService.execute(any(Long.class))).thenReturn(singleApplicationRes);

        GedAdmissionGradeDto admissionGrade = (GedAdmissionGradeDto) singleApplicationRes.admissionGrade();

        applicationDtoPerform(get("/application/v1/application/{userId}", 1L), singleApplicationRes)
                .andExpect(jsonPath("$.admissionGrade.totalScore").value(admissionGrade.totalScore()))
                .andExpect(jsonPath("$.admissionGrade.percentileRank").value(admissionGrade.percentileRank()))
                .andExpect(jsonPath("$.admissionGrade.gedTotalScore").value(admissionGrade.gedTotalScore()))
                .andExpect(jsonPath("$.admissionGrade.gedMaxScore").value(admissionGrade.gedMaxScore()))
                .andDo(this.documentationHandler.document(
                        pathParameters(parameterWithName("userId").description("조회하고자 하는 USER의 식별자")),
                        requestSessionCookie(),
                        responseFields(
                                Stream.concat(
                                        Arrays.stream(applicationCommonResponseFields),
                                        Arrays.stream(gedResponseFields)
                                ).toArray(FieldDescriptor[]::new)
                        )
                ));
    }

    @Test
    @DisplayName("USER ID로 원서 단일 조회 (일반 전형)")
    void GeneralReadOne() throws Exception {
        SingleApplicationRes singleApplicationRes = createSingleApplicationRes(new GeneralAdmissionGradeDto(
                BigDecimal.valueOf(298),
                BigDecimal.valueOf(0.7),
                BigDecimal.valueOf(18),
                BigDecimal.valueOf(36),
                BigDecimal.valueOf(36),
                BigDecimal.valueOf(48),
                BigDecimal.valueOf(64),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(262),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(6),
                BigDecimal.valueOf(36)
        ));

        Mockito.when(querySingleApplicationService.execute(any(Long.class))).thenReturn(singleApplicationRes);

        GeneralAdmissionGradeDto admissionGrade = (GeneralAdmissionGradeDto) singleApplicationRes.admissionGrade();

        applicationDtoPerform(get("/application/v1/application/{userId}", 1L), singleApplicationRes)
                .andExpect(jsonPath("$.admissionGrade.totalScore").value(admissionGrade.totalScore()))
                .andExpect(jsonPath("$.admissionGrade.percentileRank").value(admissionGrade.percentileRank()))
                .andExpect(jsonPath("$.admissionGrade.grade1Semester1Score").value(admissionGrade.grade1Semester1Score()))
                .andExpect(jsonPath("$.admissionGrade.grade1Semester2Score").value(admissionGrade.grade1Semester2Score()))
                .andExpect(jsonPath("$.admissionGrade.grade2Semester1Score").value(admissionGrade.grade2Semester1Score()))
                .andExpect(jsonPath("$.admissionGrade.grade2Semester2Score").value(admissionGrade.grade2Semester2Score()))
                .andExpect(jsonPath("$.admissionGrade.grade3Semester1Score").value(admissionGrade.grade3Semester1Score()))
                .andExpect(jsonPath("$.admissionGrade.artisticScore").value(admissionGrade.artisticScore()))
                .andExpect(jsonPath("$.admissionGrade.curricularSubtotalScore").value(admissionGrade.curricularSubtotalScore()))
                .andExpect(jsonPath("$.admissionGrade.attendanceScore").value(admissionGrade.attendanceScore()))
                .andExpect(jsonPath("$.admissionGrade.volunteerScore").value(admissionGrade.volunteerScore()))
                .andExpect(jsonPath("$.admissionGrade.extracurricularSubtotalScore").value(admissionGrade.extracurricularSubtotalScore()))
                .andDo(this.documentationHandler.document(
                                pathParameters(parameterWithName("userId").description("조회하고자 하는 USER의 식별자")),
                                requestSessionCookie(),
                                responseFields(
                                        Stream.concat(
                                                Arrays.stream(applicationCommonResponseFields),
                                                Arrays.stream(generalResponseFields)
                                        ).toArray(FieldDescriptor[]::new)
                                )
                        )
                );
    }

    @Test
    @DisplayName("자신의 USER ID로 원서 단일 조회 (검정고시)")
    void GedReadMe() throws Exception {
        SingleApplicationRes singleApplicationRes = createSingleApplicationRes(new GedAdmissionGradeDto(
                BigDecimal.valueOf(261),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100)
        ));

        Mockito.when(querySingleApplicationService.execute(any(Long.class))).thenReturn(singleApplicationRes);

        GedAdmissionGradeDto admissionGrade = (GedAdmissionGradeDto) singleApplicationRes.admissionGrade();

        applicationDtoPerform(get("/application/v1/application/me"), singleApplicationRes)
                .andExpect(jsonPath("$.admissionGrade.totalScore").value(admissionGrade.totalScore()))
                .andExpect(jsonPath("$.admissionGrade.percentileRank").value(admissionGrade.percentileRank()))
                .andExpect(jsonPath("$.admissionGrade.gedTotalScore").value(admissionGrade.gedTotalScore()))
                .andExpect(jsonPath("$.admissionGrade.gedMaxScore").value(admissionGrade.gedMaxScore()))
                .andDo(this.documentationHandler.document(
                        responseFields(
                                Stream.concat(
                                        Arrays.stream(applicationCommonResponseFields),
                                        Arrays.stream(gedResponseFields)
                                ).toArray(FieldDescriptor[]::new)
                        )
                ));
    }


    @Test
    @DisplayName("USER ID로 원서 단일 조회 (일반 전형)")
    void GeneralReadMe() throws Exception {
        SingleApplicationRes singleApplicationRes = createSingleApplicationRes(new GeneralAdmissionGradeDto(
                BigDecimal.valueOf(298),
                BigDecimal.valueOf(0.7),
                BigDecimal.valueOf(18),
                BigDecimal.valueOf(36),
                BigDecimal.valueOf(36),
                BigDecimal.valueOf(48),
                BigDecimal.valueOf(64),
                BigDecimal.valueOf(60),
                BigDecimal.valueOf(262),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(6),
                BigDecimal.valueOf(36)
        ));

        Mockito.when(querySingleApplicationService.execute(any(Long.class))).thenReturn(singleApplicationRes);

        GeneralAdmissionGradeDto admissionGrade = (GeneralAdmissionGradeDto) singleApplicationRes.admissionGrade();

        applicationDtoPerform(get("/application/v1/application/me"), singleApplicationRes)
                .andExpect(jsonPath("$.admissionGrade.totalScore").value(admissionGrade.totalScore()))
                .andExpect(jsonPath("$.admissionGrade.percentileRank").value(admissionGrade.percentileRank()))
                .andExpect(jsonPath("$.admissionGrade.grade1Semester1Score").value(admissionGrade.grade1Semester1Score()))
                .andExpect(jsonPath("$.admissionGrade.grade1Semester2Score").value(admissionGrade.grade1Semester2Score()))
                .andExpect(jsonPath("$.admissionGrade.grade2Semester1Score").value(admissionGrade.grade2Semester1Score()))
                .andExpect(jsonPath("$.admissionGrade.grade2Semester2Score").value(admissionGrade.grade2Semester2Score()))
                .andExpect(jsonPath("$.admissionGrade.grade3Semester1Score").value(admissionGrade.grade3Semester1Score()))
                .andExpect(jsonPath("$.admissionGrade.artisticScore").value(admissionGrade.artisticScore()))
                .andExpect(jsonPath("$.admissionGrade.curricularSubtotalScore").value(admissionGrade.curricularSubtotalScore()))
                .andExpect(jsonPath("$.admissionGrade.attendanceScore").value(admissionGrade.attendanceScore()))
                .andExpect(jsonPath("$.admissionGrade.volunteerScore").value(admissionGrade.volunteerScore()))
                .andExpect(jsonPath("$.admissionGrade.extracurricularSubtotalScore").value(admissionGrade.extracurricularSubtotalScore()))
                .andDo(this.documentationHandler.document(
                                responseFields(
                                        Stream.concat(
                                                Arrays.stream(applicationCommonResponseFields),
                                                Arrays.stream(generalResponseFields)
                                        ).toArray(FieldDescriptor[]::new)
                                )
                        )
                );
    }

    @Test
    @DisplayName("원서 생성")
    void create() throws Exception {
        doNothing().when(createApplicationService).execute(any(ApplicationReqDto.class), any(Long.class));

        this.mockMvc.perform(post("/application/v1/application/me")
                        .content(objectMapper.writeValueAsString(applicationReqDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(this.documentationHandler.document(
                        requestSessionCookie(),
                        requestFields(createRequestFields)
                ));
    }

    @Test
    @DisplayName("원서 수정")
    void modify() throws Exception {
        doNothing().when(modifyApplicationService).execute(any(ApplicationReqDto.class), any(Long.class));

        this.mockMvc.perform(put("/application/v1/application/me")
                        .content(objectMapper.writeValueAsString(applicationReqDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(this.documentationHandler.document(
                        requestSessionCookie(),
                        requestFields(createRequestFields)
                ));
    }

    @Test
    @DisplayName("원서 전체 조회")
    void findAll() throws Exception {
        ApplicationListDto applicationListDto = new ApplicationListDto(
                new ApplicationListInfoDto(1),
                List.of(new ApplicationsDto(
                        1L,
                        "human",
                        GraduationStatus.GRADUATE,
                        "01012341234",
                        "01012341234",
                        "휴먼선생",
                        "01012341234",
                        true,
                        true,
                        EvaluationStatus.NOT_YET,
                        EvaluationStatus.NOT_YET,
                        Screening.SPECIAL_VETERANS,
                        Screening.SOCIAL,
                        Screening.GENERAL,
                        1L,
                        "100"
                ))
        );

        Mockito.when(applicationListQuery.execute(any(Integer.class), any(Integer.class))).thenReturn(applicationListDto);

        this.mockMvc.perform(get("/application/v1/application/all")
                        .param("page", "0")
                        .param("size", "1")
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.info.count").value(applicationListDto.info().count()))
                .andExpect(jsonPath("$.applications[0].applicationId").value(applicationListDto.applications().get(0).applicationId()))
                .andExpect(jsonPath("$.applications[0].applicantName").value(applicationListDto.applications().get(0).applicantName()))
                .andExpect(jsonPath("$.applications[0].graduation").value(applicationListDto.applications().get(0).graduation().toString()))
                .andExpect(jsonPath("$.applications[0].applicantPhoneNumber").value(applicationListDto.applications().get(0).applicantPhoneNumber()))
                .andExpect(jsonPath("$.applications[0].guardianPhoneNumber").value(applicationListDto.applications().get(0).guardianPhoneNumber()))
                .andExpect(jsonPath("$.applications[0].teacherName").value(applicationListDto.applications().get(0).teacherName()))
                .andExpect(jsonPath("$.applications[0].teacherPhoneNumber").value(applicationListDto.applications().get(0).teacherPhoneNumber()))
                .andExpect(jsonPath("$.applications[0].isFinalSubmitted").value(applicationListDto.applications().get(0).isFinalSubmitted()))
                .andExpect(jsonPath("$.applications[0].isPrintsArrived").value(applicationListDto.applications().get(0).isPrintsArrived()))
                .andExpect(jsonPath("$.applications[0].firstEvaluation").value(applicationListDto.applications().get(0).firstEvaluation().toString()))
                .andExpect(jsonPath("$.applications[0].secondEvaluation").value(applicationListDto.applications().get(0).secondEvaluation().toString()))
                .andExpect(jsonPath("$.applications[0].registrationNumber").value(applicationListDto.applications().get(0).registrationNumber()))
                .andExpect(jsonPath("$.applications[0].secondScore").value(applicationListDto.applications().get(0).secondScore()))
                .andDo(this.documentationHandler.document(
                        queryParameters(
                                parameterWithName("page").description("페이지"),
                                parameterWithName("size").description("원서 크기")
                        ),
                        requestSessionCookie(),
                        responseFields(
                                fieldWithPath("info.count").type(NUMBER).description("원서 개수"),
                                fieldWithPath("applications[].applicationId").type(NUMBER).description("원서 식별자"),
                                fieldWithPath("applications[].applicantName").type(STRING).description("지원자 이름"),
                                fieldWithPath("applications[].graduation").type(STRING).description("중학교 졸업 상태"),
                                fieldWithPath("applications[].applicantPhoneNumber").type(STRING).description("지원자 전화번호"),
                                fieldWithPath("applications[].guardianPhoneNumber").type(STRING).description("보호자 전화번호"),
                                fieldWithPath("applications[].teacherName").type(STRING).description("선생님 이름"),
                                fieldWithPath("applications[].teacherPhoneNumber").type(STRING).description("선생님 전화번호"),
                                fieldWithPath("applications[].isFinalSubmitted").type(BOOLEAN).description("최종 제출 여부"),
                                fieldWithPath("applications[].isPrintsArrived").type(BOOLEAN).description("서류 도착 여부"),
                                fieldWithPath("applications[].firstEvaluation").type(enumAsString(EvaluationStatus.class)).description("1차 평가 결과"),
                                fieldWithPath("applications[].secondEvaluation").type(enumAsString(EvaluationStatus.class)).description("2차 평가 결과"),
                                fieldWithPath("applications[].screeningSubmittedAt").type(enumAsString(Screening.class)).description("최종제출 시 전형 상태"),
                                fieldWithPath("applications[].screeningFirstEvaluationAt").type(enumAsString(Screening.class)).description("1차 평가 이후 전형 상태"),
                                fieldWithPath("applications[]screeningSecondEvaluationAt").type(enumAsString(Screening.class)).description("2차 평가 이후 전형 상태"),
                                fieldWithPath("applications[].registrationNumber").type(NUMBER).description("접수 번호"),
                                fieldWithPath("applications[].secondScore").type(STRING).description("2차 시험 점수")
                        )
                ));
    }

    @Test
    @DisplayName("원서 상태 수정")
    void modifyStatus() throws Exception {
        ApplicationStatusReqDto applicationStatusReqDto = new ApplicationStatusReqDto(
                true,
                true,
                "NOT_YET",
                "NOT_YET",
                "SPECIAL_VETERANS",
                "SOCIAL",
                "GENERAL",
                1L,
                BigDecimal.valueOf(100),
                "SW"
        );

        doNothing().when(modifyApplicationStatusService).execute(any(Long.class), any(ApplicationStatusReqDto.class));

        this.mockMvc.perform(put("/application/v1/status/{userId}", 1)
                        .content(objectMapper.writeValueAsString(applicationStatusReqDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(this.documentationHandler.document(
                        pathParameters(
                                parameterWithName("userId").description("유저 식별자")
                        ),
                        requestSessionCookie(),
                        requestFields(
                                fieldWithPath("isFinalSubmitted").type(BOOLEAN).description("최종제출 여부"),
                                fieldWithPath("isPrintsArrived").type(BOOLEAN).description("서류 도착 여부"),
                                fieldWithPath("firstEvaluation").type(STRING).description("1차 평과 결과"),
                                fieldWithPath("secondEvaluation").type(STRING).description("2차 평과 결과"),
                                fieldWithPath("screeningSubmittedAt").type(STRING).description("최종제출 시 전형 상태"),
                                fieldWithPath("screeningFirstEvaluationAt").type(STRING).description("1차 평가 이후 전형 상태"),
                                fieldWithPath("screeningSecondEvaluationAt").type(STRING).description("2차 평가 이후 전형 상태"),
                                fieldWithPath("registrationNumber").type(NUMBER).description("접수 번호"),
                                fieldWithPath("secondScore").type(NUMBER).description("2차 평가 점수"),
                                fieldWithPath("finalMajor").type(STRING).description("최종 합격 전공")
                        )
                ));
    }

    @Test
    @DisplayName("원서 삭제")
    void deleteApplication() throws Exception {
        doNothing().when(deleteApplicationService).execute(any(Long.class));

        this.mockMvc.perform(delete("/application/v1/application/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(this.documentationHandler.document(
                        requestSessionCookie()
                ));
    }

    @Test
    @DisplayName("수험표 출력")
    void tickets() throws Exception {
        List<TicketResDto> ticketResDto = List.of(
                new TicketResDto(
                        1L,
                        "누군가",
                        Gender.MALE,
                        "20030423",
                        "https://naver.com",
                        "이세상 어딘가",
                        GraduationStatus.GRADUATE,
                        1L
                )
        );

        Mockito.when(queryTicketsService.execute(any(Integer.class), any(Integer.class))).thenReturn(ticketResDto);

        this.mockMvc.perform(get("/application/v1/tickets")
                        .param("page", "0")
                        .param("size", "1")
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].applicationId").value(ticketResDto.get(0).applicationId()))
                .andExpect(jsonPath("$[0].applicantName").value(ticketResDto.get(0).applicantName()))
                .andExpect(jsonPath("$[0].applicantGender").value(ticketResDto.get(0).applicantGender().toString()))
                .andExpect(jsonPath("$[0].applicantBirth").value(ticketResDto.get(0).applicantBirth()))
                .andExpect(jsonPath("$[0].applicantImageUri").value(ticketResDto.get(0).applicantImageUri()))
                .andExpect(jsonPath("$[0].address").value(ticketResDto.get(0).address()))
                .andExpect(jsonPath("$[0].graduation").value(ticketResDto.get(0).graduation().toString()))
                .andExpect(jsonPath("$[0].registrationNumber").value(ticketResDto.get(0).registrationNumber()))
                .andDo(this.documentationHandler.document(
                        queryParameters(
                                parameterWithName("page").description("페이지"),
                                parameterWithName("size").description("원서 크기")
                        ),
                        responseFields(
                                fieldWithPath("[].applicationId").type(NUMBER).description("원서 식별자"),
                                fieldWithPath("[].applicantName").type(STRING).description("지원자 이름"),
                                fieldWithPath("[].applicantGender").type(STRING).description("지원자 성별"),
                                fieldWithPath("[].applicantBirth").type(STRING).description("지원자 생년월일"),
                                fieldWithPath("[].applicantImageUri").type(STRING).description("지원자 증명사진"),
                                fieldWithPath("[].address").type(STRING).description("지원자 주소"),
                                fieldWithPath("[].graduation").type(STRING).description("지원자 졸업 상태"),
                                fieldWithPath("[].registrationNumber").type(NUMBER).description("접수 번호")
                        )
                ));
    }

    @Test
    @DisplayName("증명사진 업로드")
    void uploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png",
                "<<image data>>".getBytes());

        Mockito.when(imageSaveService.execute(any(MultipartFile.class))).thenReturn("https://hellogsm.kr/image.png");

        this.mockMvc.perform(multipart("/application/v1/image")
                        .file(file)
                        .cookie(new Cookie("SESSION", "SESSIONID12345"))
                        .contentType(MediaType.MULTIPART_FORM_DATA)

                )
                .andDo(this.documentationHandler.document(
                        requestParts(
                                partWithName("file").description("이미지 파일")
                        ),
                        responseFields(
                                fieldWithPath("url").type(STRING).description("이미지 url")
                        )
                ));
    }

    @Test
    @DisplayName("최종 제출")
    void finalSubmission() throws Exception {
        doNothing().when(finalSubmissionService).execute(any(Long.class));

        this.mockMvc.perform(put("/application/v1/final-submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("SESSION", "SESSIONID12345")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(this.documentationHandler.document(
                        requestSessionCookie()
                ));
    }
}
