package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;

import java.security.Principal;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @PostMapping("/{institutionId}")
    @PreAuthorize("(hasRole('ROLE_VOLUNTEER'))") 
    public AssessmentDto createAssessment(Principal principal, @PathVariable Integer institutionId, 
            @Valid @RequestBody AssessmentDto assessmentDto) {
        int userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();
        return assessmentService.createAssessment(userId, institutionId, assessmentDto);
    }

}
