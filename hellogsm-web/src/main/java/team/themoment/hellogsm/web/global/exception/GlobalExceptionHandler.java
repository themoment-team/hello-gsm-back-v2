package team.themoment.hellogsm.web.global.exception;


import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import team.themoment.hellogsm.web.global.exception.error.ExpectedException;
import team.themoment.hellogsm.web.global.exception.model.ExceptionResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Global한 예외를 핸들링 하는 Handler입니다.
 *
 * @author 양시준
 * @since 1.0.0
 */
@Slf4j
@EnableWebMvc
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 에러인 {@code ExpectedException}을 포함한 하위 에러를 핸들링합니다. <br>
     *
     * @param ex {@code ExpectedException}을 포함한 {@code ExpectedException}을 상속하는 객체
     * @return ResponseEntity
     */
    @ExceptionHandler(ExpectedException.class)
    private ResponseEntity<ExceptionResponseEntity> expectedException(ExpectedException ex) {
        log.warn("ExpectedException : {} ", ex.getMessage());
        log.trace("ExpectedException Details : ", ex);
        return ResponseEntity.status(ex.getStatusCode().value())
                .body(ExceptionResponseEntity.of(ex));
    }


    /**
     * {@code @Valid} 검증에 실패한 경우 던져지는 {@code MethodArgumentNotValidException}와 {@code HttpMessageNotReadableException}를 핸들링합니다.
     *
     * <p>
     * 반환 메시지 예시:
     * <pre>
     *  {@code
     * {
     *    "message": "{'testDto':{'password':'널이어서는 안됩니다','data':'최대 길이인 5를 넘어감니다'}}"
     * }
     * }
     * </pre>
     *
     * @param ex {@code MethodArgumentNotValidException}
     * @return ResponseEntity
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ExceptionResponseEntity> validationException(MethodArgumentNotValidException ex) {
        log.warn("Validation Failed : {}", ex.getMessage());
        log.trace("Validation Failed Details : ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new ExceptionResponseEntity(methodArgumentNotValidExceptionToJson(ex)));
    }

    /**
     * 예외처리하지 않은 {@code RuntimeException} 포함한 하위 에러를 핸들링합니다. <br>
     *
     * @param ex {@code RuntimeException}또는 {@code RuntimeException}을 상속하는 객체
     * @return ResponseEntity
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponseEntity> unExpectedException(RuntimeException ex) {
        log.error("UnExpectedException Occur : ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new ExceptionResponseEntity("internal server error has occurred"));
    }

    //  만약 ConstraintViolationException 에러가 발생했다면 JPA에서 valid 문제가 생긴건데,
    //  비즈니스 로직을 잘못 짰거나, Request DTO 에서 검증을 잘못했단 뜻이니까. BE 개발자의 문제이므로 코드를 고칠 필요가 있다.

    /**
     * {@code NoHandlerFoundException}을 처리하는 메소드입니다.
     *
     * @param ex NoHandlerFoundException 객체
     * @return ResponseEntity
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ExceptionResponseEntity> noHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("Not Found Endpoint : {}", ex.getMessage());
        log.trace("Not Found Endpoint Details : ", ex);
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ExceptionResponseEntity(HttpStatus.NOT_FOUND.getReasonPhrase()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionResponseEntity> maxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("The file is too big : {}", ex.getMessage());
        log.trace("The file is too big Details : ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new ExceptionResponseEntity("The file is too big, limited file size : " + ex.getMaxUploadSize()));
    }

    private static String methodArgumentNotValidExceptionToJson(MethodArgumentNotValidException ex) {
        Map<String, Object> globalResults = new HashMap<>();
        Map<String, String> fieldResults = new HashMap<>();

        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            globalResults.put(ex.getBindingResult().getObjectName(), error.getDefaultMessage());
        });
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldResults.put(error.getField(), error.getDefaultMessage());
        });
        globalResults.put(ex.getBindingResult().getObjectName(), fieldResults);

        return new JSONObject(globalResults).toString().replace("\"", "'");
    }

}
