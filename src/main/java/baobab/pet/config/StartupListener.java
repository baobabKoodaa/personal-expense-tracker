package baobab.pet.config;

import baobab.pet.data.DAO;
import baobab.pet.data.domain.Book;
import baobab.pet.data.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** This class exists to perform actions after the server has started. */
@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    DAO dao;

    @Autowired
    Environment environment;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        (new Thread() {
            public void run() {
                String profile = getProfile();
                System.out.println("********************************************");
                System.out.println("********************************************");
                System.out.println("     Active profile: " + profile);
                System.out.println("********************************************");
                System.out.println("********************************************");

                if (profile.equals("development")) {
                    populateDatabase();
                }
            }
        }).start();
    }

    /** Populate database for testing convenience. */
    private void populateDatabase() {
        User atte = dao.createUser("atte", "1");
        dao.setUserAsAdmin(atte);
        Book bookA = dao.createBook("Atte's expenses", atte);
        Book bookC = dao.createBook("Chess Club's expenses", atte);
        dao.createExpense(2018, 1, bookC, "Food", "Week 1 meet-up", 6083, atte);
        dao.createExpense(2018, 1, bookC, "Food", "Week 2 meet-up", 5045, atte);
        dao.createExpense(2018, 1, bookC, "Advertisements/Facebook", "", 10000, atte);
        dao.createExpense(2018, 1, bookC, "Mandatory/Rent", "", 60000, atte);
        dao.createExpense(2018, 1, bookC, "Mandatory/Equipment", "", 3000, atte);

        User mikko = dao.createUser("mikko", "masa");
        Book bookM = dao.createBook("Mikko's expenses", mikko);
        dao.createExpense(2018, 1, bookC, "Food", "Week 3 meet-up", 7020, mikko);
        dao.createExpense(2018, 1, bookC, "Food", "Week 4 meet-up", 5500, mikko);
        dao.createExpense(2018, 2, bookC, "Food", "Week 1 meet-up", 8493, mikko);
        dao.createExpense(2018, 1, bookM, "food", "",67, mikko);
        dao.createExpense(2018, 1, bookM, "food", "", 55, mikko);
        dao.createExpense(2018, 1, bookM, "food", "", 99, mikko);
        dao.createExpense(2018, 1, bookM, "entertainment", "", 10, mikko);
        dao.createExpense(2018, 1, bookM, "entertainment", "", 30, mikko);
        dao.createExpense(2018, 1, bookM, "entertainment", "", 30, mikko);

        User marianne = dao.createUser("marianne", "1");
    }

    private String getProfile() {
        for(String profile : environment.getActiveProfiles()) {
            return profile;
        }
        return "unknown";
    }
}
