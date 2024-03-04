package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import spock.lang.Unroll

@DataJpaTest
class CreateEnrollmentServiceTest extends SpockTest {

    public static final String EXIST = "exist"
    public static final String NO_EXIST = "noExist"

    def volunteer
    def activity

    def setup() {
        volunteer = authUserService.loginDemoVolunteerAuth().getUser()

        def institution = institutionService.getDemoInstitution()

        def themesDto = new ArrayList<>()

        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS,themesDto)

        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)
    }

    def "create enrollment"() {
        given: "an enrollment dto"

        def enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_1)

        when:
        def result = enrollmentService.createEnrollment(volunteer.getId(), activity.getId(), enrollmentDto)

        then: "the returned data is correct"
        result.motivation == ENROLLMENT_MOTIVATION_1
        result.activity.id == activity.id
        result.volunteer.id == volunteer.id
        and: "the enrollment is saved in the database"
        enrollmentRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedEnrollment = enrollmentRepository.findById(result.id).get()
        storedEnrollment.motivation == ENROLLMENT_MOTIVATION_1
        storedEnrollment.activity.id == activity.id
        storedEnrollment.volunteer.id == volunteer.id
    }

    @Unroll
    def "invalid arguments: motivation=#motivation | volunteerId=#volunteerId | activityId=#activityId"() {
        given: "an enrollment dto"

        def enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(motivation)

        when:
        enrollmentService.createEnrollment(getVolunteerId(volunteerId), getActivityId(activityId), enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and: "no enrollment is stored in the database"
        enrollmentRepository.count() == 0


        where:
        motivation              | volunteerId | activityId  || errorMessage
        null                    | EXIST       | EXIST       || ErrorMessage.ENROLLMENT_MOTIVATION_TOO_SHORT
        ENROLLMENT_MOTIVATION_1 | null        | EXIST       || ErrorMessage.USER_NOT_FOUND
        ENROLLMENT_MOTIVATION_1 | NO_EXIST    | EXIST       || ErrorMessage.USER_NOT_FOUND
        ENROLLMENT_MOTIVATION_1 | EXIST       | null        || ErrorMessage.ACTIVITY_NOT_FOUND
        ENROLLMENT_MOTIVATION_1 | EXIST       | NO_EXIST    || ErrorMessage.ACTIVITY_NOT_FOUND
    }

    def getVolunteerId(volunteerId){
        if (volunteerId == EXIST)
            return volunteer.id
        else if (volunteerId == NO_EXIST)
            return 222
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
