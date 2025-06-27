package com.example.cherrydan.campaign.domain;

import java.util.*;

public enum RegionGroup {
    서울(1, "서울", "seoul", Arrays.asList(
        new SubRegion(1, "교대/사당", "gyodae_sadang"),
        new SubRegion(2, "압구정/신사", "apgujeong_sinsa"),
        new SubRegion(3, "강남/논현", "gangnam_nonhyeon"),
        new SubRegion(4, "삼성/선릉", "samseong_seolleung"),
        new SubRegion(5, "송파/잠실", "songpa_jamsil"),
        new SubRegion(6, "강동/천호", "gangdong_cheongho"),
        new SubRegion(7, "건대/왕십리", "geondae_wangsimni"),
        new SubRegion(8, "홍대/마포", "hongdae_mapo"),
        new SubRegion(9, "강서/목동", "gangseo_mokdong"),
        new SubRegion(10, "노원/강북/도봉", "nowon_gangbuk_dobong"),
        new SubRegion(11, "명동/이태원", "myeongdong_itaewon"),
        new SubRegion(12, "수유/동대문/중랑", "suyu_dongdaemun_jungnang"),
        new SubRegion(13, "신촌/이대/은평", "sinchon_ewha_eunpyeong"),
        new SubRegion(14, "여의도/영등포", "yeouido_yeongdeungpo"),
        new SubRegion(15, "종로/대학로/성북", "jongno_daehakro_seongbuk"),
        new SubRegion(16, "관악/신림", "gwanak_sillim"),
        new SubRegion(17, "구로/금천", "guro_geumcheon"),
        new SubRegion(18, "기타", "etc")
    )),
    경기_인천(2, "경기/인천", "gyeonggi_incheon", Arrays.asList(
        new SubRegion(19, "남양주/구리/하남", "namyangju_guri_hanam"),
        new SubRegion(20, "일산/파주", "ilsan_paju"),
        new SubRegion(21, "안양/안산/광명", "anam_ansan_gwangmyeong"),
        new SubRegion(22, "용인/성남/수원", "yongin_sangnam_suwon"),
        new SubRegion(23, "화성", "hwaseong"),
        new SubRegion(24, "인천/부천", "incheon_buchon"),
        new SubRegion(25, "기타", "etc")
    )),
    강원(3, "강원", "gangwon", Arrays.asList(
        new SubRegion(26, "속초/양양/강릉", "sokcho_yangyang_gangryong"),
        new SubRegion(27, "춘천/홍천/원주", "chuncheon_hongcheon_wonju"),
        new SubRegion(28, "기타", "etc")
    )),
    대전_충청(4, "대전/충청", "daejeon_chungcheong", Arrays.asList(
        new SubRegion(29, "대전/세종", "daejeon_sejong"),
        new SubRegion(30, "충북", "chungbuk"),
        new SubRegion(31, "충남", "chungnam")
    )),
    대구_경북(5, "대구/경북", "daegu_gyeongbuk", Arrays.asList(
        new SubRegion(32, "대구", "daegu"),
        new SubRegion(33, "경북", "gyeongbuk")
    )),
    부산_경남(6, "부산/경남", "busan_gyeongnam", Arrays.asList(
        new SubRegion(34, "부산", "busan"),
        new SubRegion(35, "울산", "ulsan"),
        new SubRegion(36, "경남", "gyeongnam")
    )),
    광주_전라(7, "광주/전라", "gwangju_jeolla", Arrays.asList(
        new SubRegion(37, "광주", "gwangju"),
        new SubRegion(38, "전북", "jeonbuk"),
        new SubRegion(39, "전남", "jeonnam")
    )),
    제주(8, "제주", "jeju", Arrays.asList(
        new SubRegion(40, "제주", "jeju")
    ));

    private final int code;
    private final String label;
    private final String codeName;
    private final List<SubRegion> subRegions;

    RegionGroup(int code, String label, String codeName, List<SubRegion> subRegions) {
        this.code = code;
        this.label = label;
        this.codeName = codeName;
        this.subRegions = subRegions;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }
    public String getCodeName() { return codeName; }
    public List<SubRegion> getSubRegions() { return subRegions; }

    public static RegionGroup fromCode(int code) {
        for (RegionGroup r : values()) {
            if (r.code == code) return r;
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

    public static RegionGroup fromLabel(String label) {
        for (RegionGroup r : values()) {
            if (r.label.equals(label)) return r;
        }
        throw new IllegalArgumentException("Unknown label: " + label);
    }

    /**
     * 하위 지역 codeName으로 RegionGroup, SubRegion 코드 찾기
     * ex) "gyodae_sadang" → (RegionGroup.서울, SubRegion(1, "교대/사당", "gyodae_sadang"))
     */
    public static Optional<RegionGroupSubRegionMatch> findBySubRegionCodeName(String codeName) {
        String trimmedCodeName = codeName.trim();
        for (RegionGroup r : values()) {
            for (SubRegion sub : r.subRegions) {
                if (sub.getCodeName().equalsIgnoreCase(trimmedCodeName)) {
                    return Optional.of(new RegionGroupSubRegionMatch(r, sub));
                }
            }
        }
        return Optional.empty();
    }

    public static RegionGroup fromCodeName(String codeName) {
        String trimmedCodeName = codeName.trim();
        for (RegionGroup r : values()) {
            if (r.codeName.equalsIgnoreCase(trimmedCodeName)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown codeName: " + codeName);
    }

    // 하위 지역 클래스
    public static class SubRegion {
        private final int code;
        private final String label;
        private final String codeName;
        public SubRegion(int code, String label, String codeName) {
            this.code = code;
            this.label = label;
            this.codeName = codeName;
        }
        public int getCode() { return code; }
        public String getLabel() { return label; }
        public String getCodeName() { return codeName; }
    }

    // 하위 지역 매칭용 클래스
    public static class RegionGroupSubRegionMatch {
        private final RegionGroup regionGroup;
        private final SubRegion subRegion;

        public RegionGroupSubRegionMatch(RegionGroup regionGroup, SubRegion subRegion) {
            this.regionGroup = regionGroup;
            this.subRegion = subRegion;
        }

        public RegionGroup getRegionGroup() {
            return regionGroup;
        }

        public SubRegion getSubRegion() {
            return subRegion;
        }
    }
} 