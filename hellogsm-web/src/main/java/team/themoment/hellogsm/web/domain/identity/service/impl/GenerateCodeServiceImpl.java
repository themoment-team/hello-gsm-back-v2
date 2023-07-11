package team.themoment.hellogsm.web.domain.identity.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.themoment.hellogsm.web.domain.identity.domain.AuthenticationCode;
import team.themoment.hellogsm.web.domain.identity.dto.request.GenerateCodeReqDto;
import team.themoment.hellogsm.web.domain.identity.repository.CodeRepository;
import team.themoment.hellogsm.web.domain.identity.service.CodeNotificationService;
import team.themoment.hellogsm.web.domain.identity.service.GenerateCodeService;
import team.themoment.hellogsm.web.global.exception.error.ExpectedException;

@Service
@RequiredArgsConstructor
public class GenerateCodeServiceImpl implements GenerateCodeService {

    private final static Random RANDOM = new Random();

    public final static int DIGIT_NUMBER = 6;
    public final static int LIMIT_COUNT_CODE_REQUEST = 5;  // code의 만료 시간은 AuthenticationCode에서 찾을 수 있음
    public final static int MAX = (int) Math.pow(10, DIGIT_NUMBER) - 1;

    private final CodeRepository codeRepository;
    private final CodeNotificationService codeNotificationService;

    @Override
    public String execute(Long userId, GenerateCodeReqDto reqDto) {
        if (isLimitedRequest(userId))
            throw new ExpectedException(String.format(
                    "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요. 특정 시간 내 제한 횟수인 %d회를 초과하였습니다.",
                    LIMIT_COUNT_CODE_REQUEST), HttpStatus.FORBIDDEN);

        final String code = generateUniqueCode();
        codeRepository.save(new AuthenticationCode(code, userId, false, reqDto.phoneNumber(), LocalDateTime.now()));
        sendSMS(reqDto.phoneNumber(), code);
        return code;
    }

    private Boolean isLimitedRequest(Long userId) {
        long count = codeRepository.findByUserId(userId).stream().count();
        return count >= LIMIT_COUNT_CODE_REQUEST;
    }

    private void sendSMS(String phoneNumber, String code) {
        codeNotificationService.execute(phoneNumber, code);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = getRandomCode();
        } while (isDuplicate(code));
        return code;
    }

    private Boolean isDuplicate(String code) {
        return codeRepository.findById(code).isPresent();
    }

    public static String getRandomCode() {
        return String.format("%0" + DIGIT_NUMBER + "d", RANDOM.nextInt(0, MAX + 1));
    }
}
