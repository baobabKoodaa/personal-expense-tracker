package baobab.pet.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.data.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findOneByLoginname(String loginname);
}
