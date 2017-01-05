package baobab.pet.data.domain;

import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_entity") /* PostgreSQL doesn't allow a table named user */
public class User {

    private long id;
    private boolean current;
    private String name;
    private String encodedPassword;
    private String role;
    private Set<ReadAccess> readAccessSet;
    private Book latestRead;
    private Set<WriteAccess> writeAccessSet;

    @Transient
    private int latestInputYear;
    @Transient
    private int latestInputMonth;
    @Transient
    private boolean requestingToViewAllExpenses;

    public User() {
        super();
        this.current = true;
        this.readAccessSet = new HashSet<>();
        this.writeAccessSet = new HashSet<>();
    }

    public User(String name, String encodedPassword) {
        this();
        this.name = name;
        this.encodedPassword = encodedPassword;
        this.role = "NORMAL_USER";
        this.latestInputMonth = DateTime.now().getMonthOfYear();
        this.latestInputYear = DateTime.now().getYear();
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

    public boolean isRequestingToViewAllExpenses() {
        return requestingToViewAllExpenses;
    }

    public void setRequestingToViewAllExpenses(boolean requestingToViewAllExpenses) {
        this.requestingToViewAllExpenses = requestingToViewAllExpenses;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
