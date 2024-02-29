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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

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
        assessmentDto.review = ASSESMENT_REVIEW_1
    }

    def "create successful assessment with volunteer and institution has another assessment"() {
        given:
        activity.getEndingDate() >> TWO_DAYS_AGO
        institution.getActivities() >> [activity]
        volunteer.getName() >> USER_1_NAME
        institution.getAssessments() >> [otherAssessment]
        otherAssessment.getVolunteer() >> otherVolunteer
        otherVolunteer.getName() >> USER_2_NAME


        when:
        def result = new Assessment(assessmentDto, institution, volunteer)

        then: "check result"
        result.getInstitution() == institution
        result.getVolunteer() == volunteer
        result.getReview() == ASSESMENT_REVIEW_1

        and: "invocations"
        1 * volunteer.addAssessment(_)
        1 * institution.addAssessment(_)
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}