package kr.hellogsm.back_v2.domain.user.service;

import kr.hellogsm.back_v2.domain.user.dto.response.UserResDto;

public interface UserByProviderQuery {
    UserResDto execute(String provider, String providerId);
}