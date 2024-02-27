package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class AssessmentDto {
    private Integer id;

    private String review;

    private String reviewDate;

    private InstitutionDto institution;

    private UserDto volunteer;

    public AssessmentDto() {
    }

    public AssessmentDto(Assessment assessment) {
        setId(assessment.getId());
        setReview(assessment.getReview());
        setReviewDate(DateHandler.toISOString(assessment.getReviewDate()));
        setInstitution(new InstitutionDto(assessment.getInstitution(), false, false));
        setVolunteer(new UserDto(assessment.getVolunteer()));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReview() {
        return this.review;
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

    public InstitutionDto getInstitution() {
        return this.institution;
    }

    public void setInstitution(InstitutionDto institution) {
        this.institution = institution;
    }

    public UserDto getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(UserDto volunteer) {
        this.volunteer = volunteer;
    }

    @Override
    public String toString() {
        return "AssessmentDto{" +
                "id=" + id +
                ", review='" + review + '\'' +
                ", reviewDate='" + reviewDate + '\'' +
                ", institution=" + institution +
                ", volunteer=" + volunteer +
                '}';

    }
}
