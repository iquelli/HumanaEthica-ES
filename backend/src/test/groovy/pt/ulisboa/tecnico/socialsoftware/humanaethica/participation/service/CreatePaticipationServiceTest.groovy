package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import spock.lang.Unroll

@DataJpaTest
class CreateParticipationServiceTest extends SpockTest{

    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def volunteer
    def activity

    def setup() {

        volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.DEMO, User.State.SUBMITTED)
        userRepository.save(volunteer)

        def institution = institutionService.getDemoInstitution()

        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO,ONE_DAY_AGO,[])

        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)
    }

    def "create participation"() {
        given: "a participation dto"

        def participationDto = new ParticipationDto()
        def volunteerDto = new UserDto(volunteer.getAuthUser())
        participationDto.setRating(RATING_1)
        participationDto.setVolunteer(volunteerDto)

        when:
        def result = participationService.createParticipation(activity.getId(), participationDto)

        then: "the returned data is correct"
        result.rating == RATING_1
        result.activity.id == activity.id
        result.volunteer.id == volunteer.id
        and: "the participation is saved in the database"
        participationRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedParticipation = participationRepository.findById(result.id).get()
        storedParticipation.rating == RATING_1
        storedParticipation.activity.id == activity.id
        storedParticipation.volunteer.id == volunteer.id
    }


    @Unroll
    def "invalid arguments: volunteerDto=#volunteerDto | activityId=#activityId"() {
        given: "a participation dto"

        def participationDto = new ParticipationDto()
        participationDto.setRating(RATING_1)
        participationDto.setVolunteer(getVolunteerDto(volunteerDto))

        when:
        participationService.createParticipation(getActivityId(activityId), participationDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and: "no participation is stored in the database"
        participationRepository.count() == 0

        where:
        volunteerDto | activityId  || errorMessage
        null         | EXIST       || ErrorMessage.USER_NOT_FOUND
        NO_EXIST     | EXIST       || ErrorMessage.USER_NOT_FOUND
        EXIST        | null        || ErrorMessage.ACTIVITY_NOT_FOUND
        EXIST        | NO_EXIST    || ErrorMessage.ACTIVITY_NOT_FOUND
    }

    def getVolunteerDto(volunteerDto){
        if (volunteerDto == EXIST)
            return new UserDto(volunteer.getAuthUser())
        else if (volunteerDto == NO_EXIST) {
            def temp = new UserDto(volunteer.getAuthUser())
            temp.setId(222)
            return temp
        }
        return null
    }

    def getActivityId(activityId){
        if (activityId == EXIST)
            return activity.id
        else if (activityId == NO_EXIST)
            return 222
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
