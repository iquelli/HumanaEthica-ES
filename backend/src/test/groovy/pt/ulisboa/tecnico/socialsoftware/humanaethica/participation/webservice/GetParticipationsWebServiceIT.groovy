package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.webservice

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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetParticipationsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def activity

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        demoMemberLogin()

        def volunteer1 = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL,
                AuthUser.Type.DEMO, User.State.APPROVED)
        userRepository.save(volunteer1)
        def volunteer2 = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD, USER_2_EMAIL,
                AuthUser.Type.DEMO, User.State.APPROVED)
        userRepository.save(volunteer2)

        def volunteerDto1 = new UserDto(volunteer1.getAuthUser())
        def volunteerDto2 = new UserDto(volunteer2.getAuthUser())

        def institution = institutionService.getDemoInstitution()


        given: "activity info"
        def activityDto = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, ACTIVITY_NUMBER_2, ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO,ONE_DAY_AGO,[])
        and: "an activity"
        activity = new Activity(activityDto, institution, [])
        activityRepository.save(activity)

        and: "participation info"
        def participationDto = new ParticipationDto()
        participationDto.setVolunteer(volunteerDto1)
        participationDto.setRating(RATING_1)

        and: "a participation"
        def participation = new Participation(activity, volunteer1, participationDto)
        participationRepository.save(participation)

        and: "another participation"
        participationDto.setVolunteer(volunteerDto2)
        participationDto.setRating(RATING_2)
        participation = new Participation(activity, volunteer2, participationDto)
        participationRepository.save(participation)
    }

    def "get participations of activity"() {
        given: 'a member'
        demoMemberLogin()

        when:
        def response = webClient.get()
                .uri('/participations/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ParticipationDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 2
        response.get(0).rating == RATING_1
        response.get(0).activity.name == ACTIVITY_NAME_1
        response.get(0).volunteer.name == USER_1_NAME
        response.get(1).rating == RATING_2
        response.get(1).activity.name == ACTIVITY_NAME_1
        response.get(1).volunteer.name == USER_2_NAME

        cleanup:
        deleteAll()
    }

    def "login as member of another institution and get participations of activity"() {
        given: 'another member'
        def otherInstitution = new Institution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        institutionRepository.save(otherInstitution)
        createMember(USER_3_NAME, USER_3_USERNAME, USER_3_PASSWORD, USER_3_EMAIL, AuthUser.Type.NORMAL,
                otherInstitution, User.State.APPROVED)
        normalUserLogin(USER_3_USERNAME, USER_3_PASSWORD)

        when:
        webClient.get()
                .uri('/participations/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ParticipationDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "login as volunteer, and get participations of activity"() {
        given: 'a volunteer'
        demoVolunteerLogin()

        when:
        webClient.get()
                .uri('/participations/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ParticipationDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "login as admin, and get participations of activity"() {
        given: 'an admin'
        demoAdminLogin()

        when:
        webClient.get()
                .uri('/participations/' + activity.getId())
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ParticipationDto.class)
                .collectList()
                .block()

        then: "check response status"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

}
