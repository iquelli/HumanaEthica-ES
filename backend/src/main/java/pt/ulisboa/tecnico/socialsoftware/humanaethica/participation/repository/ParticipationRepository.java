package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;

@Repository
@Transactional
public interface ParticipationRepository extends JpaRepository<Participation, Integer> {

}
