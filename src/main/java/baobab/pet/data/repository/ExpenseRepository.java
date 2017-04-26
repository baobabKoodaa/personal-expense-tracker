package baobab.pet.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.data.domain.Book;
import baobab.pet.data.domain.Expense;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /** Returns latest 5 current expenses from given Book, ordered by time added. */
    List<Expense> findFirst5ByBookAndCurrentOrderByTimeAddedDesc(Book book, Boolean current);

    /** Returns all current expenses from given Book, ordered by receipt date. */
    List<Expense> findByBookAndCurrentOrderByYearDescMonthDescTimeAddedDesc(Book book, Boolean current);

    /** Returns all expenses from given book, including non current
     * expenses (deleted or old versions of modified expenses). */
    List<Expense> findByBook(Book book);

    /** Counts number of current expenses in the book. */
    long countByBookAndCurrent(Book book, Boolean current);
}
