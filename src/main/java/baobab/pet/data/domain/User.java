package baobab.pet.data.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User {

    private long id;
    private String name;
    private String encodedPassword;
    private String role;
    private Set<ReadAccess> readAccessSet;
    private Book latestRead;
    private int latestInputYear;
    private int latestInputMonth;
    private Set<WriteAccess> writeAccessSet;

    public User() {
        super();
        this.readAccessSet = new HashSet<>();
        this.writeAccessSet = new HashSet<>();
    }

    public User(String name, String encodedPassword) {
        this();
        this.name = name;
        this.encodedPassword = encodedPassword;
        this.role = "NORMAL_USER";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(unique = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 60, nullable = false)
    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    @Column(nullable = false)
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    public Set<ReadAccess> getReadAccessSet() {
        return readAccessSet;
    }

    public void setReadAccessSet(Set<ReadAccess> readAccessSet) {
        this.readAccessSet = readAccessSet;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    public Set<WriteAccess> getWriteAccessSet() {
        return writeAccessSet;
    }

    public void setWriteAccessSet(Set<WriteAccess> writeAccessSet) {
        this.writeAccessSet = writeAccessSet;
    }

    @ManyToOne
    @JoinColumn(name = "book_id")
    public Book getLatestRead() {
        return this.latestRead;
    }

    public void setLatestRead(Book latestRead) {
        this.latestRead = latestRead;
    }

    public int getLatestInputYear() {
        return latestInputYear;
    }

    public void setLatestInputYear(int latestInputYear) {
        this.latestInputYear = latestInputYear;
    }

    public int getLatestInputMonth() {
        return latestInputMonth;
    }

    public void setLatestInputMonth(int latestInputMonth) {
        this.latestInputMonth = latestInputMonth;
    }
}
