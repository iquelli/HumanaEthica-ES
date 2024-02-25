package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.util.List;

public class ParticipationDto {
    private Integer id;
    private Integer rating;
    private String acceptanceDate;
    private ActivityDto activity;
    private Volunteer volunteer;

    public ParticipationDto(){
    }

    // TODO: need deep copy for activity like the one used in ActivityDto ?
    public ParticipationDto(Participation participation){
        setId(participation.getId());
        setRating(participation.getRating());
        setVolunteer(participation.getVolunteer());
        setActivity(new ActivityDto(participation.getActivity(), false));

        setAcceptanceDate(DateHandler.toISOString(participation.getAcceptanceDate()));

        /* TODO uncomment if deep copy is needed
        if (deepCopyActivity && (participation.getActivity() != null)) {
            setActivity(new ActivityDto(participation.getActivity(), false));
        }
         */
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(String acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public ActivityDto getActivity() {
        return activity;
    }

    public void setActivity(ActivityDto activity) {
        this.activity = activity;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "ParticipantDto{" +
                "id=" + id +
                ", rating=" + rating +
                ", acceptanceDate='" + acceptanceDate + '\'' +
                ", activity=" + activity +
                ", volunteer=" + volunteer +
                '}';
    }
}
