package baobab.pet.data.domain;

import javax.persistence.*;

@Entity
public class Category {

    private long id;
    private long groupId; /* determines which categories are available to which books. */
    private String name;

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
