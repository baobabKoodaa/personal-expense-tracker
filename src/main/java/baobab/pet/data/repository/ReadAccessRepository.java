package baobab.pet.data.repository;

import baobab.pet.data.domain.Book;
import baobab.pet.data.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.data.domain.ReadAccess;

import java.util.List;

public interface ReadAccessRepository extends JpaRepository<ReadAccess, Long> {
    ReadAccess findOneByBookAndUser(Book book, User user);
    void deleteByBookAndUser(Book book, User user);
    List<ReadAccess> findByBookOrderByUserName(Book book);
}
