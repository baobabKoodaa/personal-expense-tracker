package baobab.pet.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

/** This is a temporary class to provide rudimentary logging for a few weeks
 *  just in case something goes horribly wrong with the database. TODO: Real logger. */
@Component
public class TrainingWheels {

    @Autowired
    Environment environment;

    PrintWriter writer;

    boolean production;

    @PostConstruct
    public void init() throws IOException {
        for(String profile : environment.getActiveProfiles()) {
            if (profile.equals("production")) {
                production = true;
            }
        }
        if (writer == null && production) {
            String target = System.getenv("PETLOGPATH");
            if (!target.endsWith(".txt")) {
                System.out.println("Environment variable PETLOGPATH not defined correctly!");
                return;
            }
            FileWriter fw = null;
            try {
                fw = new FileWriter(target, true);
                BufferedWriter bw = new BufferedWriter(fw);
                writer = new PrintWriter(bw);
            } catch (Exception e) {
                System.out.println("Exception when trying to wire up PrintWriter! " + e.toString());
                fw.close();
            }
        }
    }

    public void log(String s) {
        if (production) {
            try {
                writer.println(s);
                writer.flush();
            } catch (Exception e) {
                System.out.println("Exception when writing to log file! " + e.toString());
                writer.close();
            }
        }
    }
}
