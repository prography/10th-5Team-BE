package com.example.cherrydan.campaign.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지역 enum
 */
@Getter
@RequiredArgsConstructor
public enum Region {
    // 전체
    ALL("전체", "전체 지역"),

    // 서울특별시
    SEOUL("서울", "서울특별시"),
    SEOUL_GANGNAM_NONHYEON("강남/논현", "서울 강남구/논현동"),
    SEOUL_GANGDONG_CHEONHO("강동/천호", "서울 강동구/천호동"),
    SEOUL_GANGSEO_MOKDONG("강서/목동", "서울 강서구/목동"),
    SEOUL_GEONNDAE_WANGSIMNI("건대/왕십리", "서울 광진구/성동구"),
    SEOUL_GWANAK_SILLIM("관악/신림", "서울 관악구/신림동"),
    SEOUL_GURO_SADANG("교대/사당", "서울 서초구/동작구"),
    SEOUL_NOWON_GANGBUK("노원/강북", "서울 노원구/강북구"),
    SEOUL_MYEONGDONG_ITAEWON("명동/이태원", "서울 중구/용산구"),
    SEOUL_SAMSUNG_SEONNEUNG("삼성/선릉", "서울 강남구 삼성동/선릉"),
    SEOUL_SONGPA_JAMSIL("송파/잠실", "서울 송파구/잠실동"),
    SEOUL_SINCHON_IDAE("신촌/이대", "서울 서대문구/마포구"),
    SEOUL_APGUJEONG_SINSA("압구정/신사", "서울 강남구/신사동"),
    SEOUL_YEOUIDO_YEONGDEUNGPO("여의도/영등포", "서울 영등포구"),
    SEOUL_JONGNO_DAEHAKRO("종로/대학로", "서울 종로구"),
    SEOUL_HONGDAE_MAPO("홍대/마포", "서울 마포구"),

    // 부산광역시
    BUSAN("부산", "부산광역시"),
    BUSAN_HAEUNDAE_CENTUM("해운대/센텀", "부산 해운대구"),
    BUSAN_SEOMYEON_BUSANJIN("서면/부산진", "부산 부산진구"),
    BUSAN_NAMPO_JUNG("남포/중구", "부산 중구"),
    BUSAN_SASANG_KIMHAE("사상/김해", "부산 사상구/김해시"),
    BUSAN_GANGSEO_NOKSAN("강서/녹산", "부산 강서구"),

    // 대구광역시
    DAEGU("대구", "대구광역시"),
    DAEGU_DONGSEONG_JUNG("동성로/중구", "대구 중구"),
    DAEGU_SUSEONG_BEOMEO("수성/범어", "대구 수성구"),
    DAEGU_DALSEO_SEONGSO("달서/성서", "대구 달서구"),
    DAEGU_BUKGU_CHILGOK("북구/칠곡", "대구 북구/칠곡군"),

    // 인천광역시
    INCHEON("인천", "인천광역시"),
    INCHEON_BUPYEONG_GALSAN("부평/갈산", "인천 부평구"),
    INCHEON_YEONSU_SONGDO("연수/송도", "인천 연수구"),
    INCHEON_NAMDONG_GUWOL("남동/구월", "인천 남동구"),
    INCHEON_JUNG_CHINATOWN("중구/차이나타운", "인천 중구"),

    // 광주광역시
    GWANGJU("광주", "광주광역시"),
    GWANGJU_DONGGU_GEUMNAM("동구/금남로", "광주 동구"),
    GWANGJU_SEOGU_CHUNGJANG("서구/충장로", "광주 서구"),
    GWANGJU_BUK_ILGOK("북구/일곡", "광주 북구"),
    GWANGJU_GWANGSAN_SUWAN("광산/수완", "광주 광산구"),

    // 대전광역시
    DAEJEON("대전", "대전광역시"),
    DAEJEON_JUNG_EUNHAENG("중구/은행동", "대전 중구"),
    DAEJEON_SEOGU_DUNSAN("서구/둔산", "대전 서구"),
    DAEJEON_YUSEONG_EXPO("유성/엑스포", "대전 유성구"),
    DAEJEON_DAEDEOK_SINTANJIN("대덕/신탄진", "대전 대덕구"),

    // 울산광역시
    ULSAN("울산", "울산광역시"),
    ULSAN_NAMGU_SAMSAN("남구/삼산", "울산 남구"),
    ULSAN_DONGGU_ILSAN("동구/일산", "울산 동구"),
    ULSAN_BUKGU_NONGSO("북구/농소", "울산 북구"),
    ULSAN_JUNG_SEONGNAM("중구/성남", "울san 중구"),

    // 세종특별자치시
    SEJONG("세종", "세종특별자치시"),
    SEJONG_JOCHIWON("조치원", "세종 조치원읍"),
    SEJONG_HANSOL("한솔", "세종 한솔동"),
    SEJONG_DASOM("다솜", "세종 다솜동"),

    // 경기도
    GYEONGGI("경기", "경기도"),
    GYEONGGI_SUWON_YEONGTONG("수원/영통", "경기 수원시"),
    GYEONGGI_SEONGNAM_BUNDANG("성남/분당", "경기 성남시"),
    GYEONGGI_ANYANG_PYEONGCHON("안양/평촌", "경기 안양시"),
    GYEONGGI_ANSAN_SIHEUNG("안산/시흥", "경기 안산시/시흥시"),
    GYEONGGI_GOYANG_ILSAN("고양/일산", "경기 고양시"),
    GYEONGGI_YONGIN_GIHEUNG("용인/기흥", "경기 용인시"),
    GYEONGGI_BUCHEON_WONMI("부천/원미", "경기 부천시"),
    GYEONGGI_HWASEONG_DONGTAN("화성/동탄", "경기 화성시"),
    GYEONGGI_NAMYANGJU_GURI("남양주/구리", "경기 남양주시/구리시"),
    GYEONGGI_PAJU_GIMPO("파주/김포", "경기 파주시/김포시"),

    // 강원특별자치도
    GANGWON("강원", "강원특별자치도"),
    GANGWON_CHUNCHEON_MYEONGDONG("춘천/명동", "강원 춘천시"),
    GANGWON_GANGNEUNG_GYEONGPO("강릉/경포", "강원 강릉시"),
    GANGWON_WONJU_HYEHYEON("원주/혜현", "강원 원주시"),
    GANGWON_DONGHAE_MUKHO("동해/묵호", "강원 동해시"),
    GANGWON_SOKCHO_JUNGANG("속초/중앙", "강원 속초시"),
    GANGWON_SAMCHEOK_DOWNTOWN("삼척/시내", "강원 삼척시"),

    // 충청북도
    CHUNGBUK("충북", "충청북도"),
    CHUNGBUK_CHEONGJU_SEONGAHN("청주/성안길", "충북 청주시"),
    CHUNGBUK_CHUNGJU_JUNGANG("충주/중앙", "충북 충주시"),
    CHUNGBUK_JECHEON_UIRIM("제천/의림", "충북 제천시"),
    CHUNGBUK_JINCHEON_DOWNTOWN("진천/시내", "충북 진천군"),

    // 충청남도
    CHUNGNAM("충남", "충청남도"),
    CHUNGNAM_CHEONAN_YAWOORI("천안/야우리", "충남 천안시"),
    CHUNGNAM_ASAN_ONYANG("아산/온양", "충남 아산시"),
    CHUNGNAM_SEOSAN_DONGMUN("서산/동문", "충남 서산시"),
    CHUNGNAM_DANGJIN_JUNGANG("당진/중앙", "충남 당진시"),
    CHUNGNAM_GONGJU_UNGJIN("공주/웅진", "충남 공주시"),

    // 전라북도
    JEONBUK("전북", "전라북도"),
    JEONBUK_JEONJU_HANOK("전주/한옥마을", "전북 전주시"),
    JEONBUK_GUNSAN_JUNGANG("군산/중앙", "전북 군산시"),
    JEONBUK_IKSAN_MODAKSAN("익산/모악산", "전북 익산시"),
    JEONBUK_JEONGEUP_DOWNTOWN("정읍/시내", "전북 정읍시"),

    // 전라남도
    JEONNAM("전남", "전라남도"),
    JEONNAM_MOKPO_HADANG("목포/하당", "전남 목포시"),
    JEONNAM_YEOSU_EXPO("여수/엑스포", "전남 여수시"),
    JEONNAM_SUNCHEON_JORYE("순천/조례", "전남 순천시"),
    JEONNAM_NAJU_GEUMSEONG("나주/금성", "전남 나주시"),

    // 경상북도
    GYEONGBUK("경북", "경상북도"),
    GYEONGBUK_POHANG_JUNGANG("포항/중앙", "경북 포항시"),
    GYEONGBUK_GYEONGJU_HWANGNAM("경주/황남", "경북 경주시"),
    GYEONGBUK_GIMCHEON_JUNGANG("김천/중앙", "경북 김천시"),
    GYEONGBUK_ANDONG_JUNGANG("안동/중앙", "경북 안동시"),
    GYEONGBUK_GUMI_GONGDAN("구미/공단", "경북 구미시"),

    // 경상남도
    GYEONGNAM("경남", "경상남도"),
    GYEONGNAM_CHANGWON_SANGNAM("창원/상남", "경남 창원시"),
    GYEONGNAM_JINJU_JUNGANG("진주/중앙", "경남 진주시"),
    GYEONGNAM_TONGYEONG_JUNGANG("통영/중앙", "경남 통영시"),
    GYEONGNAM_SACHEON_JUNGANG("사천/중앙", "경남 사천시"),
    GYEONGNAM_KIMHAE_BUWON("김해/부원", "경남 김해시"),
    GYEONGNAM_MIRYANG_JUNGANG("밀양/중앙", "경남 밀양시"),

    // 제주특별자치도
    JEJU("제주", "제주특별자치도"),
    JEJU_JEJU_SHINJEJU("제주/신제주", "제주 제주시"),
    JEJU_SEOGWIPO_JUNGANG("서귀포/중앙", "제주 서귀포시"),
    JEJU_JUNGMUN_RESORT("중문/리조트", "제주 서귀포시 중문동"),
    JEJU_SEONGSAN_ILCHUL("성산/일출", "제주 서귀포시 성산읍");

    private final String code;
    private final String description;

    /**
     * 코드로 Region 찾기
     */
    public static Region fromCode(String code) {
        for (Region region : values()) {
            if (region.getCode().equals(code)) {
                return region;
            }
        }
        throw new IllegalArgumentException("Invalid region code: " + code);
    }
}