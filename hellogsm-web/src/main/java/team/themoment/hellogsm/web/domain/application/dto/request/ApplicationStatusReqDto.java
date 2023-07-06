package team.themoment.hellogsm.web.domain.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ApplicationStatusReqDto(
        @NotNull
        Boolean isFinalSubmitted,

        @NotNull
        Boolean isPrintsArrived,

        @Pattern(regexp = "^(NOT_YET|PASS|FALL)$")
        @NotBlank
        String firstEvaluation,

        @Pattern(regexp = "^(NOT_YET|PASS|FALL)$")
        @NotBlank
        String secondEvaluation,

        @Pattern(regexp = "^(GENERAL|SOCIAL|SPECIAL_VETERANS|SPECIAL_ADMISSION|)$") // null 포함
        String screeningSubmittedAt,

        @Pattern(regexp = "^(GENERAL|SOCIAL|SPECIAL_VETERANS|SPECIAL_ADMISSION|)$") // null 포함
        String screeningFirstEvaluationAt,

        @Pattern(regexp = "^(GENERAL|SOCIAL|SPECIAL_VETERANS|SPECIAL_ADMISSION|)$") // null 포함
        String screeningSecondEvaluationAt,

        Long registrationNumber,

        BigDecimal secondScore,

        @Pattern(regexp = "^(AI|IOT|SW|)$") // null 포함
        String finalMajor
) {
}
