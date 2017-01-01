package baobab.pet.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.domain.ReadAccess;

public interface ReadAccessRepository extends JpaRepository<ReadAccess, Long> {
}
