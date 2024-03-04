package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution

@DataJpaTest
class CreateAssessmentServiceTest extends SpockTest {

    def volunteer
    def institution

    def setup() {
        volunteer = authUserService.loginDemoVolunteerAuth().getUser()

        institution = institutionService.getDemoInstitution()
        institution.setName(INSTITUTION_1_NAME)

        def theme = new Theme(THEME_NAME_1, Theme.State.APPROVED, null)
        themeRepository.save(theme)
        def themesDto = new ArrayList<>()
        themesDto.add(new ThemeDto(theme, false, false, false))

        // add completed activity to institution
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, ACTIVITY_DESCRIPTION_1,
                                            THREE_DAYS_AGO, TWO_DAYS_AGO, ONE_DAY_AGO, themesDto)
        def activity = new Activity(activityDto, institution, [theme])
        activityRepository.save(activity)

        institutionRepository.save(institution)
    }

    def "create assessment"() {
        given: "an assessment dto"
        def assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)

        when:
        def result = assessmentService.createAssessment(volunteer.getId(), institution.getId(), assessmentDto)

        then: "the returned data is correct"
        result.review == ASSESSMENT_REVIEW_1
        result.institution.id == institution.id
        result.volunteer.id == volunteer.id
        and: "the assessment is saved in the database"
        assessmentRepository.findAll().size() == 1
        and: "the stored data is correct"
        def storedAssessment = assessmentRepository.findById(result.id).get()
        storedAssessment.review == ASSESSMENT_REVIEW_1
        storedAssessment.institution.id == institution.id
        storedAssessment.volunteer.id == volunteer.id
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}

}
