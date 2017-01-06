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
        Book bookC = dao.createBook("Common expenses", atte);
        dao.createExpense(2016, 10, bookA, "food/fast food", 1083, atte);
        dao.createExpense(2016, 11, bookA, "food/high end", 2045, atte);
        dao.createExpense(2016, 12, bookA, "food/fast food", 830, atte);
        dao.createExpense(2016, 12, bookA, "food/fast food", 666, atte);
        dao.createExpense(2016, 12, bookA, "entertainment", 3000, atte);

        User mikko = dao.createUser("mikko", "masa");
        Book bookM = dao.createBook("Mikko's expenses", mikko);
        dao.createExpense(2016, 10, bookM, "food", 23, mikko);
        dao.createExpense(2016, 11, bookM, "food", 67, mikko);
        dao.createExpense(2016, 12, bookM, "food", 55, mikko);
        dao.createExpense(2016, 12, bookM, "food", 99, mikko);
        dao.createExpense(2016, 12, bookM, "entertainment", 10, mikko);
        dao.createExpense(2016, 12, bookM, "entertainment", 30, mikko);
        dao.createExpense(2016, 12, bookM, "entertainment", 30, mikko);

        User nomad = dao.createUser("marianne", "1");
    }

    private String getProfile() {
        for(String profile : environment.getActiveProfiles()) {
            return profile;
        }
        return "unknown";
    }
}
