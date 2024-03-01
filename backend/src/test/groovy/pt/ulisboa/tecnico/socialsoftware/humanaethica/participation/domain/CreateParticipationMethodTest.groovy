package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain

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

    def participationDto

    def setup() {
        given: "participation info"
        participationDto = new ParticipationDto()
        participationDto.rating = RATING_1;
    }

    def "create successful participation with activity and volunteer having one another participation each"() {
        given:
        activity.getName() >> ACTIVITY_NAME_1
        activity.getApplicationDeadline() >> ONE_DAY_AGO
        activity.getParticipantsNumberLimit() >> 2
        activity.getParticipations() >> [otherParticipation]
        volunteer.getParticipations() >> [otherParticipation_2]
        otherParticipation_2.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2

        when:
        def result = new Participation(activity, volunteer, participationDto)

        then: "check result"
        result.getActivity() == activity
        result.getVolunteer() == volunteer
        result.getRating() == RATING_1
        and: "invocations"
        1 * activity.addParticipation(_)
        1 * volunteer.addParticipation(_)
    }

    @Unroll
    def "create participation and violate activity participation limit invariant"() {
        given:
        activity.getName() >> ACTIVITY_NAME_1
        activity.getApplicationDeadline() >> ONE_DAY_AGO
        activity.getParticipantsNumberLimit() >> participantsLimit
        activity.getParticipations() >> [otherParticipation, otherParticipation_2]
        volunteer.getParticipations() >> [otherParticipation_3]
        otherParticipation_3.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.PARTICIPATION_LIMIT_REACHED

        where:
        participantsLimit << [-1, 0, 1] // TODO: is null suppose to work?
    }

    @Unroll
    def "create participation and violate activity unique participation invariant"() {
        given:
        activity.getName() >> ACTIVITY_NAME_1
        activity.getApplicationDeadline() >> ONE_DAY_AGO
        activity.getParticipantsNumberLimit() >> 5
        activity.getParticipations() >> [otherParticipation, otherParticipation_2]
        volunteer.getParticipations() >> [otherParticipation]
        otherParticipation.getActivity() >> activity

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.VOLUNTEER_IS_ALREADY_A_PARTICIPANT
    }

    @Unroll
    def "create participation and violate application after deadline invariant"() {
        given:
        activity.getName() >> ACTIVITY_NAME_1
        activity.getApplicationDeadline() >> IN_ONE_DAY
        activity.getParticipantsNumberLimit() >> 2
        activity.getParticipations() >> [otherParticipation]
        volunteer.getParticipations() >> [otherParticipation_2]
        otherParticipation_2.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.PARTICIPATION_BEFORE_APPLICATION_DEADLINE
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}