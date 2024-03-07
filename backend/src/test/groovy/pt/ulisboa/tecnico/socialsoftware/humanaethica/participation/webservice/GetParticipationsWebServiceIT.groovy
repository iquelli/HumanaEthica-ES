package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

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

        def user = demoMemberLogin() // TODO remove variable user?

        def volunteer1 = new Volunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.SUBMITTED)
        userRepository.save(volunteer1)
        def volunteer2 = new Volunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.SUBMITTED)
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

    def "get participations"() {
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

        // TODO: also verify volunteer name!
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

}
