package team.themoment.hellogsm.web.domain.application.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import io.micrometer.common.lang.Nullable;
import team.themoment.hellogsm.entity.domain.application.entity.Application;
import team.themoment.hellogsm.entity.domain.application.entity.admission.AdmissionInfo;
import team.themoment.hellogsm.entity.domain.application.entity.admission.DesiredMajor;
import team.themoment.hellogsm.entity.domain.application.entity.grade.GedAdmissionGrade;
import team.themoment.hellogsm.entity.domain.application.entity.grade.GraduateAdmissionGrade;
import team.themoment.hellogsm.entity.domain.application.entity.grade.MiddleSchoolGrade;
import team.themoment.hellogsm.entity.domain.application.entity.status.AdmissionStatus;
import team.themoment.hellogsm.entity.domain.application.enums.EvaluationStatus;
import team.themoment.hellogsm.entity.domain.application.enums.GraduationStatus;
import team.themoment.hellogsm.entity.domain.application.enums.Major;
import team.themoment.hellogsm.entity.domain.application.enums.Screening;
import team.themoment.hellogsm.web.domain.application.dto.domain.*;
import team.themoment.hellogsm.web.domain.application.dto.request.ApplicationReqDto;
import team.themoment.hellogsm.web.domain.application.dto.request.ApplicationStatusReqDto;
import team.themoment.hellogsm.web.domain.application.dto.response.ApplicationListDto;
import team.themoment.hellogsm.web.domain.application.dto.response.ApplicationListInfoDto;
import team.themoment.hellogsm.web.domain.application.dto.response.ApplicationsDto;
import team.themoment.hellogsm.web.domain.application.dto.response.SingleApplicationRes;
import team.themoment.hellogsm.web.domain.application.dto.response.TicketResDto;
import team.themoment.hellogsm.web.domain.identity.dto.domain.IdentityDto;
import team.themoment.hellogsm.web.domain.identity.dto.request.IdentityReqDto;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        typeConversionPolicy = ReportingPolicy.WARN
)
public interface ApplicationMapper {
    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);


    default SingleApplicationRes createSingleApplicationDto(Application application) {
        AdmissionInfoDto admissionInfo = ApplicationMapper.INSTANCE.admissionInfoToAdmissionInfoDto(application.getAdmissionInfo());
        AdmissionStatusDto admissionStatus = ApplicationMapper.INSTANCE.admissionStatusToAdmissionStatusDto(application.getAdmissionStatus());
        SuperGrade admissionGrade;


        if (application.getAdmissionInfo().getGraduation().equals(GraduationStatus.GED)) {
            admissionGrade = gedAdmissionGradeToGedAdmissionGradeDto((GedAdmissionGrade) application.getAdmissionGrade());
        } else {
            admissionGrade = graduateAdmissionGradeToAdmissionGradeDto((GraduateAdmissionGrade) application.getAdmissionGrade());
        }

        // TODO admissionGrade가 SuperGrade 타입이면 막기

        return new SingleApplicationRes(
                application.getId(),
                admissionInfo,
                application.getMiddleSchoolGrade().getMiddleSchoolGradeText(),
                admissionGrade,
                admissionStatus
        );
    }

    @BeanMapping(ignoreUnmappedSourceProperties = "id")
    @Mappings({
            @Mapping(source = "totalScore", target = "totalScore"),
            @Mapping(source = "percentileRank", target = "percentileRank"),
            @Mapping(source = "grade1Semester1Score", target = "grade1Semester1Score"),
            @Mapping(source = "grade1Semester2Score", target = "grade1Semester2Score"),
            @Mapping(source = "grade2Semester1Score", target = "grade2Semester1Score"),
            @Mapping(source = "grade2Semester2Score", target = "grade2Semester2Score"),
            @Mapping(source = "grade3Semester1Score", target = "grade3Semester1Score"),
            @Mapping(source = "artisticScore", target = "artisticScore"),
            @Mapping(source = "curricularSubtotalScore", target = "curricularSubtotalScore"),
            @Mapping(source = "attendanceScore", target = "attendanceScore"),
            @Mapping(source = "volunteerScore", target = "volunteerScore"),
            @Mapping(source = "extracurricularSubtotalScore", target = "extracurricularSubtotalScore"),
    })
    GeneralAdmissionGradeDto graduateAdmissionGradeToAdmissionGradeDto(GraduateAdmissionGrade admissionGrade);

    @BeanMapping(ignoreUnmappedSourceProperties = "id")
    @Mappings({
            @Mapping(source = "totalScore", target = "totalScore"),
            @Mapping(source = "percentileRank", target = "percentileRank"),
            @Mapping(source = "gedTotalScore", target = "gedTotalScore"),
            @Mapping(source = "gedMaxScore", target = "gedMaxScore"),
    })
    GedAdmissionGradeDto gedAdmissionGradeToGedAdmissionGradeDto(GedAdmissionGrade admissionGrade);

    @BeanMapping(ignoreUnmappedSourceProperties = {"id"})
    @Mappings({
            @Mapping(source = "applicantName", target = "applicantName"),
            @Mapping(source = "applicantGender", target = "applicantGender"),
            @Mapping(source = "applicantBirth", target = "applicantBirth"),
            @Mapping(source = "address", target = "address"),
            @Mapping(source = "detailAddress", target = "detailAddress"),
            @Mapping(source = "graduation", target = "graduation"),
            @Mapping(source = "telephone", target = "telephone"),
            @Mapping(source = "applicantPhoneNumber", target = "applicantPhoneNumber"),
            @Mapping(source = "guardianName", target = "guardianName"),
            @Mapping(source = "relationWithApplicant", target = "relationWithApplicant"),
            @Mapping(source = "guardianPhoneNumber", target = "guardianPhoneNumber"),
            @Mapping(source = "teacherName", target = "teacherName"),
            @Mapping(source = "teacherPhoneNumber", target = "teacherPhoneNumber"),
            @Mapping(source = "schoolName", target = "schoolName"),
            @Mapping(source = "schoolLocation", target = "schoolLocation"),
            @Mapping(source = "applicantImageUri", target = "applicantImageUri"),
            @Mapping(source = "screening", target = "screening"),
            @Mapping(source = "desiredMajor", target = "desiredMajor"),
    })
    AdmissionInfoDto admissionInfoToAdmissionInfoDto(AdmissionInfo admissionInfo);

    @BeanMapping(ignoreUnmappedSourceProperties = {"id", "isFinalSubmitted", "isPrintsArrived"})
    @Mappings({
            @Mapping(source = "finalSubmitted", target = "isFinalSubmitted"),
            @Mapping(source = "printsArrived", target = "isPrintsArrived"),
            @Mapping(source = "firstEvaluation", target = "firstEvaluation"),
            @Mapping(source = "secondEvaluation", target = "secondEvaluation"),
            @Mapping(source = "screeningSubmittedAt", target = "screeningSubmittedAt"),
            @Mapping(source = "screeningFirstEvaluationAt", target = "screeningFirstEvaluationAt"),
            @Mapping(source = "screeningSecondEvaluationAt", target = "screeningSecondEvaluationAt"),
            @Mapping(source = "registrationNumber", target = "registrationNumber"),
            @Mapping(source = "secondScore", target = "secondScore"),
            @Mapping(source = "finalMajor", target = "finalMajor"),
    })
    AdmissionStatusDto admissionStatusToAdmissionStatusDto(AdmissionStatus admissionStatus);

    @BeanMapping(ignoreUnmappedSourceProperties = {"middleSchoolGrade", "admissionGrade", "userId"})
    @Mappings({
            @Mapping(source = "id", target = "applicationId"),
            @Mapping(source = "admissionInfo.applicantName", target = "applicantName"),
            @Mapping(source = "admissionInfo.applicantGender", target = "applicantGender"),
            @Mapping(source = "admissionInfo.applicantBirth", target = "applicantBirth"),
            @Mapping(source = "admissionInfo.applicantImageUri", target = "applicantImageUri"),
            @Mapping(source = "admissionInfo.address", target = "address"),
            @Mapping(source = "admissionInfo.graduation", target = "graduation"),
            @Mapping(source = "admissionStatus.registrationNumber", target = "registrationNumber"),
    })
    TicketResDto ApplicationToTicketResDto(Application application);

    List<TicketResDto> ApplicationListToTicketResDtoList(List<Application> applicationList);

    default ApplicationListDto createApplicationListDto(List<Application> applicationList) {
        return new ApplicationListDto(
                new ApplicationListInfoDto(applicationList.size()),
                applicationListToApplicationsDtoList(applicationList)
        );
    }

    @BeanMapping(ignoreUnmappedSourceProperties = {"middleSchoolGrade", "admissionGrade", "userId"})
    @Mappings({
            @Mapping(source = "id", target = "applicationId"),
            @Mapping(source = "admissionInfo.applicantName", target = "applicantName"),
            @Mapping(source = "admissionInfo.graduation", target = "graduation"),
            @Mapping(source = "admissionInfo.applicantPhoneNumber", target = "applicantPhoneNumber"),
            @Mapping(source = "admissionInfo.guardianPhoneNumber", target = "guardianPhoneNumber"),
            @Mapping(source = "admissionInfo.teacherName", target = "teacherName"),
            @Mapping(source = "admissionInfo.teacherPhoneNumber", target = "teacherPhoneNumber"),
            @Mapping(source = "admissionStatus.isFinalSubmitted", target = "isFinalSubmitted"),
            @Mapping(source = "admissionStatus.isPrintsArrived", target = "isPrintsArrived"),
            @Mapping(source = "admissionStatus.firstEvaluation", target = "firstEvaluation"),
            @Mapping(source = "admissionStatus.secondEvaluation", target = "secondEvaluation"),
            @Mapping(source = "admissionStatus.screeningSubmittedAt", target = "screeningSubmittedAt"),
            @Mapping(source = "admissionStatus.screeningFirstEvaluationAt", target = "screeningFirstEvaluationAt"),
            @Mapping(source = "admissionStatus.screeningSecondEvaluationAt", target = "screeningSecondEvaluationAt"),
            @Mapping(source = "admissionStatus.registrationNumber", target = "registrationNumber"),
            @Mapping(source = "admissionStatus.secondScore", target = "secondScore"),
    })
    ApplicationsDto applicationToApplicationsDto(Application applicationList);

    default AdmissionStatus createNewAdmissionStatus(Long admissionStatusId, ApplicationStatusReqDto applicationStatusReqDto) {
        Major finalMajor = null;
        Screening screeningSubmittedAt = null;
        Screening screeningFirstEvaluationAt = null;
        Screening screeningSecondEvaluationAt = null;

        try {
            if(applicationStatusReqDto.finalMajor() != null)
                finalMajor = Major.valueOf(applicationStatusReqDto.finalMajor());
            if(applicationStatusReqDto.screeningSubmittedAt() != null)
                screeningSubmittedAt = Screening.valueOf(applicationStatusReqDto.screeningSubmittedAt());
            if(applicationStatusReqDto.screeningFirstEvaluationAt() != null)
                screeningFirstEvaluationAt = Screening.valueOf(applicationStatusReqDto.screeningFirstEvaluationAt());
            if(applicationStatusReqDto.screeningSecondEvaluationAt() != null)
                screeningSecondEvaluationAt = Screening.valueOf(applicationStatusReqDto.screeningSecondEvaluationAt());
        } catch (Exception ex) {
            throw new RuntimeException("예상하지 못한 에러 발생",ex);
        }

        return AdmissionStatus.builder()
                .id(admissionStatusId)
                .isPrintsArrived(applicationStatusReqDto.isPrintsArrived())
                .firstEvaluation(EvaluationStatus.valueOf(applicationStatusReqDto.firstEvaluation()))
                .secondEvaluation(EvaluationStatus.valueOf(applicationStatusReqDto.secondEvaluation()))
                .isFinalSubmitted(applicationStatusReqDto.isFinalSubmitted())
                .registrationNumber(applicationStatusReqDto.registrationNumber())
                .screeningSubmittedAt(screeningSubmittedAt)
                .screeningFirstEvaluationAt(screeningFirstEvaluationAt)
                .screeningSecondEvaluationAt(screeningSecondEvaluationAt)
                .finalMajor(finalMajor)
                .secondScore(applicationStatusReqDto.secondScore())
                .build();
    }

    List<ApplicationsDto> applicationListToApplicationsDtoList(List<Application> applicationList);


    default AdmissionStatus updateFinalSubmission(AdmissionStatus admissionStatus) {
        return AdmissionStatus.builder()
                .id(admissionStatus.getId())
                .isFinalSubmitted(true)
                .isPrintsArrived(admissionStatus.isPrintsArrived())
                .firstEvaluation(admissionStatus.getFirstEvaluation())
                .secondEvaluation(admissionStatus.getSecondEvaluation())
                .registrationNumber(admissionStatus.getRegistrationNumber())
                .screeningSubmittedAt(admissionStatus.getScreeningSubmittedAt())
                .screeningFirstEvaluationAt(admissionStatus.getScreeningFirstEvaluationAt())
                .screeningSecondEvaluationAt(admissionStatus.getScreeningSecondEvaluationAt())
                .secondScore(admissionStatus.getSecondScore())
                .finalMajor(admissionStatus.getFinalMajor())
                .build();
    }


    default Application applicationReqDtoAndIdentityDtoToApplication(ApplicationReqDto applicationReqDto, IdentityDto identityDto, Long userId, @Nullable Long appId) {

        DesiredMajor desiredMajor = DesiredMajor.builder()
                .firstDesiredMajor(Major.valueOf(applicationReqDto.firstDesiredMajor()))
                .secondDesiredMajor(Major.valueOf(applicationReqDto.secondDesiredMajor()))
                .thirdDesiredMajor(Major.valueOf(applicationReqDto.thirdDesiredMajor()))
                .build();

        AdmissionInfo admissionInfo = AdmissionInfo.builder()
                .id(userId)
                .applicantImageUri(applicationReqDto.applicantImageUri())
                .applicantName(identityDto.name())
                .applicantGender(identityDto.gender())
                .applicantBirth(identityDto.birth())
                .address(applicationReqDto.address())
                .detailAddress(applicationReqDto.detailAddress())
                .graduation(GraduationStatus.valueOf(applicationReqDto.graduation()))
                .telephone(applicationReqDto.telephone())
                .applicantPhoneNumber(identityDto.phoneNumber())
                .guardianName(applicationReqDto.guardianName())
                .relationWithApplicant(applicationReqDto.relationWithApplicant())
                .guardianPhoneNumber(applicationReqDto.guardianPhoneNumber())
                .teacherName(applicationReqDto.teacherName())
                .teacherPhoneNumber(applicationReqDto.teacherPhoneNumber())
                .screening(Screening.valueOf(applicationReqDto.screening()))
                .desiredMajor(desiredMajor)
                .build();

        AdmissionStatus admissionStatus = AdmissionStatus.init(userId);
        MiddleSchoolGrade middleSchoolGrade = new MiddleSchoolGrade(userId, applicationReqDto.middleSchoolGrade());

        return new Application(
                userId,
                admissionInfo,
                admissionStatus,
                middleSchoolGrade,
                userId
        );
    }

    @BeanMapping(ignoreUnmappedSourceProperties = {
            "applicantName",
            "applicantGender",
            "applicantBirth",
            "applicantPhoneNumber",
            "code"
    })
    @Mappings({
            @Mapping(source = "admissionInfo.id", target = "id"),
            @Mapping(source = "admissionInfo.applicantImageUri", target = "applicantImageUri"),
            @Mapping(source = "identityReqDto.name", target = "applicantName"),
            @Mapping(source = "identityReqDto.gender", target = "applicantGender"),
            @Mapping(source = "identityReqDto.birth", target = "applicantBirth"),
            @Mapping(source = "admissionInfo.address", target = "address"),
            @Mapping(source = "admissionInfo.detailAddress", target = "detailAddress"),
            @Mapping(source = "admissionInfo.telephone", target = "telephone"),
            @Mapping(source = "identityReqDto.phoneNumber", target = "applicantPhoneNumber"),
            @Mapping(source = "admissionInfo.guardianName", target = "guardianName"),
            @Mapping(source = "admissionInfo.relationWithApplicant", target = "relationWithApplicant"),
            @Mapping(source = "admissionInfo.guardianPhoneNumber", target = "guardianPhoneNumber"),
            @Mapping(source = "admissionInfo.screening", target = "screening"),
            @Mapping(source = "admissionInfo.schoolName", target = "schoolName"),
            @Mapping(source = "admissionInfo.schoolLocation", target = "schoolLocation"),
            @Mapping(source = "admissionInfo.teacherName", target = "teacherName"),
            @Mapping(source = "admissionInfo.teacherPhoneNumber", target = "teacherPhoneNumber"),
            @Mapping(source = "admissionInfo.desiredMajor.firstDesiredMajor", target = "desiredMajor.firstDesiredMajor"),
            @Mapping(source = "admissionInfo.desiredMajor.secondDesiredMajor", target = "desiredMajor.secondDesiredMajor"),
            @Mapping(source = "admissionInfo.desiredMajor.thirdDesiredMajor", target = "desiredMajor.thirdDesiredMajor"),
    })
    AdmissionInfo toConsistentAdmissionInfoWithIdentity(AdmissionInfo admissionInfo, IdentityReqDto identityReqDto);
}
