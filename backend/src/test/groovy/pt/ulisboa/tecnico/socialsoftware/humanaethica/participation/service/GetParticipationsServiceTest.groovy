package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer


@DataJpaTest
class GetParticipationsServiceTest extends SpockTest {

    def activity;
    def volunteer1;
    def volunteer2;

    def setup() {
        volunteer1 = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.DEMO, User.State.SUBMITTED)
        volunteer2 = new Volunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.DEMO, User.State.SUBMITTED)
        userRepository.save(volunteer1)
        userRepository.save(volunteer2)

        def institution = institutionService.getDemoInstitution()

        given: "activity info"

        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 2, ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO, TWO_DAYS_AGO, ONE_DAY_AGO, null)

        and: "an activity"
        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        and: "participation info"
        def participationDto = new ParticipationDto()
        participationDto.setRating(RATING_1)

        and: "an participation"
        def participation = new Participation(activity, volunteer1, participationDto)
        participationRepository.save(participation)

        and: "another participation"
        participationDto.setRating(RATING_2)
        participation = new Participation(activity, volunteer2, participationDto)
        participationRepository.save(participation)
    }

    def 'get two participations'() {
        when:
        def result = participationService.getParticipationsByActivity(activity.id)

        then:
        result.size() == 2
        result.get(0).rating == RATING_1
        result.get(0).activity.id == activity.id
        result.get(0).volunteer.id == volunteer1.id
        result.get(1).rating == RATING_2
        result.get(1).activity.id == activity.id
        result.get(1).volunteer.id == volunteer2.id
    }

    def "invalid arguments: activityId=#activityId"() {
        when:
        participationService.getParticipationsByActivity(activityId)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ACTIVITY_NOT_FOUND

        where:
        activityId << [null, 222]
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}

}

