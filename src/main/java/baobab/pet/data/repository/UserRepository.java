package baobab.pet.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.data.domain.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findOneByName(String name);
    List<User> findByCurrentOrderByIdAsc(boolean current);
}
