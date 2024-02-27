package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
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

    @Unroll
    def "create enrollment and violate a volunteer can apply only once to an activity"() {
        given:
        activity.getApplicationDeadline() >> IN_TWO_DAYS
        activity.getName() >> ACTIVITY_NAME_1
        activity.getEnrollments() >> [otherEnrollment]
        volunteer.getEnrollments() >> [otherEnrollment]
        otherEnrollment.getActivity() >> activity

        when:
        new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.VOLUNTEER_ALREADY_ENROLLED
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}