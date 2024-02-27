package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import spock.lang.Unroll

@DataJpaTest
class CreateEnrollmentMethodTest extends SpockTest {
    Activity activity = Mock()
    Activity otherActivity = Mock()
    Volunteer volunteer = Mock()
    Enrollment otherEnrollment = Mock()
    def enrollmentDto

    def setup() {
        given: "enrollment info"
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.motivation = ENROLLMENT_MOTIVATION_1;
    }

    def "create successful enrollment with activity and volunteer has another enrollment"() {
        given:
        activity.getApplicationDeadline() >> IN_TWO_DAYS
        activity.getName() >> ACTIVITY_NAME_1
        volunteer.getEnrollments() >> [otherEnrollment]
        otherEnrollment.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2

        when:
        def result = new Enrollment(activity, volunteer, enrollmentDto)

        then: "check result"
        result.getActivity() == activity
        result.getMotivation() == ENROLLMENT_MOTIVATION_1
        result.getVolunteer() == volunteer
        and: "invocations"
        1 * volunteer.addEnrollment(_)
        1 * activity.addEnrollment(_)
    }

    def "create enrollment and violate a volunteer can apply only once to an activity"() {
        given:
        activity.getApplicationDeadline() >> IN_TWO_DAYS
        activity.getName() >> ACTIVITY_NAME_1
        volunteer.getEnrollments() >> [otherEnrollment]
        otherEnrollment.getActivity() >> activity

        when:
        new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.VOLUNTEER_ALREADY_ENROLLED
    }

    def "create enrollment and violate volunteer cannot apply after deadline"() {
        given:
        activity.getApplicationDeadline() >> ONE_DAY_AGO
        activity.getName() >> ACTIVITY_NAME_1
        volunteer.getEnrollments() >> [otherEnrollment]
        otherEnrollment.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2

        when:
        new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_PERIOD_CLOSED
    }

    def "create enrollment and violate volunteers must provide a motivation of at least 10 character"() {
        given:
        activity.getApplicationDeadline() >> IN_TWO_DAYS
        activity.getName() >> ACTIVITY_NAME_1
        volunteer.getEnrollments() >> [otherEnrollment]
        otherEnrollment.getActivity() >> otherActivity
        otherActivity.getName() >> ACTIVITY_NAME_2
        enrollmentDto.setMotivation(motivation)

        when:
        new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ENROLLMENT_MOTIVATION_TOO_SHORT

        where:
        motivation << [null, "", " ", "motivatio", "12*123!"]
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}