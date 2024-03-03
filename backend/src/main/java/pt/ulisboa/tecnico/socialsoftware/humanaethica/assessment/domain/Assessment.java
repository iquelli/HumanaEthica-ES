package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "assessment")
public class Assessment {

    private static final int REVIEW_MIN_LEN = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String review;

    private LocalDateTime reviewDate;

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private Volunteer volunteer;

    public Assessment() {
    }

    public Assessment(AssessmentDto assessmentDto, Institution institution, Volunteer volunteer) {
        setReview(assessmentDto.getReview());
        setReviewDate(DateHandler.now());
        setInstitution(institution);
        setVolunteer(volunteer);

        verifyInvariants();
    }

    public Integer getId() {
        return id;
    }

    public String getReview() {
        return this.review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getReviewDate() {
        return this.reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public Institution getInstitution() {
        return this.institution;
    }

    public void setInstitution(Institution institution) {
        institution.addAssessment(this);
        this.institution = institution;
    }

    public Volunteer getVolunteer() {
        return this.volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        volunteer.addAssessment(this);
        this.volunteer = volunteer;
    }

    private void verifyInvariants() {
        reviewIsRequired();
        reviewLengthAboveMinimumLength();
        institutionHasCompletedActivity();
        volunteerHasNotAssessedInstituition();
    }

    private void reviewIsRequired() {
        if (this.review == null || this.review.trim().isEmpty()) {
            throw new HEException(ASSESSMENT_REVIEW_TOO_SHORT, REVIEW_MIN_LEN);
        }
    }

    private void reviewLengthAboveMinimumLength() {
        if (this.review.length() < REVIEW_MIN_LEN) {
            throw new HEException(ASSESSMENT_REVIEW_TOO_SHORT, REVIEW_MIN_LEN);
        }
    }

    private void institutionHasCompletedActivity() {
        if (this.institution == null || !this.institution.getActivities()
                .stream()
                .anyMatch(a -> a.getEndingDate().isBefore(this.reviewDate))) {
            throw new HEException(ASSESSMENT_INSTITUTION_WITHOUT_COMPLETED_ACTIVITY, this.institution.getName());
        }
    }

    private void volunteerHasNotAssessedInstituition() {
        Boolean hasAssessedInstitution = false;
        if(this.institution != null && this.institution.getAssessments() != null) {
            hasAssessedInstitution = this.institution.getAssessments()
                                            .stream()
                                            .anyMatch(a -> a.getVolunteer().getName()
                                            .equals(this.volunteer.getName()));
        }

        if (this.volunteer == null || hasAssessedInstitution) {
            throw new HEException(ASSESSMENT_VOLUNTEER_HAS_ASSESSED_INSTITUTION, this.volunteer.getName(),
                    this.institution.getName());
        }

    }

}
