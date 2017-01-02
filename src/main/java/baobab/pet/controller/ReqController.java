package baobab.pet.controller;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import baobab.pet.data.domain.*;
import baobab.pet.data.DAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.security.AccessControlException;
import java.security.InvalidParameterException;
import java.security.Principal;

@Controller
public class ReqController {

    @Autowired
    DAO dao;

    @RequestMapping("/")
    public String defaultMapping(Model model, Principal auth) {
        User user = dao.findUserByLoginname(auth.getName());
        Book book = dao.detLatestBookForUser(user);

        model.addAttribute("user", auth.getName());
        model.addAttribute("book", book);
        model.addAttribute("categories", dao.findCategoriesByGroupId(book.getGroupId()));
        model.addAttribute("expenses", dao.findSomeRecentExpenses(book));
        return "index";
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
            Principal auth
    ) {
        User user = dao.findUserByLoginname(auth.getName());
        Book book = dao.findBookById(bookId);
        if (!dao.hasWriteAccess(user, book)) {
            throw new AccessControlException("User does not have write access to this book!");
        }
        long amountCents = getAmountInCents(amountRaw);
        validateInputYear(year);
        validateInputMonth(month);
        if (previousVersion.isEmpty()) {
            /* When adding a new expense. */
            dao.createExpense(year, month, book, category, amountCents, user);
        }
        else {
            /* When modifying an expense. */
            Long prevId = Long.parseLong(previousVersion);
            Expense previous = dao.findExpenseById(prevId);
            if (!previous.isCurrent()) {
                throw new InvalidParameterException("Only current expenses can be modified!");
            }
            if (!dao.hasWriteAccess(user, previous.getBook())) {
                throw new AccessControlException("User does not have write access to this book!");
            }
            Expense current = dao.createExpense(year, month, book, category, amountCents, user);
            dao.updateVersionHistory(current, previous);
        }
        return "redirect:/";
    }

    @DeleteMapping("/deleteExpense")
    public String processRequestToDeleteExpense(@RequestParam long id, Principal auth) {
        User user = dao.findUserByLoginname(auth.getName());
        Expense expense = dao.findExpenseById(id);
        Book book = expense.getBook();
        if (!dao.hasWriteAccess(user, book)) {
            throw new AccessControlException("User does not have write access to this book!");
        }
        dao.deleteExpense(expense);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/logout")
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";//You can redirect wherever you want, but generally it's a good practice to show login screen again.
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
            /* Unusual delimiter. User input very likely contains some mistake. */
            throw new InvalidParameterException("Invalid amount.");
        }
        if (i != amountRaw.length() - 2) {
            /* Unexpected amount of decimals in user input. */
            throw new InvalidParameterException("Invalid amount.");
        }
        for (; i<amountRaw.length(); i++) {
            char c = amountRaw.charAt(i);
            sb.append(c);
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
