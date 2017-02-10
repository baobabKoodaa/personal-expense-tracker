package baobab.pet.controller;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import baobab.pet.data.domain.*;
import baobab.pet.data.DAO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import java.security.AccessControlException;
import java.security.InvalidParameterException;
import java.security.Principal;
import java.util.List;

import static baobab.pet.controller.FlashMessenger.flashMessage;

@Controller
public class BookController {

    @Autowired
    DAO dao;

    @RequestMapping("/")
    public String processRequestToGetMainPage(
            Model model,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        model.addAttribute("user", user);
        List<Book> books = dao.getBooksForUserWithReadAccess(user, true);
        model.addAttribute("books", books);
        Book activeBook = dao.getLatestBookForUser(user);
        if (activeBook == null) {
            return "welcome";
        }
        model.addAttribute("yearNow", DateTime.now().getYear());
        model.addAttribute("monthNow", DateTime.now().getMonthOfYear());
        model.addAttribute("activeId", activeBook.getId());
        model.addAttribute("activeBookName", activeBook.getName());
        model.addAttribute("expenseCount", dao.getBookSize(activeBook));
        model.addAttribute("categories", dao.findVisibleCategoriesByGroupId(activeBook.getGroupId()));
        if (activeBook.getOwner() == user) {
            model.addAttribute("showModifyBookButton", true);
        }
        if (user.isRequestingToViewAllExpenses()) {
            dao.setFlagShowAllExpensesTo(user, false);
            model.addAttribute("allExpensesAreListed", true);
            model.addAttribute("expenses", dao.findAllCurrentExpenses(activeBook));
        } else {
            model.addAttribute("expenses", dao.findSomeRecentExpenses(activeBook));
        }
        return "index";
    }

    @RequestMapping("/viewAllExpenses")
    public String processRequestToViewAllExpenses(
            Principal auth
    ) {
        User user = dao.findUserByName(auth.getName());
        dao.setFlagShowAllExpensesTo(user, true);
        return "redirect:/";
    }

    /** Adding or modifying an expense. */
    @Transactional
    @PostMapping("/postExpense")
    public String processRequestToPostExpense(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam long bookId,
            @RequestParam String category,
            @RequestParam String amountRaw,
            @RequestParam String previousVersion,
            @RequestParam String details,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        if (!dao.hasWriteAccess(user, book)) {
            flashMessage("You do not have access to book " + book.getName(), r);
            return "redirect:/";
        }
        try {
            postExpense(amountRaw, year, month, user, previousVersion, book, category, details);
        } catch (Exception ex) {
            flashMessage(ex.getMessage(), r);
            return "redirect:/";
        }
        flashMessage("Expense recorded succesfully.", r);
        return "redirect:/";
    }

    void postExpense(String amountRaw, int year, int month, User user, String previousVersion, Book book, String category, String details) {
        long amountCents = getAmountInCents(amountRaw);
        validateInputYear(year);
        validateInputMonth(month);
        dao.setLatestInputDate(user, year, month);
        if (previousVersion.isEmpty()) {
            /* When adding a new expense. */
            dao.createExpense(year, month, book, category, details, amountCents, user);
        } else {
            /* When modifying an expense. */
            Long prevId = Long.parseLong(previousVersion);
            Expense previous = dao.findExpenseById(prevId);
            if (!previous.isCurrent()) {
                throw new InvalidParameterException("Only current expenses can be modified!");
            }
            if (!dao.hasWriteAccess(user, previous.getBook())) {
                throw new AccessControlException("User does not have write access to this book!");
            }
            Expense current = dao.createExpense(year, month, book, category, details, amountCents, user);
            dao.updateVersionHistory(current, previous);
        }
    }

    @DeleteMapping("/deleteExpense")
    public String processRequestToDeleteExpense(
            @RequestParam long id,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        Expense expense = dao.findExpenseById(id);
        Book book = expense.getBook();
        if (dao.hasWriteAccess(user, book)) {
            dao.deleteExpense(expense);
            flashMessage("Expense succesfully deleted.", r);
        } else {
            flashMessage("You do not have write access to this book!", r);
        }
        return "redirect:/";
    }

    @GetMapping("/select/{id}")
    public String processRequestToSelectBook(
            @PathVariable long id,
            Principal auth
    ) {
        User user = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(id);
        if (dao.hasReadAccess(user, book)) {
            dao.setLatestBook(user, book);
        }
        return "redirect:/";
    }

    @GetMapping("/newBook")
    public String processRequestToGetNewBookPage(
            Model model,
            Principal auth
    ) {
        User user = dao.findUserByName(auth.getName());
        List<Book> books = dao.getBooksForUserWithReadAccess(user, true);
        model.addAttribute("books", books);
        model.addAttribute("user", user);
        model.addAttribute("activeId", "new");
        return "new_book";
    }

    @PostMapping("/newBook")
    public String processRequestToPostNewBook(
            @RequestParam String bookName,
            Principal auth,
            RedirectAttributes r) {
        User user = dao.findUserByName(auth.getName());
        dao.createBook(bookName, user);
        flashMessage("Book created succesfully.", r);
        return "redirect:/";
    }

    @GetMapping("/modifyBook")
    public String processRequestToGetModifyBookPage(
            Model model,
            Principal auth
    ) {
        User user = dao.findUserByName(auth.getName());
        List<Book> books = dao.getBooksForUserWithReadAccess(user, true);
        Book activeBook = dao.getLatestBookForUser(user);
        List<Category> categories = dao.findAllCategoriesByGroupId(activeBook.getGroupId());
        model.addAttribute("activeId", activeBook.getId()); // needed for navbar
        model.addAttribute("activeBook", activeBook);
        model.addAttribute("books", books);
        model.addAttribute("user", user);
        model.addAttribute("categories", categories);
        return "modify_book";
    }

    @Transactional
    @PostMapping("/modifyBook")
    public String processRequestToModifyBook(
            @RequestParam long bookId,
            @RequestParam String bookName,
            Principal auth,
            RedirectAttributes r
    ) {
        User requestor = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        if (book.getOwner() != requestor) {
            flashMessage("Only the owner of a book can modify it!", r);
        } else if (!book.getName().equals(bookName)) {
            dao.setBookName(book, bookName);
            flashMessage("Name change succesful.", r);
        }
        return "redirect:/modifyBook";
    }

    @PostMapping("/addCategory")
    public String processRequestToAddCategory(
            @RequestParam long bookId,
            @RequestParam String categoryName,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        if (!dao.hasWriteAccess(user, book)) {
            flashMessage("You don't have write access to this book!", r);
        } else {
            Category category = dao.detCategory(categoryName, book);
            flashMessage("Succesfully added category " + categoryName, r);
        }
        return "redirect:/modifyBook";
    }

    @PostMapping("/hideCategoryFromDropbox")
    public String processRequestToHideCategoryFromDropbox(
            @RequestParam long bookId,
            @RequestParam long categoryId,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        Category category = dao.findCategoryById(categoryId);
        if (book.getGroupId() != category.getGroupId()) {
            flashMessage("Category does not belong to said book!", r);
        } else if (!dao.hasWriteAccess(user, book)) {
            flashMessage("You do not have write access to this book!", r);
        } else {
            dao.hideCategory(category);
            flashMessage("Succesfully hidden category " + category.getName(), r);
        }
        return "redirect:/modifyBook";
    }

    @PostMapping("/showCategoryInDropbox")
    public String processRequestToShowCategoryInDropbox(
            @RequestParam long bookId,
            @RequestParam long categoryId,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        Category category = dao.findCategoryById(categoryId);
        if (book.getGroupId() != category.getGroupId()) {
            flashMessage("Category does not belong to said book!", r);
        } else if (!dao.hasWriteAccess(user, book)) {
            flashMessage("You do not have write access to this book!", r);
        } else {
            dao.unhideCategory(category);
            flashMessage("Succesfully brought back category " + category.getName(), r);
        }
        return "redirect:/modifyBook";
    }

    @DeleteMapping("/deleteBook")
    public String processRequestToDeleteBook(
            @RequestParam long bookId,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        if (dao.hasWriteAccess(user, book)) {
            dao.disableBook(book);
            flashMessage("Deleted book " + book.getName() + ". You can still restore it from the profile page.", r);
        } else {
            flashMessage("You don't have write access to " + book.getName(), r);
        }
        return "redirect:/";
    }

    @PostMapping("/restoreBook")
    public String processRequestToRestoreBook(
            @RequestParam long bookId,
            Principal auth,
            RedirectAttributes r) {
        User user = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        if (dao.hasWriteAccess(user, book)) {
            dao.enableBook(book);
            flashMessage("Restored book " + book.getName(), r);
        } else {
            flashMessage("You don't have write access to book " + book.getName(), r);
        }
        return "redirect:/profile";
    }

    /** Transforms an amount from String to long with some error checking.
     *  Examples:
     *  48,32 -> 4832
     *  55    -> 5500
     *  33.00 -> 3300  */
    private long getAmountInCents(String amountRaw) {
        if (amountRaw.isEmpty()) {
            throw new InvalidParameterException("Invalid amount.");
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        if (amountRaw.charAt(0) == '-') {
            /* Negative amounts allowed. */
            sb.append('-');
            i++;
        }
        for (; i<amountRaw.length(); i++) {
            /* Read until delimiter character or end of string. */
            char c = amountRaw.charAt(i);
            if (c >= '0' && c <= '9') sb.append(c);
            else break;
        }
        if (i == amountRaw.length()) {
            /* User input represents whole amount with 00 decimals. */
            sb.append("00");
            return Long.parseLong(sb.toString());
        }
        char delimiter = amountRaw.charAt(i++);
        if (delimiter != '.' && delimiter != ',') {
            /* Unusual delimiter. User input likely contains some mistake. */
            throw new InvalidParameterException("Invalid amount.");
        }
        int numberOfDecimals = amountRaw.length() - i;
        if (numberOfDecimals != 2 && numberOfDecimals != 1) {
            /* Expected either 1 or 2 decimals after seeing delimiter. */
            throw new InvalidParameterException("Invalid amount.");
        }
        for (; i<amountRaw.length(); i++) {
            char c = amountRaw.charAt(i);
            sb.append(c);
        }
        if (numberOfDecimals == 1) {
            /* User input was like 5.1, so we pad the missing 0. */
            sb.append('0');
        }
        try {
            return Long.parseLong(sb.toString());
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid amount.");
        }
    }

    private void validateInputYear(int year) {
        int nextYear = 1 + DateTime.now().getYear();
        if (year < 1 || year > nextYear) {
            throw new InvalidParameterException("Invalid year.");
        }
    }

    private void validateInputMonth(int month) {
        if (month < 1 || month > 12) {
            throw new InvalidParameterException("Invalid month.");
        }
    }
}
