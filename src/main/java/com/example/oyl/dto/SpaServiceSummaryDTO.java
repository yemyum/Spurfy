package com.example.oyl.dto;

import lombok.*;

@Getter
@Setter
@Builder // 여러 필드 매핑할 땐 빌더가 가독성 good!
@NoArgsConstructor
@AllArgsConstructor
public class SpaServiceSummaryDTO {

    private String serviceId;
    private String name;
    private Long price;
    private int durationMinutes;

}
