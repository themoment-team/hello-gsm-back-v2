package team.themoment.hellogsm.web.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.themoment.hellogsm.entity.domain.application.entity.Application;
import team.themoment.hellogsm.web.domain.application.repository.ApplicationRepository;
import team.themoment.hellogsm.web.domain.application.service.DeleteApplicationService;
import team.themoment.hellogsm.web.global.exception.error.ExpectedException;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class DeleteApplicationServiceImpl implements DeleteApplicationService {
    private final ApplicationRepository applicationRepository;

    @Override
    public void execute(Long userId) {
        Application application = applicationRepository.findByUserId(userId)
                .orElseThrow(() -> new ExpectedException("원서가 존재하지 않습니다", HttpStatus.BAD_REQUEST));
        if (application.getAdmissionStatus().isFinalSubmitted())
            throw new ExpectedException("최종제출이 완료된 원서입니다", HttpStatus.BAD_REQUEST);
        applicationRepository.deleteApplicationByUserId(userId);
    }
}
