package baobab.pet.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class FlashMessager {
    public static void flashMessage(String content, RedirectAttributes container) {
        container.addFlashAttribute("flashMessage", content);
    }
}
