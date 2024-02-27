package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue(User.UserTypes.VOLUNTEER)
public class Volunteer extends User {

    @OneToMany(mappedBy = "volunteer", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Assessment> assessments = new ArrayList<>();

    @OneToMany(mappedBy = "volunteer", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();

    public Volunteer() {
    }

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Participation> participations = new ArrayList<>();

    public Volunteer(String name, String username, String email, AuthUser.Type type, State state) {
        super(name, username, email, Role.VOLUNTEER, type, state);
    }

    public Volunteer(String name, State state) {
        super(name, Role.VOLUNTEER, state);
    }

    public List<Assessment> getAssessments() {
        return this.assessments;
    }

    public void addAssessment(Assessment assessment) {
        this.assessments.add(assessment);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
    }


    public List<Participation> getParticipations() {
        return participations;
    }

    public void addParticipation(Participation participation){
        this.participations.add(participation);
    }
}
