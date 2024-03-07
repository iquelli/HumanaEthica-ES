package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetEnrollmentsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def activity

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def user = demoMemberLogin()

        def volunteer1 = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL,
                AuthUser.Type.DEMO, User.State.APPROVED)
        def volunteer2 = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD, USER_2_EMAIL,
                AuthUser.Type.DEMO, User.State.APPROVED)

        def institution = institutionService.getDemoInstitution()

        given: "activity info"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 1,
                ACTIVITY_DESCRIPTION_1, IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, [])
        and: "an activity"
        activityDto = activityService.registerActivity(user.id, activityDto)
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

    def "get enrollments"() {
        given: 'a member'
        demoMemberLogin()

        when:
        def response = webClient.get()
                .uri('/enrollments/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 2
        response.get(0).motivation == ENROLLMENT_MOTIVATION_1
        response.get(0).activity.name == ACTIVITY_NAME_1
        response.get(1).motivation == ENROLLMENT_MOTIVATION_2
        response.get(1).activity.name == ACTIVITY_NAME_1

        cleanup:
        deleteAll()
    }

    def "login as member of another institution and get enrollments"() {
        given: 'another member'
        def otherInstitution = new Institution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        institutionRepository.save(otherInstitution)
        def otherMember = createMember(USER_3_NAME, USER_3_USERNAME, USER_3_PASSWORD, USER_3_EMAIL, AuthUser.Type.NORMAL,
                otherInstitution, User.State.APPROVED)
        normalUserLogin(USER_3_USERNAME, USER_3_PASSWORD)

        when:
        webClient.get()
                .uri('/enrollments/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "login as volunteer, and get enrollments"() {
        given: 'a volunteer'
        demoVolunteerLogin()

        when:
        webClient.get()
                .uri('/enrollments/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "login as admin, and get enrollments"() {
        given: 'an admin'
        demoAdminLogin()

        when:
        webClient.get()
                .uri('/enrollments/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

}
