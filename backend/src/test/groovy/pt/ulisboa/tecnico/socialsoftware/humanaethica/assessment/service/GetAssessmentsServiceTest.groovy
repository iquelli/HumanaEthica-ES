package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import spock.lang.Unroll

@DataJpaTest
class GetAssessmentsServiceTest extends SpockTest {

    public static final int INEXISTENT_INSTITUTION_ID = 222
    def institution


    def setup() {
        institution = institutionService.getDemoInstitution()

        // add completed activity to institution
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO,ONE_DAY_AGO,null)
        def activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        given: "assessment info"
        def assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)

        and: "an assessment"
        def volunteer = new Volunteer(USER_1_NAME, User.State.SUBMITTED)
        def assessment = new Assessment(institution, volunteer, assessmentDto)
        assessmentRepository.save(assessment)

        and: 'another assessment'
        assessmentDto.review = ASSESSMENT_REVIEW_2
        volunteer = new Volunteer(USER_2_NAME, User.State.SUBMITTED)
        assessment = new Assessment(institution, volunteer, assessmentDto)
        assessmentRepository.save(assessment)
    }

    def 'get two assessments'() {
        when:
        def result = assessmentService.getAssessmentsByInstitution(institution.getId())

        then:
        result.size() == 2
        result.get(0).review == ASSESSMENT_REVIEW_1
        result.get(0).volunteer.getName() == USER_1_NAME
        result.get(1).review == ASSESSMENT_REVIEW_2
        result.get(1).volunteer.getName() == USER_2_NAME
    }

    @Unroll
    def "invalid arguments: institutionId=#institutionId"() {
        when:
        assessmentService.getAssessmentsByInstitution(institutionId)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.INSTITUTION_NOT_FOUND

        where:
        institutionId << [null, INEXISTENT_INSTITUTION_ID]
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}