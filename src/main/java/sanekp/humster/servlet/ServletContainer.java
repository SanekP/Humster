package sanekp.humster.servlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sanek_000 on 7/27/2014.
 */
public class ServletContainer {
    private Map<String, Object> context = new ConcurrentHashMap<>();
    private Map<String, HttpServlet> map = new ConcurrentHashMap<>();

    public Map<String, Object> getContext() {
        return context;
    }

    public void load(String name) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(name);
        load((Class<? extends HttpServlet>) clazz);
    }

    public void load(Class<? extends HttpServlet> clazz) {
        try {
            HttpServlet servlet = clazz.newInstance();
            servlet.init(this);
            WebServlet annotation = clazz.getAnnotation(WebServlet.class);
            for (String value : annotation.value()) {
                map.put(value, servlet);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return map of URLs and servlets witch it handles
     */
    public Map<String, HttpServlet> getMap() {
        return map;
    }
}
