package pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

@DataJpaTest
class CreateParticipationMethodTest extends SpockTest {
    Activity activity = Mock()
    Activity otherActivity = Mock()
    Volunteer volunteer = Mock()
    Participation otherParticipation = Mock()
    Participation otherParticipation_2 = Mock()
    Participation otherParticipation_3 = Mock()
    Participation otherParticipation_4 = Mock()
    Participation otherParticipation_5 = Mock()
    Participation otherParticipation_6 = Mock()
    def participationDto

    def setup() {
        given: "participation info"
        participationDto = new ParticipationDto()
        participationDto.rating = RATING_1;
    }

    def "create successful participation with activity and volunteer having one another participation each: participants=#participants"() {
        given:
        activity.getParticipantsNumberLimit() >> participants
        activity.getParticipations() >> [otherParticipation_2]
        volunteer.getParticipations() >> [otherParticipation]
        otherParticipation.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2
        activity.getApplicationDeadline() >> ONE_DAY_AGO

        when:
        def result = new Paricipation(activity, volunteer, participationDto)

        then: "check result"
        result.getActivity() == activity
        result.getVolunteer() == volunteer
        result.getRating() == RATING_1
        and: "invocations"
        1 * activity.addParticipation(_)
        1 * volunteer.addParticipation(_)

        where participants << [2,3,4,5]
    }

    @Unroll
    def "create participation and violates activity participation limit"() {
        given:
        activity.getParticipantsNumberLimit() >> limitOfParticipants
        activity.getParticipations() >> participants
        volunteer.getParticipations() >> [otherParticipation]
        otherParticipation.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2
        activity.getApplicationDeadline() >> ONE_DAY_AGO

        and: "a participation dto"
        participationDto = new ActivityDto()
        participationDto.setActivity(activity)
        participationDto.setVolunteer(volunteer)
        participationDto.setRating(RATING_1)

        when:
        new Paricipation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.PARTICIPATION_LIMIT_REACHED

        where:
        participants || limitOfParticipants   //ASK TEACHER ABOUT MAKING THIS SIMPLER
        [otherParticipation_2] || 1
        [otherParticipation_2, otherParticipation_3]|| 2
        [otherParticipation_2, otherParticipation_3, otherParticipation_4]|| 3
        [otherParticipation_2, otherParticipation_3, otherParticipation_4, otherParticipation_5]|| 4
        [otherParticipation_2, otherParticipation_3, otherParticipation_4, otherParticipation_5, otherParticipation_6]|| 5
    }

    @Unroll
    def "create participation and violates activity unique participation"() {
        given:
        activity.getParticipantsNumberLimit() >> 5
        activity.getParticipations() >> [otherParticipation]
        volunteer.getParticipations() >> [otherParticipation]   //ASK TEACHER ABOUT REDUNDANT INFO
        otherParticipation.setActivity(activity)
        activity.getName() >> ACTIVITY_NAME_1
        activity.getApplicationDeadline() >> ONE_DAY_AGO

        and: "a participation dto"
        participationDto = new ActivityDto()
        participationDto.setActivity(activity)
        participationDto.setVolunteer(volunteer)
        participationDto.setRating(RATING_1)


        when:
        new Paricipation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.VOLUNTEER_IS_ALREADY_A_PARTICIPANT
    }

    // TODO: ADD 3RD TEST

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}