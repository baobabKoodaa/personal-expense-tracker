package baobab.pet.domain;

import javax.persistence.*;

@Entity
public class Category {

    private long id;
    private Long parentCategoryId;
    private String name;

    public Category() {
        super();
    }

    public Category(String name) {
        this();
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(nullable = true)
    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
