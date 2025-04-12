package com.esgframework.dto;

import com.esgframework.models.SubmissionStatus;
import javax.validation.constraints.NotNull;

public class ReviewRequest {
    @NotNull
    private SubmissionStatus status;

    private String comments;

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
