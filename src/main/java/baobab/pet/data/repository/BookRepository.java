package baobab.pet.data.repository;

import baobab.pet.data.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    Book findOneByName(String name);
    List<Book> findByGroupId(long groupId);
}
