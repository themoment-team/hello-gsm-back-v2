package team.themoment.hellogsm.batch.jobs.setRegistrationNumber;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import team.themoment.hellogsm.entity.domain.application.entity.Application;
import team.themoment.hellogsm.entity.domain.application.entity.status.AdmissionStatus;
import team.themoment.hellogsm.entity.domain.application.enums.Screening;

@Configuration
@RequiredArgsConstructor
public class SetRegistrationNumberJobConfig {
    public final static int CHUNK_SIZE = 100;
    public final static String JOB_NAME = "setRegistrationNumberJob";
    public final static String BEAN_PREFIX = JOB_NAME + "_";
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final RegistrationNumberSequence registrationNumberSequence;
    private final RegistrationNumberParameter parameter;

    @Bean(BEAN_PREFIX + "registrationNumberSequence")
    @JobScope
    public RegistrationNumberSequence registrationNumberSequence() {
        return new RegistrationNumberSequence();
    }

    @Bean(BEAN_PREFIX + "parameter")
    @JobScope
    public RegistrationNumberParameter parameter(
            @Value("#{jobParameters[DATE_TIME]}") String strDateTime
    ) {
        return new RegistrationNumberParameter(strDateTime);
    }

    @Bean(JOB_NAME)
    public Job setRegistrationNumberJobConfig() {
        return new JobBuilder(JOB_NAME, this.jobRepository)
                .preventRestart()  // 재시작 false
                .start(setRegistrationNumberStep())  // 접수번호 할당 Job 시작
                    .on("FAILED")  // 만약 접수번호 할당 Job을 실패하면
                    .to(clearRegistrationNumberStep()) // 접수번호 초기화 Job 수행
                    .on("*") // 접수번호 초기화 Job의 모든 경우에
                    .end() // Flow 종료
                .from(setRegistrationNumberStep())  // 접수번호 할당 Job으로부터
                    .on("*") // FAILED를 제외한 모든 경우에
                    .end() // Flow 종료
                .end() // Job 작업 종료
                .build();
    }

    @Bean
    @JobScope
    public Step clearRegistrationNumberStep() {
        return new StepBuilder(BEAN_PREFIX + "initStep", this.jobRepository)
                .<Application, AdmissionStatus>chunk(CHUNK_SIZE, this.platformTransactionManager)
                .reader(finalSubmittedApplicationIR())
                .processor(clearRegistrationNumberIP())
                .writer(admissionStatusIW())
                .transactionManager(this.platformTransactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step setRegistrationNumberStep() {
        return new StepBuilder(BEAN_PREFIX + "setRegistrationNumberStep", this.jobRepository)
                .<Application, AdmissionStatus>chunk(CHUNK_SIZE, this.platformTransactionManager)
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        // TODO 로깅 + 웹훅 같은 서비스 사용해서 결과 notice 가능하도록 구현
                        registrationNumberSequence.init();
                    }
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        // TODO 로깅 + 웹훅 같은 서비스 사용해서 결과 notice 가능하도록 구현
                        registrationNumberSequence.clear();
                    }
                })
                .exceptionHandler((context, throwable) -> {
                    //TODO 예외처리 + 로깅 추가
                    //아마 throws 안하면 배치 작업 실패 안됨
                    throw throwable;
                })
                .reader(finalSubmittedApplicationIR())
                .processor(setRegistrationNumberIP())
                .writer(admissionStatusIW())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Application> finalSubmittedApplicationIR() {
        return new JpaPagingItemReaderBuilder<Application>()
                .name(BEAN_PREFIX + "setRegistrationNumberItemReader")
                .entityManagerFactory(this.entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString( // TODO QueryDSL 도입 + 리팩토링하기
                        "SELECT a " +
                                "FROM Application a " +
                                "WHERE a.admissionStatus.isPrintsArrived = true " +
                                "AND a.admissionStatus.isFinalSubmitted = true"
                )
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Application, AdmissionStatus> clearRegistrationNumberIP() {
        return application -> {
            AdmissionStatus admissionStatus = application.getAdmissionStatus();
            AdmissionStatus clearAdmissionStatus = AdmissionStatus.builder()
                    .id(admissionStatus.getId())
                    .isFinalSubmitted(admissionStatus.isFinalSubmitted())
                    .isPrintsArrived(admissionStatus.isPrintsArrived())
                    .firstEvaluation(admissionStatus.getFirstEvaluation())
                    .secondEvaluation(admissionStatus.getSecondEvaluation())
                    .registrationNumber(null)
                    .secondScore(admissionStatus.getSecondScore())
                    .finalMajor(admissionStatus.getFinalMajor())
                    .build();
            return clearAdmissionStatus;
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<Application, AdmissionStatus> setRegistrationNumberIP() {
        return application -> {
            AdmissionStatus admissionStatus = application.getAdmissionStatus();
            Screening screening = application.getAdmissionInfo().getScreening();
            registrationNumberSequence.add(screening);
            Long registrationNumber = getRegistrationNumber(screening);
            AdmissionStatus newAdmissionStatus = AdmissionStatus.builder()
                    .id(admissionStatus.getId())
                    .isFinalSubmitted(admissionStatus.isFinalSubmitted())
                    .isPrintsArrived(admissionStatus.isPrintsArrived())
                    .firstEvaluation(admissionStatus.getFirstEvaluation())
                    .secondEvaluation(admissionStatus.getSecondEvaluation())
                    .registrationNumber(registrationNumber)
                    .secondScore(admissionStatus.getSecondScore())
                    .finalMajor(admissionStatus.getFinalMajor())
                    .build();
            return newAdmissionStatus;
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<AdmissionStatus> admissionStatusIW() {
        return new JpaItemWriterBuilder<AdmissionStatus>()
                .usePersist(false)
                .entityManagerFactory(this.entityManagerFactory)
                .build();
    }

    private Long getRegistrationNumber(Screening screening) {
        Long prefix = switch (screening) {
            case GENERAL:
                yield 1000L;
            case SOCIAL:
                yield 2000L;
            case SPECIAL_VETERANS, SPECIAL_ADMISSION:
                yield 3000L;
            default:
                throw new IllegalArgumentException("Invalid screening: " + screening);
        };
        return registrationNumberSequence.get(screening) + prefix;
    }
}
