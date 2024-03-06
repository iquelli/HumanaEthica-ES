package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import spock.lang.Unroll

@DataJpaTest
class GetEnrollmentsServiceTest extends SpockTest {

    def activity;

    def setup() {
        def volunteer1 = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL,
                AuthUser.Type.DEMO, User.State.APPROVED)
        def volunteer2 = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD, USER_2_EMAIL,
                AuthUser.Type.DEMO, User.State.APPROVED)

        def institution = institutionService.getDemoInstitution()

        given: "activity info"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, [])
        and: "an activity"
        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        and: "enrollment info"
        def enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_1)
        and: "an enrollment"
        def enrollment = new Enrollment(activity, volunteer1, enrollmentDto)
        enrollmentRepository.save(enrollment)
        and: "another enrollment"
        enrollmentDto.setMotivation(ENROLLMENT_MOTIVATION_2)
        enrollment = new Enrollment(activity, volunteer2, enrollmentDto)
        enrollmentRepository.save(enrollment)
    }

    def 'get two enrollments'() {
        when:
        def result = enrollmentService.getEnrollmentsByActivity(activity.id)

        then:
        result.size() == 2
        result.get(0).motivation == ENROLLMENT_MOTIVATION_1
        result.get(1).motivation == ENROLLMENT_MOTIVATION_2
    }

    @Unroll
    def "invalid arguments: activityId=#activityId"() {
        when:
        enrollmentService.getEnrollmentsByActivity(activityId)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ACTIVITY_NOT_FOUND

        where:
        activityId << [null, 222]
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
