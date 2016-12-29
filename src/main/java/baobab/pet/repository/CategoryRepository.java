package baobab.pet.repository;

import baobab.pet.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findOneByName(String name);
}
