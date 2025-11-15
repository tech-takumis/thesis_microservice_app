package com.hashjosh.constant.ai;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class LeafAnalysisImageDTO {
    private String image_type;
    private String image_path;
    private String presigned_url;
    private int width;
    private int height;
    private long file_size;
    private String created_at;
}