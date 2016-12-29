package baobab.pet.repository;

import baobab.pet.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findOneByNameAndGroupId(String name, long groupId);
    List<Category> findByGroupId(long groupId);
}
