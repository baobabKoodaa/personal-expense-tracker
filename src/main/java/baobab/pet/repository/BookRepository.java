package baobab.pet.repository;

import baobab.pet.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Book findOneByName(String name);
}