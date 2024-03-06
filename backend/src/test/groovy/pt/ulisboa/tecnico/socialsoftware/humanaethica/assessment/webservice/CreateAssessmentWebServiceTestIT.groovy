package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateAssessmentWebServiceTestIT extends SpockTest {

    @LocalServerPort
    private int port

    def assessmentDto
    def institutionId

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def institution = institutionService.getDemoInstitution()

        // add completed activity to institution
        def themesDto = []
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO, ONE_DAY_AGO,themesDto)
        def activity = new Activity(activityDto, institution, [] )
        activityRepository.save(activity)

        institutionId = institution.id

        assessmentDto = new AssessmentDto()
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)
    }

    def "login as volunteer, and create an assessment"() {
        given:
        demoVolunteerLogin()

        when:
        def response = webClient.post()
                .uri('/assessments/' + institutionId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "check response data"
        response.review == ASSESSMENT_REVIEW_1
        response.institution.getName() == INSTITUTION_DEMO_NAME
        response.volunteer.getName() == VOLUNTEER_DEMO_NAME

        and: 'check database data'
        assessmentRepository.count() == 1
        def assessment = assessmentRepository.findAll().get(0)
        assessment.getReview() == ASSESSMENT_REVIEW_1
        assessment.getInstitution().getName() == INSTITUTION_DEMO_NAME
        assessment.getVolunteer().getName() == VOLUNTEER_DEMO_NAME

        cleanup:
        deleteAll()
    }

    def "login as member, and assess an institution"() {
        given: 'a member'
        demoMemberLogin()

        when: 'the member assesses an institution'
        webClient.post()
                .uri('/assessments/' + institutionId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        assessmentRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as admin, and assess an institution"() {
        given: 'an admin'
        demoAdminLogin()

        when: 'the admin assesses an institution'
                webClient.post()
                .uri('/assessments/' + institutionId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(assessmentDto)
                .retrieve()
                .bodyToMono(AssessmentDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        assessmentRepository.count() == 0

        cleanup:
        deleteAll()
    }

}
