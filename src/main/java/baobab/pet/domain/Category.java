package baobab.pet.domain;

import javax.persistence.*;

@Entity
public class Category {

    private long id;
    private Long parentCategoryId;
    private String name;

    /** GroupId determines which categories are available to which books. */
    private long groupId;

    public Category() {
        super();
    }

    public Category(String name, long groupId) {
        this();
        this.name = name;
        this.groupId = groupId;
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

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
