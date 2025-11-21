package com.hashjosh.constant.ai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AIResultDTO {
    private int id;
    private String result;
    private String applicationId;
    private String userId;
    private String prediction;
    private String accuracy;
    private String confidence;
    private Double severity; // Changed from String to Double to match API response
    private String lesion_area;
    private String leaf_area;
    private String image_path;
    private String original_image_url;
    private List<Top3PredictionDTO> top3_predictions;
    private List<LeafAnalysisImageDTO> leaf_analysis_images;
}


