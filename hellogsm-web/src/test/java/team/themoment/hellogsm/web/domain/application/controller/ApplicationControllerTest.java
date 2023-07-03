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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import team.themoment.hellogsm.entity.domain.application.entity.admission.DesiredMajor;
import team.themoment.hellogsm.entity.domain.application.enums.*;
import team.themoment.hellogsm.web.domain.application.dto.domain.*;
import team.themoment.hellogsm.web.domain.application.dto.response.SingleApplicationRes;
import team.themoment.hellogsm.web.domain.application.service.*;
import team.themoment.hellogsm.web.global.security.auth.AuthenticatedUserManager;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    ApplicationControllerTest() {
    }

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
                        null
                ));
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

        this.mockMvc.perform(get("/application/v1/application/{userId}", 1L)
                .cookie(new Cookie("SESSION", "SESSIONID12345")))
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
                .andExpect(jsonPath("$.admissionStatus.registrationNumber").value(singleApplicationRes.admissionStatus().registrationNumber()))
                .andExpect(jsonPath("$.admissionStatus.secondScore").value(singleApplicationRes.admissionStatus().secondScore()))
                .andExpect(jsonPath("$.admissionStatus.finalMajor").value(singleApplicationRes.admissionStatus().finalMajor()))

                .andExpect(jsonPath("$.middleSchoolGrade").value(singleApplicationRes.middleSchoolGrade()))

                .andExpect(jsonPath("$.admissionGrade.totalScore").value(admissionGrade.totalScore()))
                .andExpect(jsonPath("$.admissionGrade.percentileRank").value(admissionGrade.percentileRank()))
                .andExpect(jsonPath("$.admissionGrade.gedTotalScore").value(admissionGrade.gedTotalScore()))
                .andExpect(jsonPath("$.admissionGrade.gedMaxScore").value(admissionGrade.gedMaxScore()))
                .andDo(this.documentationHandler.document(
                        pathParameters(parameterWithName("userId").description("조회하고자 하는 USER의 식별자")),
                        responseFields(
                                fieldWithPath("id").type(NUMBER).description("Application 식별자"),

                                fieldWithPath("admissionInfo.applicantName").type(STRING).description("지원자 이름"),
                                fieldWithPath("admissionInfo.applicantGender").type(STRING).description("지원자 성별"),
                                fieldWithPath("admissionInfo.applicantBirth").type(STRING).description("지원자 생년월일"),
                                fieldWithPath("admissionInfo.address").type(STRING).description("지원자 주소"),
                                fieldWithPath("admissionInfo.detailAddress").type(STRING).description("지원자 상세 주소"),
                                fieldWithPath("admissionInfo.graduation").type(GraduationStatus.class).description("지원자 졸업 여부"),
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
                                fieldWithPath("admissionInfo.desiredMajor.firstDesiredMajor").type(STRING).description("지원자 1지망 학과"),
                                fieldWithPath("admissionInfo.desiredMajor.secondDesiredMajor").type(STRING).description("지원자 2지망 학과"),
                                fieldWithPath("admissionInfo.desiredMajor.thirdDesiredMajor").type(STRING).description("지원자 3지망 학과"),
                                fieldWithPath("admissionInfo.screening").type(Screening.class).description("지원 전형"),

                                fieldWithPath("middleSchoolGrade").type(STRING).description("중학교 점수가 json 문자열 형태로 되어있음"),

                                fieldWithPath("admissionGrade.totalScore").type(NUMBER).description("총 점수"),
                                fieldWithPath("admissionGrade.percentileRank").type(NUMBER).description("백분율 점수"),
                                fieldWithPath("admissionGrade.gedTotalScore").type(NUMBER).description("검정고시 점수"),
                                fieldWithPath("admissionGrade.gedMaxScore").type(NUMBER).description("검정고시 최대 점수"),

                                fieldWithPath("admissionStatus.isFinalSubmitted").type(BOOLEAN).description("최종 제출 여부"),
                                fieldWithPath("admissionStatus.isPrintsArrived").type(BOOLEAN).description("서류 도착 여부"),
                                fieldWithPath("admissionStatus.firstEvaluation").type(STRING).description("첫 번째 시험 평가 결과"),
                                fieldWithPath("admissionStatus.secondEvaluation").type(STRING).description("두 번째 시험 평가 결과"),
                                fieldWithPath("admissionStatus.registrationNumber").type(NUMBER).description("접수 번호").optional(),
                                fieldWithPath("admissionStatus.secondScore").type(NUMBER).description("2차 점수").optional(),
                                fieldWithPath("admissionStatus.finalMajor").type(STRING).description("최종 학과").optional()
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

        this.mockMvc.perform(get("/application/v1/application/{userId}", 1L)
                .cookie(new Cookie("SESSION", "SESSIONID12345")))
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
                .andExpect(jsonPath("$.admissionStatus.registrationNumber").value(singleApplicationRes.admissionStatus().registrationNumber()))
                .andExpect(jsonPath("$.admissionStatus.secondScore").value(singleApplicationRes.admissionStatus().secondScore()))
                .andExpect(jsonPath("$.admissionStatus.finalMajor").value(singleApplicationRes.admissionStatus().finalMajor()))

                .andExpect(jsonPath("$.middleSchoolGrade").value(singleApplicationRes.middleSchoolGrade()))

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
                        requestCookies(cookieWithName("SESSION").description("사용자의 SESSION ID, 브라우저로 접근 시 자동 생성됩니다.")),
                        responseFields(
                                fieldWithPath("id").type(NUMBER).description("Application 식별자"),

                                fieldWithPath("admissionInfo.applicantName").type(STRING).description("지원자 이름"),
                                fieldWithPath("admissionInfo.applicantGender").type(STRING).description("지원자 성별"),
                                fieldWithPath("admissionInfo.applicantBirth").type(STRING).description("지원자 생년월일"),
                                fieldWithPath("admissionInfo.address").type(STRING).description("지원자 주소"),
                                fieldWithPath("admissionInfo.detailAddress").type(STRING).description("지원자 상세 주소"),
                                fieldWithPath("admissionInfo.graduation").type(GraduationStatus.class).description("지원자 졸업 여부"),
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
                                fieldWithPath("admissionInfo.desiredMajor.firstDesiredMajor").type(STRING).description("지원자 1지망 학과"),
                                fieldWithPath("admissionInfo.desiredMajor.secondDesiredMajor").type(STRING).description("지원자 2지망 학과"),
                                fieldWithPath("admissionInfo.desiredMajor.thirdDesiredMajor").type(STRING).description("지원자 3지망 학과"),
                                fieldWithPath("admissionInfo.screening").type(Screening.class).description("지원 전형"),

                                fieldWithPath("middleSchoolGrade").type(STRING).description("중학교 점수가 json 문자열 형태로 되어있음"),

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
                                fieldWithPath("admissionGrade.extracurricularSubtotalScore").type(NUMBER).description("비교과 성적 소개"),

                                fieldWithPath("admissionStatus.isFinalSubmitted").type(BOOLEAN).description("최종 제출 여부"),
                                fieldWithPath("admissionStatus.isPrintsArrived").type(BOOLEAN).description("서류 도착 여부"),
                                fieldWithPath("admissionStatus.firstEvaluation").type(EvaluationStatus.class).description("첫 번째 시험 평가 결과"),
                                fieldWithPath("admissionStatus.secondEvaluation").type(EvaluationStatus.class).description("두 번째 시험 평가 결과"),
                                fieldWithPath("admissionStatus.registrationNumber").type(NUMBER).description("접수 번호").optional(),
                                fieldWithPath("admissionStatus.secondScore").type(NUMBER).description("2차 점수").optional(),
                                fieldWithPath("admissionStatus.finalMajor").type(Major.class).description("최종 학과").optional()
                        )
                )
        );
    }
}