package baobab.pet.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.data.domain.Book;
import baobab.pet.data.domain.User;
import baobab.pet.data.domain.WriteAccess;

public interface WriteAccessRepository extends JpaRepository<WriteAccess, Long> {
    WriteAccess findOneByBookAndUser(Book book, User user);
    void deleteByBookAndUser(Book book, User user);
}
