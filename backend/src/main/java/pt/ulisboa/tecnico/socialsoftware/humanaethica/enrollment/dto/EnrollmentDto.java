package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class EnrollmentDto {

    private Integer id;
    private String motivation;
    private String creationDate;
    private ActivityDto activity;
    private UserDto volunteer;

    public EnrollmentDto(){
    }

    public EnrollmentDto(Enrollment enrollment){
        setId(enrollment.getId());
        setMotivation(enrollment.getMotivation());
        setCreationDate(DateHandler.toISOString(enrollment.getCreationDate()));
        setActivity(new ActivityDto(enrollment.getActivity(), false));
        setVolunteer(new UserDto(enrollment.getVolunteer()));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public ActivityDto getActivity() {
        return activity;
    }

    public void setActivity(ActivityDto activity) {
        this.activity = activity;
    }

    public UserDto getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(UserDto volunteer) {
        this.volunteer = volunteer;
    }

    @Override
    public String toString() {
        return "EnrollmentDto{" +
                "id=" + id +
                ", motivation='" + motivation + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", activity=" + activity +
                ", volunteer=" + volunteer +
                '}';
    }
}
