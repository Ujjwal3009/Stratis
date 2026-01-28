package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult {
    private boolean success;
    private int questionCount;
    private String message;
    private List<QuestionDTO> questions;
}
