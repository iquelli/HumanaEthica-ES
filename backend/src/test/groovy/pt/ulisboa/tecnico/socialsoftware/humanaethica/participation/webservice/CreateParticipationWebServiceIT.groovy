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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.repository.ParticipationRepository
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateParticipationWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def participationDto
    def activityId

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def volunteer = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.SUBMITTED)
        userRepository.save(volunteer)

        def institution = institutionService.getDemoInstitution()

        def themesDto = new ArrayList<>()

        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
                THREE_DAYS_AGO,TWO_DAYS_AGO,ONE_DAY_AGO,themesDto)

        def activity = new Activity(activityDto, institution, [] )
        activityRepository.save(activity)

        activityId = activity.id

        participationDto = new ParticipationDto()

        def volunteerDto = new UserDto(volunteer.getAuthUser())

        participationDto.setVolunteer(volunteerDto)
        participationDto.setRating(RATING_1)
    }

    def "login as member, and create a participation"() {
        given:
        demoMemberLogin()

        when:
        def response = webClient.post()
                .uri('/participations/'+ activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(participationDto)
                .retrieve()
                .bodyToMono(ParticipationDto.class)
                .block()

        then: "check response data"
        response.rating == RATING_1
        and: 'check database data'
        participationRepository.count() == 1
        def participation = participationRepository.findAll().get(0)
        participation.getRating() == RATING_1

        cleanup:
        deleteAll()
    }

    def "login as volunteer, and create a participation"() {
        given:
        demoVolunteerLogin()

        when:
        webClient.post()
                .uri('/participations/'+ activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(participationDto)
                .retrieve()
                .bodyToMono(ParticipationDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        participationRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "login as admin, and create a participation"() {
        given:
        demoAdminLogin()

        when:
        webClient.post()
                .uri('/participations/'+ activityId)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(participationDto)
                .retrieve()
                .bodyToMono(ParticipationDto.class)
                .block()

        then: "an error is returned"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        participationRepository.count() == 0

        cleanup:
        deleteAll()
    }

}
