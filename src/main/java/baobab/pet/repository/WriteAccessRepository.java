package baobab.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.domain.Book;
import baobab.pet.domain.User;
import baobab.pet.domain.WriteAccess;

public interface WriteAccessRepository extends JpaRepository<WriteAccess, Long> {
    WriteAccess findOneByBookAndUser(Book book, User user);
}
