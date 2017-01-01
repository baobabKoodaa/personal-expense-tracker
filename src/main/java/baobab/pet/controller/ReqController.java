package baobab.pet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import baobab.pet.data.domain.*;
import baobab.pet.data.DAO;

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
        Expense current = dao.createExpense(year, month, book, category, amountCents, user);
        if (!previousVersion.isEmpty()) {
            /* When modifying an expense. */
            Long prevId = Long.parseLong(previousVersion);
            Expense previous = dao.findExpenseById(prevId);
            if (!dao.hasWriteAccess(user, previous.getBook())) {
                throw new AccessControlException("User does not have write access to this book!");
            }
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

    /** amountRaw input examples: 48,32    55    33.00    */
    private long getAmountInCents(String amountRaw) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
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
        i++; // skip delimiter character
        if (i != amountRaw.length() - 2) {
            /* Unexpected amount of decimals in user input. */
            throw new InvalidParameterException("Invalid amount. i = " + i + ", amountRaw = " + amountRaw);
        }
        for (; i<amountRaw.length(); i++) {
            char c = amountRaw.charAt(i);
            sb.append(c);
        }
        return Long.parseLong(sb.toString());
    }
}
