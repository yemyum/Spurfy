package com.example.oyl.dto;

import com.example.oyl.domain.Tag;

public class TagResponseDTO {
    private Long tagId;
    private String tagName;

    public TagResponseDTO(Tag tag) {
        this.tagId = tag.getTagId();
        this.tagName = tag.getTagName();
    }
}
