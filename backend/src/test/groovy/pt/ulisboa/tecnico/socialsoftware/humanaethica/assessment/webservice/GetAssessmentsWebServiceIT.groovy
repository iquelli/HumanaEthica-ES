package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetAssessmentsWebServiceIT extends SpockTest {

    @LocalServerPort
    private int port

    def institution

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        given: "two volunteers"
        def volunteer1 = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD,
                                        USER_1_EMAIL, AuthUser.Type.DEMO, User.State.APPROVED)
        def volunteer2 = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD,
                                        USER_2_EMAIL, AuthUser.Type.DEMO, User.State.APPROVED)

        and: "an institution with a completed activity"
        institution = institutionService.getDemoInstitution()
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1, 
                            ACTIVITY_DESCRIPTION_1, THREE_DAYS_AGO, TWO_DAYS_AGO, ONE_DAY_AGO, [])
        def activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)
        institutionRepository.save(institution)
        
        and: "assessment info"
        def assessmentDto = new AssessmentDto()

        and: "an assessment"
        assessmentDto.setReview(ASSESSMENT_REVIEW_1)
        def assessment = new Assessment(institution, volunteer1, assessmentDto)
        assessmentRepository.save(assessment)

        and: "another assessment"
        assessmentDto.setReview(ASSESSMENT_REVIEW_2)
        assessment = new Assessment(institution, volunteer2, assessmentDto)
        assessmentRepository.save(assessment)
    }

    def "get assessments"() {
        when:
        def response = webClient.get()
                        .uri('/assessments/' + institution.getId())
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .retrieve()
                        .bodyToFlux(AssessmentDto.class)
                        .collectList()
                        .block()

        then: "check response"
        response.size() == 2
        response.get(0).review == ASSESSMENT_REVIEW_1
        response.get(0).volunteer.name == USER_1_NAME
        response.get(1).review == ASSESSMENT_REVIEW_2
        response.get(1).volunteer.name == USER_2_NAME

        cleanup:
        deleteAll()
    }

    def "get assessments while logged in as a member"() {
        given: "a member"
        demoMemberLogin()

        when:
        def response = webClient.get()
                        .uri('/assessments/' + institution.getId())
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .retrieve()
                        .bodyToFlux(AssessmentDto.class)
                        .collectList()
                        .block()

        then: "check response"
        response.size() == 2
        response.get(0).review == ASSESSMENT_REVIEW_1
        response.get(0).volunteer.name == USER_1_NAME
        response.get(1).review == ASSESSMENT_REVIEW_2
        response.get(1).volunteer.name == USER_2_NAME

        cleanup:
        deleteAll()
    }

    def "get assessments while logged in as a member of another institution"() {
        given: "a member of another institution"
        def otherInstitution = new Institution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        institutionRepository.save(otherInstitution)
        def otherMember = createMember(USER_3_NAME, USER_3_USERNAME, USER_3_PASSWORD, USER_3_EMAIL,
                                        AuthUser.Type.NORMAL, otherInstitution, User.State.APPROVED)
        normalUserLogin(USER_3_USERNAME, USER_3_PASSWORD)

        when:
        def response = webClient.get()
                        .uri('/assessments/' + institution.getId())
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .retrieve()
                        .bodyToFlux(AssessmentDto.class)
                        .collectList()
                        .block()

        then: "check response"
        response.size() == 2
        response.get(0).review == ASSESSMENT_REVIEW_1
        response.get(0).volunteer.name == USER_1_NAME
        response.get(1).review == ASSESSMENT_REVIEW_2
        response.get(1).volunteer.name == USER_2_NAME

        cleanup:
        deleteAll()
    }

    def "get assessments while logged in as a volunteer"() {
        given: "a volunteer"
        demoVolunteerLogin()

        when:
        def response = webClient.get()
                        .uri('/assessments/' + institution.getId())
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .retrieve()
                        .bodyToFlux(AssessmentDto.class)
                        .collectList()
                        .block()

        then: "check response"
        response.size() == 2
        response.get(0).review == ASSESSMENT_REVIEW_1
        response.get(0).volunteer.name == USER_1_NAME
        response.get(1).review == ASSESSMENT_REVIEW_2
        response.get(1).volunteer.name == USER_2_NAME

        cleanup:
        deleteAll()
    }

    def "get assessments while logged in as an admin"() {
        given: "an admin"
        demoAdminLogin()

        when:
        def response = webClient.get()
                        .uri('/assessments/' + institution.getId())
                        .headers(httpHeaders -> httpHeaders.putAll(headers))
                        .retrieve()
                        .bodyToFlux(AssessmentDto.class)
                        .collectList()
                        .block()

        then: "check response"
        response.size() == 2
        response.get(0).review == ASSESSMENT_REVIEW_1
        response.get(0).volunteer.name == USER_1_NAME
        response.get(1).review == ASSESSMENT_REVIEW_2
        response.get(1).volunteer.name == USER_2_NAME

        cleanup:
        deleteAll()
    }

}
