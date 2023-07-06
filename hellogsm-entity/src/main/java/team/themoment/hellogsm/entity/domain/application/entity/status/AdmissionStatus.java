package team.themoment.hellogsm.entity.domain.application.entity.status;


import jakarta.persistence.*;
import lombok.*;
import team.themoment.hellogsm.entity.domain.application.enums.EvaluationStatus;
import team.themoment.hellogsm.entity.domain.application.enums.Major;
import team.themoment.hellogsm.entity.domain.application.enums.Screening;

import java.math.BigDecimal;

/**
 * 입학 원서의 상태를 저장하는 Entity입니다.
 *
 * @author 양시준
 * @since 1.0.0
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@Getter
public class AdmissionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_status_id")
    private Long id;

    @Column(name = "is_final_submitted", nullable = false)
    private Boolean isFinalSubmitted;

    @Column(name = "is_prints_arrived", nullable = false)
    private Boolean isPrintsArrived;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_evaluation", nullable = false)
    private EvaluationStatus firstEvaluation;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_evaluation", nullable = false)
    private EvaluationStatus secondEvaluation;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_submitted_at", nullable = true)
    private Screening screeningSubmittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_first_evaluation_at", nullable = true)
    private Screening screeningFirstEvaluationAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "screening_second_evaluation_at", nullable = true)
    private Screening screeningSecondEvaluationAt;

    @Column(name = "registration_number", nullable = true)
    private Long registrationNumber;  // 접수 번호, 원서 제출 기간 후 배정됨

    @Column(name = "second_score", nullable = true) // TODO 이름 바꾸기 - 2차 시험 점수
    private BigDecimal secondScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_major", nullable = true)
    private Major finalMajor; // "배정되지 않음" 상태를 표현하기 애패해서 일단은 null로 둠

    /**
     * 초기 상태의 {@code AdmissionStatus}를 생성합니다.
     *
     * @return 초기 상태의 {@code AdmissionStatus} 객체
     */
    public static AdmissionStatus init(Long id) {
        return AdmissionStatus.builder()
                .id(id)
                .isFinalSubmitted(false)
                .isPrintsArrived(false)
                .firstEvaluation(EvaluationStatus.NOT_YET)
                .secondEvaluation(EvaluationStatus.NOT_YET)
                .screeningSubmittedAt(null)
                .screeningFirstEvaluationAt(null)
                .screeningSecondEvaluationAt(null)
                .finalMajor(null)
                .build();
    }

    public Boolean isFinalSubmitted() {
        return isFinalSubmitted;
    }

    public Boolean isPrintsArrived() {
        return isPrintsArrived;
    }

    // null이 아닌 기본 값이 있는 경우 기본값 정해주기
    @PrePersist
    public void prePersist() {
        isFinalSubmitted = isFinalSubmitted == null ? false : isFinalSubmitted;
        isPrintsArrived = isPrintsArrived == null ? false : isPrintsArrived;
        firstEvaluation = firstEvaluation == null ? EvaluationStatus.NOT_YET : firstEvaluation;
        secondEvaluation = secondEvaluation == null ? EvaluationStatus.NOT_YET : secondEvaluation;
    }
}
