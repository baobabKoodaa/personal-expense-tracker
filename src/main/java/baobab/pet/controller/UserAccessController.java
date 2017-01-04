package baobab.pet.controller;

import baobab.pet.data.DAO;
import baobab.pet.data.domain.Book;
import baobab.pet.data.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.security.Principal;

import static baobab.pet.controller.FlashMessenger.flashMessage;

@Controller
public class UserAccessController {

    @Autowired
    DAO dao;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String processRequestToGetProfilePage(
            Model model,
            Principal auth
    ) {
        User user = dao.findUserByName(auth.getName());
        model.addAttribute("books", dao.getBooksForUserWithReadAccess(user, true));
        model.addAttribute("trash", dao.getBooksForUserWithReadAccess(user, false));
        model.addAttribute("user", user);
        model.addAttribute("activeId", "profile");
        model.addAttribute("users", dao.getUsers());
        return "profile";
    }

    @PostMapping("/changePassword")
    public String processRequestToChangePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Model model,
            Principal auth,
            RedirectAttributes r
    ) {
        User user = dao.findUserByName(auth.getName());
        if (!passwordEncoder.matches(oldPassword, user.getEncodedPassword())) {
            flashMessage("Old password does not match the one on record.", r);
        } else {
            flashMessage("Password changed succesfully.", r);
            dao.setPassword(user, newPassword);
        }
        return "redirect:/profile";
    }

    @PostMapping("/newUser")
    public String processRequestToCreateUser(
            @RequestParam String newUserName,
            @RequestParam String newUserPassword,
            Model model,
            Principal auth,
            RedirectAttributes r
    ) {
        User requestor = dao.findUserByName(auth.getName());
        if (!requestor.getRole().equals("ADMIN")) {
            flashMessage("Only admins can create new users!", r);
        } else {
            dao.createUser(newUserName, newUserPassword);
            flashMessage("New user succesfully created.", r);
        }
        return "redirect:/profile";
    }

    @DeleteMapping("/disableUser")
    public String processRequestToDisableUser(
            @RequestParam String targetName,
            Principal auth,
            RedirectAttributes r
    ) {
        User requestor = dao.findUserByName(auth.getName());
        User target = dao.findUserByName(targetName);
        if (!requestor.getRole().equals("ADMIN")) {
            flashMessage("Only admins can delete users!", r);
            return "redirect:/profile";
        }
        if (target == requestor) {
            flashMessage("Admins can delete other admins but not themselves.", r);
            return "redirect:/profile";
        }
        if (target != null) {
            dao.disableUser(target);
            /** TODO: Boot from active sessions. */
            flashMessage("Succesfully deleted user " + targetName, r);
        } else {
            flashMessage("User " + targetName + " not found", r);
        }
        return "redirect:/profile";
    }

    @PostMapping("/enableWriteAccess")
    public String processRequestToEnableWriteAccess(
            @RequestParam String targetName,
            @RequestParam long bookId,
            Principal auth,
            RedirectAttributes r
    ) {
        User requestor = dao.findUserByName(auth.getName());
        User target = dao.findUserByName(targetName);
        Book book = dao.findBookById(bookId);
        if (book.getOwner() != requestor) {
            flashMessage("Only the owner of a book can change access permissions.", r);
            return "redirect:/modifyBook";
        }
        if (target == null) {
            flashMessage("User " + targetName + " not found", r);
            return "redirect:/modifyBook";
        }
        if (!dao.hasWriteAccess(target, book)) {
            dao.enableWriteAccess(book, target);
        }
        if (!dao.hasReadAccess(target, book)) {
            dao.enableReadAccess(book, target);
        }
        flashMessage("Access permissions granted to " + targetName, r);
        return "redirect:/modifyBook";
    }

    @Transactional
    @PostMapping("/disableWriteAccess")
    public String processRequestToDisableWriteAccess(
            @RequestParam String targetName,
            @RequestParam long bookId,
            Principal auth,
            RedirectAttributes r
    ) {
        User requestor = dao.findUserByName(auth.getName());
        User target = dao.findUserByName(targetName);
        Book book = dao.findBookById(bookId);
        if (book.getOwner() != requestor) {
            flashMessage("Only the owner of a book can change access permissions.", r);
        } else if (target == null) {
            flashMessage("User " + targetName + " not found", r);
        } else if (target == requestor) {
            flashMessage("Owner of a book always has access permission.", r);
        } else if (dao.hasWriteAccess(target, book)) {
            dao.disableWriteAccess(book, target);
            dao.disableReadAccess(book, target);
            flashMessage("Access permissions removed from " + targetName, r);
        }
        return "redirect:/modifyBook";
    }

    @PostMapping("/transferOwnership")
    public String processRequestToTransferOwnership(
            @RequestParam String newOwnerName,
            @RequestParam long bookId,
            Principal auth,
            RedirectAttributes r
    ) {
        User requestor = dao.findUserByName(auth.getName());
        Book book = dao.findBookById(bookId);
        User newOwner = dao.findUserByName(newOwnerName);
        if (book.getOwner() != requestor) {
            flashMessage("Only the owner of a book can modify it!", r);
        } else if (newOwner == null) {
            flashMessage("Unable to find user " + newOwnerName, r);
        } else if (newOwner != requestor) {
            dao.setBookOwner(book, newOwner);
            flashMessage("Succesfully changed owner of " + book.getName() + " to " + newOwnerName, r);
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/logout")
    public String logoutPage (
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }
}
