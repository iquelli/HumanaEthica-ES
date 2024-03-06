package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "participation")
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer rating;
    private LocalDateTime acceptanceDate;

    @ManyToOne
    private Activity activity;

    @ManyToOne
    private Volunteer volunteer;

    public Participation() {
    }

    public Participation(Activity activity, Volunteer volunteer, ParticipationDto participationDto) {
        setActivity(activity);
        setVolunteer(volunteer);
        setAcceptanceDate(DateHandler.now());
        setRating(participationDto.getRating());

        verifyInvariants();
    }


    public Integer getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(LocalDateTime acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addParticipation(this);
    }

    public Activity getActivity() {
        return activity;
    }


    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addParticipation(this);
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    private void verifyInvariants() {
        numberOfParticipantsBelowLimit();
        participatesInActivityOnlyOnce();
        participationAfterApplicationDeadline();
    }

    private void numberOfParticipantsBelowLimit(){
       if (activity.getParticipations().stream().count() > activity.getParticipantsNumberLimit()){
           throw new HEException(PARTICIPATION_LIMIT_REACHED, activity.getName());
       }
    }

    private void participatesInActivityOnlyOnce(){
        boolean isParticipant = volunteer.getParticipations().stream().
                anyMatch(participation -> participation != this && participation.getActivity().getName().equals(activity.getName()));
        if (isParticipant){
            throw new HEException(VOLUNTEER_IS_ALREADY_A_PARTICIPANT, volunteer.getName(), activity.getName());
        }
    }

    private void participationAfterApplicationDeadline() {
        LocalDateTime applicationDeadLine = activity.getApplicationDeadline();
        if (!this.acceptanceDate.isAfter(applicationDeadLine)) {
            throw new HEException(PARTICIPATION_BEFORE_APPLICATION_DEADLINE);
        }
    }
}
