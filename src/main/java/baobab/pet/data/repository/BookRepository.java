package baobab.pet.data.repository;

import baobab.pet.data.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Book findOneByName(String name);

}
