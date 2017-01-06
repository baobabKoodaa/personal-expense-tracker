package baobab.pet.data.repository;

import baobab.pet.data.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findOneByNameAndGroupId(String name, long groupId);
    List<Category> findByGroupId(long groupId);
    List<Category> findByGroupIdAndHidden(long groupId, boolean hidden);
}
