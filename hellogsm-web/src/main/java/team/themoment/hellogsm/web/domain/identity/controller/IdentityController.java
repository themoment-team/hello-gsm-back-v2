package team.themoment.hellogsm.web.domain.identity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import team.themoment.hellogsm.web.domain.identity.dto.domain.IdentityDto;
import team.themoment.hellogsm.web.domain.identity.dto.request.IdentityReqDto;
import team.themoment.hellogsm.web.domain.identity.service.CreateIdentityService;
import team.themoment.hellogsm.web.domain.identity.service.IdentityQuery;
import team.themoment.hellogsm.web.domain.identity.service.ModifyIdentityService;
import team.themoment.hellogsm.web.global.security.auth.AuthenticatedUserManager;

import java.net.URI;

@RestController
@RequestMapping("/identity/v1")
@RequiredArgsConstructor
public class IdentityController {
    private final AuthenticatedUserManager manager;
    private final CreateIdentityService createIdentityService;
    private final ModifyIdentityService modifyIdentityService;
    private final IdentityQuery identityQuery;

    // 사용은 안 하지만 혹시 몰라서 남김
//    @PostMapping("/identity/{userId}")
//    public ResponseEntity<IdentityDto> createByUserId(
//            @RequestBody @Valid IdentityReqDto reqDto,
//            @PathVariable Long userId
//    ) {
//        IdentityDto identityResDto = createIdentityService.execute(reqDto, userId);
//        return ResponseEntity.status(HttpStatus.CREATED).body(identityResDto);
//    }

    @PostMapping("/identity/me")
    public ResponseEntity<Object> create(
            @RequestBody @Valid IdentityReqDto reqDto
    ) {
        createIdentityService.execute(reqDto, manager.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/auth/v1/logout"));
        // 인증정보 갱신을 위한 로그아웃 uri로 리다이렉트
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    @GetMapping("/identity/me")
    public ResponseEntity<IdentityDto> find() {
        IdentityDto identityResDto = identityQuery.execute(manager.getId());
        return ResponseEntity.status(HttpStatus.OK).body(identityResDto);
    }

    @PutMapping("/identity/me")
    public ResponseEntity<IdentityDto> modify(
            @RequestBody @Valid IdentityReqDto reqDto
    ) {
        modifyIdentityService.execute(reqDto, manager.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/auth/v1/logout"));
        // 굳이 리다이렉트 할 필요는 없는데, create() 랑 리턴 타입을 맞추기 위해 리다이렉트
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    @GetMapping("/identity/{userId}")
    public ResponseEntity<IdentityDto> findByUserId(
            @PathVariable Long userId
    ) {
        IdentityDto identityResDto = identityQuery.execute(userId);
        return ResponseEntity.status(HttpStatus.OK).body(identityResDto);
    }
}
