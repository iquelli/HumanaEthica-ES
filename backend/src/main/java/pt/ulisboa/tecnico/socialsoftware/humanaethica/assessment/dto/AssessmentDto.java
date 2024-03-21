package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;

public class AssessmentDto {

    private Integer id;
    private String review;
    private String reviewDate;
    private String volunteerName;
    private Integer institutionId;

    public AssessmentDto() {
    }

    public AssessmentDto(Assessment assessment) {
        this.id = assessment.getId();
        this.review = assessment.getReview();
        this.reviewDate = DateHandler.toISOString(assessment.getReviewDate());
        this.volunteerName = assessment.getVolunteer().getName();
        this.institutionId = assessment.getInstitution().getId();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getVolunteerName() {
        return this.volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public Integer getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Integer institutionID) {
        this.institutionId = institutionID;
    } 
 
}
