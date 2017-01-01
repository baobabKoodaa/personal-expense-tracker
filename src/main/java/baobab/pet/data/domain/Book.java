package baobab.pet.data.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Book implements Serializable {

    private long id;
    private long groupId; /* determines which categories are available to which books. */
    private String name;
    private User owner;
    private Set<ReadAccess> readAccessSet;
    private Set<WriteAccess> writeAccessSet;

    public Book() {
        super();
        this.readAccessSet = new HashSet<>();
        this.writeAccessSet = new HashSet<>();
    }

    public Book(String name, User owner) {
        this();
        this.name = name;
        this.owner = owner;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "book", orphanRemoval = true)
    public Set<ReadAccess> getReadAccessSet() {
        return readAccessSet;
    }

    public void setReadAccessSet(Set<ReadAccess> readAccessSet) {
        this.readAccessSet = readAccessSet;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "book", orphanRemoval = true)
    public Set<WriteAccess> getWriteAccessSet() {
        return writeAccessSet;
    }

    public void setWriteAccessSet(Set<WriteAccess> writeAccessSet) {
        this.writeAccessSet = writeAccessSet;
    }

    @OneToOne
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
