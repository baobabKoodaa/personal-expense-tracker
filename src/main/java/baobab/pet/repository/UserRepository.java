package baobab.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findOneByLoginname(String loginname);
}
