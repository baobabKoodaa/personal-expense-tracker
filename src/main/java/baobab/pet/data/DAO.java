package baobab.pet.data;

import baobab.pet.data.domain.*;
import baobab.pet.data.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    PasswordEncoder passwordEncoder;

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

    public void setUserAsAdmin(User user) {
        user.setRole("ADMIN");
        userRepository.save(user);
    }

    /** Returns most recently used book or any non-deleted book. */
    @Transactional
    public Book getLatestBookForUser(User user) {
        Book latest = user.getLatestRead();
        if (latest == null) {
            /** Assign any accessible book as latest. */
            for (ReadAccess r : user.getReadAccessSet()) {
                Book b = r.getBook();
                if (!b.isCurrent()) {
                    continue;
                }
                latest = b;
                setBookAsLatestForUser(latest, user);
                break;
            }
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
        enableReadAccess(book, user);
        enableWriteAccess(book, user);
        setBookAsLatestForUser(book, user);
        return book;
    }

    public void enableReadAccess(Book book, User user) {
        readAccessRepository.save(new ReadAccess(book, user));
    }

    public void disableReadAccess(Book book, User user) {
        if (user.getLatestRead() == book) {
            user.setLatestRead(null);
        }
        userRepository.save(user);
        readAccessRepository.deleteByBookAndUser(book, user);
    }

    public void enableWriteAccess(Book book, User user) {
        writeAccessRepository.save(new WriteAccess(book, user));
    }

    public void disableWriteAccess(Book book, User user) {
        if (user.getLatestRead() == book) {
            user.setLatestRead(null);
        }
        userRepository.save(user);
        writeAccessRepository.deleteByBookAndUser(book, user);
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

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Expense> findSomeRecentExpenses(Book book) {
        return expenseRepository.findFirst10ByBookAndCurrentOrderByTimeAddedDesc(book, true);
    }

    public long getBookSize(Book book) {
        return expenseRepository.countByBook(book);
    }

    public List<Expense> findAllCurrentExpenses(Book book) {
        return expenseRepository.findByBookAndCurrentOrderByYearDescMonthDesc(book, true);
    }

    public Expense createExpense(int year, int month, Book book, String categoryName, long amountCents, User user) {
        Category category = detCategory(categoryName, book);
        Expense expense = new Expense(year, month, book, category, amountCents, user);
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

    /** Param current should be set true for active books, false when listing trashed books. */
    public List<Book> getBooksForUserWithReadAccess(User user, boolean current) {
        Set<ReadAccess> readAccessSet = user.getReadAccessSet();
        List<Book> list = new ArrayList<Book>();
        for (ReadAccess r : readAccessSet) {
            Book b = r.getBook();
            if (b.isCurrent() == current) {
                list.add(r.getBook());
            }
        }
        Collections.sort(list, BOOK_COMPARATOR);
        return list;
    }

    /** Compares books by id (effectively time created).
     *  Deterministic behavior is needed for navigation bar. */
    private static Comparator<Book> BOOK_COMPARATOR = new Comparator<Book>() {
        public int compare(Book book1, Book book2) {
            return Long.compare(book1.getId(), book2.getId());
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

    public void setFlagShowAllExpensesTo(User user, boolean bool) {
        user.setRequestingToViewAllExpenses(bool);
        userRepository.save(user);
    }

    public void setPassword(User user, String clearTextPassword) {
        user.setEncodedPassword(encode(clearTextPassword));
        userRepository.save(user);
    }

    public String encode(String plaintextPassword) {
        return BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
    }

    public List<User> getUsers() {
        return userRepository.findByCurrentOrderByIdAsc(true);
    }

    public void disableUser(User user) {
        user.setCurrent(false);
        userRepository.save(user);
    }

    public void enableBook(Book book) {
        book.setCurrent(true);
        bookRepository.save(book);
    }

    public void disableBook(Book book) {
        book.setCurrent(false);
        for (ReadAccess r : book.getReadAccessSet()) {
            User u = r.getUser();
            if (u.getLatestRead() == book) {
                u.setLatestRead(null);
            }
        }
        bookRepository.save(book);
    }

    public void setBookOwner(Book book, User owner) {
        book.setOwner(owner);
        if (!hasWriteAccess(owner, book)) {
            enableWriteAccess(book, owner);
        }
        if (!hasReadAccess(owner, book)) {
            enableReadAccess(book, owner);
        }
        bookRepository.save(book);
    }

    public void setBookName(Book book, String bookName) {
        book.setName(bookName);
        bookRepository.save(book);
    }
}
