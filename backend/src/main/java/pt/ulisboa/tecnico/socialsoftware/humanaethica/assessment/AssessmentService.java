package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.repository.AssessmentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class AssessmentService {

    @Autowired
    AssessmentRepository assessmentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    InstitutionRepository institutionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AssessmentDto createAssessment(Integer userId, Integer institutionId, AssessmentDto assessmentDto) {
        if (userId == null) {
            throw new HEException(USER_NOT_FOUND);
        }
        Volunteer volunteer = (Volunteer) userRepository.findById(userId)
                .orElseThrow(() -> new HEException(USER_NOT_FOUND, userId));

        if (institutionId == null) {
            throw new HEException(INSTITUTION_NOT_FOUND);
        }
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));

        Assessment assessment = new Assessment(institution, volunteer, assessmentDto);

        assessmentRepository.save(assessment);
        return new AssessmentDto(assessment, true, true);
    }

}
