package com.example.cherrydan.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequestDTO {
    private String category;
    private String title;
    private String content;
}