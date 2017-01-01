package baobab.pet.data.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "book_id", "user_id" })})
public class ReadAccess implements Serializable {

    private long id;
    private Book book;
    private User user;

    public ReadAccess() {
        super();
    }

    public ReadAccess(Book book, User user) {
        this();
        this.book = book;
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
