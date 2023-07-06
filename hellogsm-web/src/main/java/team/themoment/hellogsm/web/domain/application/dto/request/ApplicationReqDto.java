package team.themoment.hellogsm.web.domain.application.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import team.themoment.hellogsm.entity.domain.application.entity.Application;
import team.themoment.hellogsm.entity.domain.application.entity.admission.AdmissionInfo;
import team.themoment.hellogsm.entity.domain.application.entity.admission.DesiredMajor;
import team.themoment.hellogsm.entity.domain.application.entity.status.AdmissionStatus;
import team.themoment.hellogsm.entity.domain.application.entity.grade.MiddleSchoolGrade;
import team.themoment.hellogsm.entity.domain.application.enums.Gender;
import team.themoment.hellogsm.entity.domain.application.enums.GraduationStatus;
import team.themoment.hellogsm.entity.domain.application.enums.Major;
import team.themoment.hellogsm.entity.domain.application.enums.Screening;
import team.themoment.hellogsm.web.domain.application.annotation.NotSpace;
import team.themoment.hellogsm.web.global.exception.error.ExpectedException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 원서 생성을 담당하는 dto 입니다
 *
 * @author 변찬우
 * @since 1.0.0
 */
public record ApplicationReqDto(
        @NotBlank
        String applicantImageUri,

        @NotBlank
        String address,

        @NotBlank
        String detailAddress,

        @NotBlank
        @Pattern(regexp = "^(CANDIDATE|GRADUATE|GED)$")
        String graduation,

        @NotBlank
        @Pattern(regexp = "^0(?:\\d|\\d{2})(?:\\d{3}|\\d{4})\\d{4}$")
        String telephone,

        @NotBlank
        String guardianName,

        @NotBlank
        String relationWithApplicant,

        @NotBlank
        @Pattern(regexp = "^0(?:\\d|\\d{2})(?:\\d{3}|\\d{4})\\d{4}$")
        String guardianPhoneNumber,

        @Pattern(regexp = "^(?!\\s*$).+")
        String teacherName,

        @Pattern(regexp = "^0(?:\\d|\\d{2})(?:\\d{3}|\\d{4})\\d{4}$")
        String teacherPhoneNumber,

        @Pattern(regexp = "^(AI|SW|IOT)$")
        @NotBlank
        String firstDesiredMajor,

        @Pattern(regexp = "^(AI|SW|IOT)$")
        @NotBlank
        String secondDesiredMajor,

        @Pattern(regexp = "^(AI|SW|IOT)$")
        @NotBlank
        String thirdDesiredMajor,

        @NotBlank
        String middleSchoolGrade,

        @NotSpace
        String schoolName,

        @NotSpace
        String schoolLocation,

        @Pattern(regexp = "^(GENERAL|SOCIAL|SPECIAL_VETERANS|SPECIAL_ADMISSION)$")
        @NotBlank
        String screening
) {

    @AssertTrue(message = "중복된 전공이 있습니다")
    private boolean isDuplicateMajor() {
        Set<String> majorSet = new HashSet<>(List.of(firstDesiredMajor, secondDesiredMajor, thirdDesiredMajor));

        return majorSet.size() == 3;
    }
}
