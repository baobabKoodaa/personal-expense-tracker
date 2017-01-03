package baobab.pet.controller;

import baobab.pet.data.DAO;
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
import java.security.Principal;

import static baobab.pet.controller.FlashMessager.flashMessage;

@Controller
public class UserAccessController {

    @Autowired
    DAO dao;

    @Autowired
    PasswordEncoder passwordEncoder;

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
        if (requestor.getRole().equals("ADMIN")) {
            if (target != null) {
                dao.disableUser(target);
                /** TODO: Boot from active sessions. */
                flashMessage("Succesfully disabled user " + targetName, r);
            } else {
                flashMessage("User " + targetName + " not found", r);
            }
        } else {
            flashMessage("Only admins can delete users!", r);
        }
        return "redirect:/profile";
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
