package baobab.pet.data.domain;

import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
public class Expense {

    private long id;
    private boolean current;
    private int year;
    private int month;
    private Book book;
    private Category category;
    private String details;
    private long amountCents;
    private User user;
    private Long previousVersionId;
    private Long nextVersionId;
    private long timeAdded;

    public Expense() {
        super();
    }

    public Expense(int year, int month, Book book, Category category, String details, long amountCents, User user) {
        this();
        this.current = true;
        this.year = year;
        this.month = month;
        this.book = book;
        this.category = category;
        this.details = details;
        this.amountCents = amountCents;
        this.user = user;
        this.timeAdded = DateTime.now().getMillis();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public Long getPreviousVersionId() {
        return previousVersionId;
    }

    public void setPreviousVersionId(Long previousVersionId) {
        this.previousVersionId = previousVersionId;
    }

    public Long getNextVersionId() {
        return nextVersionId;
    }

    public void setNextVersionId(Long nextVersionId) {
        this.nextVersionId = nextVersionId;
    }

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }

    @ManyToOne
    @JoinColumn(name = "user_entity_id", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public String parseTimeAdded() {
        return new DateTime((this.getTimeAdded())).toString("dd/MM/yyyy");
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String amountFormatted() {
        if (amountCents < 10) {
            return "0.0" + amountCents;
        }
        if (amountCents < 100) {
            return "0."+amountCents;
        }
        String a = ""+amountCents;
        String euros = a.substring(0, a.length()-2);
        String cents = a.substring(a.length()-2);
        return euros + "." + cents;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", bookName=" + book.getName() +
                ", categoryName=" + category.getName() +
                ", details='" + details + '\'' +
                ", amountCents=" + amountCents +
                ", username=" + user.getName() +
                ", timeAdded=" + timeAdded +
                '}';
    }
}
