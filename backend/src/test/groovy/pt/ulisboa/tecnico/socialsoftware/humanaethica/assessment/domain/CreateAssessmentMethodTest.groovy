package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import spock.lang.Unroll


@DataJpaTest
class CreateAssessmentMethodTest extends SpockTest {
    Institution institution = Mock()
    Activity activity = Mock()
    Volunteer volunteer = Mock()
    Volunteer otherVolunteer = Mock()
    Assessment otherAssessment = Mock()
    def assessmentDto

    def setup() {
        given: "assessment info"
        assessmentDto = new AssessmentDto()
        assessmentDto.review = ASSESSMENT_REVIEW_1
    }

    def "create successful assessment with volunteer and institution has another assessment"() {
        given:
        activity.getEndingDate() >> TWO_DAYS_AGO
        institution.getActivities() >> [activity]
        volunteer.getName() >> USER_1_NAME
        institution.getAssessments() >> [otherAssessment]
        institution.getName() >> INSTITUTION_1_NAME
        otherAssessment.getVolunteer() >> otherVolunteer
        otherVolunteer.getName() >> USER_2_NAME

        and: "an assessment dto"
        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)

        when:
        def result = new Assessment(institution, volunteer, assessmentDto)

        then: "check result"
        result.getInstitution() == institution
        result.getVolunteer() == volunteer
        result.getReview() == ASSESSMENT_REVIEW_1

        and: "invocations"
        1 * volunteer.addAssessment(_)
        1 * institution.addAssessment(_)
    }

    @Unroll
    def "create assessment and violate review cannot have less than 10 characters"() {
        given:
        activity.getEndingDate() >> TWO_DAYS_AGO
        institution.getActivities() >> [activity]
        institution.getName() >> INSTITUTION_1_NAME
        volunteer.getName() >> USER_1_NAME

        and: "an assessment dto"
        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(review)

        when:
        def result = new Assessment(institution, volunteer, assessmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_REVIEW_TOO_SHORT

        where:
        review << [null, "", " ", "123456789", "review"]
    }

    @Unroll
    def "create assessment and violate institution must have a completed activity"() {
        given:
        activity.getEndingDate() >> IN_TWO_DAYS
        institution.getActivities() >> [activity]
        institution.getName() >> INSTITUTION_1_NAME
        volunteer.getName() >> USER_1_NAME

        and: "an assessment dto"
        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)

        when:
        def result = new Assessment(institution, volunteer, assessmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_INSTITUTION_WITHOUT_COMPLETED_ACTIVITY
    }

    @Unroll
    def "create assessment and violate volunteer can only evaluate an institution once"() {
        given:
        activity.getEndingDate() >> TWO_DAYS_AGO
        institution.getActivities() >> [activity]
        volunteer.getName() >> USER_1_NAME
        institution.getAssessments() >> [otherAssessment]
        institution.getName() >> INSTITUTION_1_NAME
        otherAssessment.getVolunteer() >> volunteer

        and: "an assessment dto"
        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)

        when:
        def result = new Assessment(institution, volunteer, assessmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ASSESSMENT_VOLUNTEER_HAS_ASSESSED_INSTITUTION
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
