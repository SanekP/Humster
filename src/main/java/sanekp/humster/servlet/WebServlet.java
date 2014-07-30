package sanekp.humster.servlet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sanek_000 on 7/27/2014.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WebServlet {
    /**
     * @return URLs to handle
     */
    String[] value();
}
