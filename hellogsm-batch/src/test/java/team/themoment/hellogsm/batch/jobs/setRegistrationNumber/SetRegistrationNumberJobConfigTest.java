package team.themoment.hellogsm.batch.jobs.setRegistrationNumber;

import static org.junit.jupiter.api.Assertions.*;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import team.themoment.hellogsm.batch.TestBatchConfig;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@DisplayName("접수번호 할당 배치 Job 테스트")
@SpringBatchTest
@SpringBootTest
class SetRegistrationNumberJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    // 예외 발생 시 예외가 throw 되지 않고, 초기화하는 코드가 실행되니 주의
    // 성공했다고 떠도 예외가 발생해서 롤백되었을 수도 있다는 말임
    //@Test
    @DisplayName("Job 실행을 위한 코드")
    public void testJob(@Qualifier(value = SetRegistrationNumberJobConfig.JOB_NAME) @Autowired Job job) throws Exception {
        // given
        jobLauncherTestUtils.setJob(job);
        jobLauncherTestUtils.setJobRepository(jobLauncherTestUtils.getJobRepository());
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(
                        "DATE_TIME",
                        ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss-z"))
                )
                .toJobParameters();

        // when
        JobExecution jobExecution =
                jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(
                ExitStatus.COMPLETED,
                jobExecution.getExitStatus()
        );
    }

}
