package baobab.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import baobab.pet.domain.Book;
import baobab.pet.domain.Expense;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /** Returns latest 10 current expenses from given Book. */
    List<Expense> findFirst10ByBookAndCurrentOrderByTimeAddedDesc(Book book, Boolean current);

    /** Returns all expenses from given book, including non current
     * expenses (deleted or old versions of modified expenses). */
    List<Expense> findByBook(Book book);
}
