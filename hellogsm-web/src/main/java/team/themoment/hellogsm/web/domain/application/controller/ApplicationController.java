package team.themoment.hellogsm.web.domain.application.controller;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import team.themoment.hellogsm.web.domain.application.dto.request.ApplicationReqDto;
import team.themoment.hellogsm.web.domain.application.dto.response.ApplicationListDto;
import team.themoment.hellogsm.web.domain.application.dto.request.ApplicationStatusReqDto;
import team.themoment.hellogsm.web.domain.application.dto.response.SingleApplicationRes;
import team.themoment.hellogsm.web.domain.application.service.ApplicationListQuery;
import team.themoment.hellogsm.web.domain.application.service.CreateApplicationService;
import team.themoment.hellogsm.web.domain.application.service.DeleteApplicationService;
import team.themoment.hellogsm.web.domain.application.service.ModifyApplicationService;
import team.themoment.hellogsm.web.domain.application.service.ModifyApplicationStatusService;
import team.themoment.hellogsm.web.domain.application.service.QuerySingleApplicationService;
import team.themoment.hellogsm.web.domain.application.dto.response.TicketResDto;
import team.themoment.hellogsm.web.domain.application.service.*;
import team.themoment.hellogsm.web.global.exception.error.ExpectedException;
import team.themoment.hellogsm.web.global.security.auth.AuthenticatedUserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 원서를 관리하는 controller 입니다
 *
 * @author 변찬우
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/application/v1")
public class ApplicationController {
    private final AuthenticatedUserManager manager;
    private final CreateApplicationService createApplicationService;
    private final ModifyApplicationService modifyApplicationService;
    private final QuerySingleApplicationService querySingleApplicationService;
    private final ApplicationListQuery applicationListQuery;
    private final ModifyApplicationStatusService modifyApplicationStatusService;
    private final DeleteApplicationService deleteApplicationService;
    private final QueryTicketsService queryTicketsService;
    private final ImageSaveService imageSaveService;
    private final FinalSubmissionService finalSubmissionService;

    @GetMapping("/application/{userId}")
    public SingleApplicationRes readOne(@PathVariable("userId") Long userId) {
        return querySingleApplicationService.execute(userId);
    }

    @GetMapping("/application/me")
    public SingleApplicationRes readMe() {
        return querySingleApplicationService.execute(manager.getId());
    }

    @PostMapping("/application/me")
    public ResponseEntity<Map<String, String>> create(
            @RequestBody @Valid ApplicationReqDto body
    ) {
        createApplicationService.execute(body, manager.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "생성되었습니다"));
    }

    @PutMapping("/application/me")
    public ResponseEntity<Map<String, String>> modify(
            @RequestBody @Valid ApplicationReqDto body
    ) {
        modifyApplicationService.execute(body, manager.getId());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "수정되었습니다"));
    }

    @GetMapping("/application/all")
    public ApplicationListDto findAll(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size
    ) {
        if (page < 0 || size < 0)
            throw new ExpectedException("0 이상만 가능합니다", HttpStatus.BAD_REQUEST);
        return applicationListQuery.execute(page, size);
    }

    @PutMapping("/status/{userId}")
    public ResponseEntity<Map<String, String>> modifyStatus(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody ApplicationStatusReqDto applicationStatusReqDto
    ) {
        modifyApplicationStatusService.execute(userId, applicationStatusReqDto);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "수정되었습니다"));
    }

    @DeleteMapping("/application/me")
    public ResponseEntity<Map<String, String>> delete() {
        deleteApplicationService.execute(manager.getId());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "삭제되었습니다"));
    }

    @GetMapping("/tickets")
    public List<TicketResDto> tickets(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size
    ) {
        if (page < 0 || size < 0)
            throw new ExpectedException("0 이상만 가능합니다", HttpStatus.BAD_REQUEST);
        return queryTicketsService.execute(page, size);
    }

    @PostMapping("/image")
    public Map<String, String> uploadImage(@Valid @RequestPart(name = "file") MultipartFile multipartFile) {
        String url = imageSaveService.execute(multipartFile);
        return Map.of("url", url);
    }

    @PutMapping("/final-submit")
    public ResponseEntity<Map<String, String>> finalSubmission() {
        finalSubmissionService.execute(manager.getId());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "수정되었습니다"));
    }
}
