package team.themoment.hellogsm.web.domain.application.dto.domain;

import team.themoment.hellogsm.entity.domain.application.enums.EvaluationStatus;
import team.themoment.hellogsm.entity.domain.application.enums.Major;
import team.themoment.hellogsm.entity.domain.application.enums.Screening;

import java.math.BigDecimal;

public record AdmissionStatusDto(
        Boolean isFinalSubmitted,
        Boolean isPrintsArrived,
        EvaluationStatus firstEvaluation,
        EvaluationStatus secondEvaluation,
        Screening screeningSubmittedAt,
        Screening screeningFirstEvaluationAt,
        Screening screeningSecondEvaluationAt,
        Long registrationNumber,
        BigDecimal secondScore,
        Major finalMajor
) {
}
