package baobab.pet.data;

import baobab.pet.data.domain.*;
import baobab.pet.data.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

@Component
public class DAO {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    ReadAccessRepository readAccessRepository;

    @Autowired
    WriteAccessRepository writeAccessRepository;

    public User findUserByName(String name) {
        return userRepository.findOneByName(name);
    }

    public Book findBookById(Long bookId) {
        return bookRepository.findOne(bookId);
    }

    public Expense findExpenseById(Long expenseId) {
        return expenseRepository.findOne(expenseId);
    }

    public boolean hasWriteAccess(User user, Book book) {
        WriteAccess writeAccess = writeAccessRepository.findOneByBookAndUser(book, user);
        return (writeAccess != null);
    }

    public boolean hasReadAccess(User user, Book book) {
        ReadAccess readAccess = readAccessRepository.findOneByBookAndUser(book, user);
        return (readAccess != null);
    }

    public User createUser(String userName, String clearTextPassword) {
        User user = new User(userName, encode(clearTextPassword));
        userRepository.save(user);
        return user;
    }

    /** Tries to, in order,
     * 1. Return most recently used book
     * 2. Return any book
     * 3. Create new book and return it. */
    @Transactional
    public Book detLatestBookForUser(User user) {
        Book latest = user.getLatestRead();
        if (latest == null) {
            /** Assign any accessible book as latest. */
            for (ReadAccess r : user.getReadAccessSet()) {
                latest = r.getBook();
                setBookAsLatestForUser(latest, user);
                break;
            }
        }
        if (latest == null) {
            /** If user has deleted all their books. */
            latest = createBook("New book", user);
            setBookAsLatestForUser(latest, user);
        }
        return latest;
    }

    public Category detCategory(String categoryName, Book book) {
        long groupId = book.getGroupId();
        Category category = categoryRepository.findOneByNameAndGroupId(categoryName, groupId);
        if (category == null) {
            category = new Category(categoryName, groupId);
            categoryRepository.save(category);
        }
        return category;
    }

    public Book createBook(String bookName, User user) {
        Book book = new Book(bookName, user);
        bookRepository.save(book);
        ensureBookHasValidGroupId(book);
        readAccessRepository.save(new ReadAccess(book, user));
        writeAccessRepository.save(new WriteAccess(book, user));
        return book;
    }

    /** JPA is unable to auto generate non key columns.
     *  That's why we need to set group id's manually.
     *  They are initially set to unique id's (here), but
     *  they can be changed later in order to form groups. */
    private void ensureBookHasValidGroupId(Book book) {
        if (book.getGroupId() == 0) {
            book.setGroupId(book.getId());
            bookRepository.save(book);
        }
    }

    private void setBookAsLatestForUser(Book book, User user) {
        user.setLatestRead(book);
        userRepository.save(user);
    }

    private String encode(String plaintextPassword) {
        return BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Expense> findSomeRecentExpenses(Book book) {
        return expenseRepository.findFirst10ByBookAndCurrentOrderByTimeAddedDesc(book, true);
    }

    public List<Expense> findAllCurrentExpenses(Book book) {
        return expenseRepository.findByBookAndCurrentOrderByYearDescMonthDesc(book, true);
    }

    public Expense createExpense(int year, int month, Book book, String category, long amountCents, User user) {
        Category lowestSubCategory = detCategory(category, book);
        Expense expense = new Expense(year, month, book, lowestSubCategory, amountCents, user);
        expenseRepository.save(expense);
        return expense;
    }

    /** Called when an expense is modified. */
    @Transactional
    public void updateVersionHistory(Expense current, Expense previous) {
        previous.setCurrent(false);
        previous.setNextVersionId(current.getId());
        expenseRepository.save(previous);
        current.setPreviousVersionId(previous.getId());
        expenseRepository.save(current);
    }

    /** Deleted expenses are kept in the database to provide a full modification history. */
    public void deleteExpense(Expense expense) {
        expense.setCurrent(false);
        expenseRepository.save(expense);
    }

    public List<Category> findCategoriesByGroupId(long groupId) {
        return categoryRepository.findByGroupId(groupId);
    }

    public List<Book> getReadBooksForUser(User user) {
        Set<ReadAccess> readAccessSet = user.getReadAccessSet();
        List<Book> list = new ArrayList<Book>();
        for (ReadAccess r : readAccessSet) {
            list.add(r.getBook());
        }
        Collections.sort(list, ALPHABETICAL_ORDER);
        return list;
    }

    private static Comparator<Book> ALPHABETICAL_ORDER = new Comparator<Book>() {
        public int compare(Book book1, Book book2) {
            return book1.getName().compareTo(book2.getName());
        }
    };

    public void setLatestBook(User user, Book book) {
        user.setLatestRead(book);
        userRepository.save(user);
    }

    public void setLatestInputDate(User user, int year, int month) {
        user.setLatestInputMonth(month);
        user.setLatestInputYear(year);
        userRepository.save(user);
    }

}
