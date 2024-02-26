package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "enrollment")
public class Enrollment {
    
    private static final int MOTIVATION_MINIMUM_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String motivation;

    @ManyToOne
    private Activity activity;

    @ManyToOne
    private Volunteer volunteer;

    private LocalDateTime enrollmentDateTime;

    public Enrollment() {
    }

    public Enrollment(EnrollmentDto enrollmentDto, Activity activity, Volunteer volunteer) {
        setMotivation(enrollmentDto.getMotivation());
        setEnrollmentDateTime(DateHandler.now());
        setActivity(activity);
        setVolunteer(volunteer);

        verifyInvariants();
    }

    public Integer getId() {
        return id;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation.trim();
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addEnrollment(this);
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addEnrollment(this);
    }

    public LocalDateTime getEnrollmentDateTime() {
        return enrollmentDateTime;
    }

    public void setEnrollmentDateTime(LocalDateTime enrollmentDateTime) {
        this.enrollmentDateTime = enrollmentDateTime;
    }

    private void verifyInvariants() {
        motivationLengthAboveMinimum();
        enrollmentBeforeDeadline();
        hasEnrolledForActivity();
    }

    private void motivationLengthAboveMinimum() {
        if (this.motivation == null || this.motivation.length() < MOTIVATION_MINIMUM_LENGTH) {
            throw new HEException(ENROLLMENT_MOTIVATION_TOO_SHORT, MOTIVATION_MINIMUM_LENGTH);
        }
    }

    private void enrollmentBeforeDeadline() {
        LocalDateTime applicationDeadLine = activity.getApplicationDeadline();
        if (!this.enrollmentDateTime.isBefore(applicationDeadLine)) {
            throw new HEException(ENROLLMENT_PERIOD_CLOSED);
        }
    }

    private void hasEnrolledForActivity() {
        boolean isEnrolled = volunteer.getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getActivity().getName().equals(activity.getName()));

        if (isEnrolled) {
            throw new HEException(VOLUNTEER_ALREADY_ENROLLED, volunteer.getName(), activity.getName());
        }
    }
}
