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

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    public Enrollment() {
    }

    public Enrollment(EnrollmentDto enrollmentDto, Activity activity, Volunteer volunteer) {
        setMotivation(enrollmentDto.getMotivation());
        setCreationDate(DateHandler.now());
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
        this.motivation = motivation;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    private void verifyInvariants() {
        motivationLengthAboveMinimum();
    }

    private void motivationLengthAboveMinimum() {
        if (this.motivation == null || this.motivation.length() < MOTIVATION_MINIMUM_LENGTH) {
            throw new HEException(ENROLLMENT_MOTIVATION_TOO_SHORT, MOTIVATION_MINIMUM_LENGTH);
        }
    }

}
