package com.example.cherrydan.user.dto;

import com.example.cherrydan.user.domain.Gender;
import com.example.cherrydan.user.domain.User;
import com.example.cherrydan.user.domain.UserTos;
import lombok.Getter;

@Getter
public class UserDto {
    private String email;
    private String name;
    private String nickname;
    private Integer birthYear;
    private Gender gender;
    
    // UserTos 정보 (배열 형태)
    private Boolean[] tosAgreements;

    public UserDto(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.birthYear = user.getBirthYear();
        this.gender = user.getGender();
        
        // UserTos 정보 설정 (배열 형태)
        UserTos userTos = user.getUserTos();
        if (userTos != null) {
            this.tosAgreements = new Boolean[]{
                userTos.getIsAgreedServiceUsage(),    // 0: 서비스 이용약관
                userTos.getIsAgreedPrivateInfo(),     // 1: 개인정보 처리방침
                userTos.getIsAgreed3rdParty(),        // 2: 제3자 정보제공
                userTos.getIsAgreedLocationInfo(),    // 3: 위치정보 이용약관
                userTos.getIsAgreedAds()              // 4: 마케팅 정보 수신
            };
        } else {
            // UserTos가 없는 경우 기본값 설정
            this.tosAgreements = new Boolean[]{
                true,   // 0: 서비스 이용약관
                true,   // 1: 개인정보 처리방침
                true,   // 2: 제3자 정보제공
                false,  // 3: 위치정보 이용약관
                false   // 4: 마케팅 정보 수신
            };
        }
    }
}
