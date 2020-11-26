package whork;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class App {
	private final static Logger LOGGER = Logger.getLogger("Whork");

    public static void main( String[] args ) throws Exception {
    	LOGGER.info("Welcome to Whork server!");
    	LOGGER.info("starting up...");
    	
    	Tomcat tomcat = new Tomcat();
    	tomcat.setPort(8080);
    	
        Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());
    	
    	tomcat.addServlet(ctx, "whork", new HttpServlet() {
    		@Override
    		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    				throws ServletException, IOException {
    			 Writer w = resp.getWriter();
                 w.write("<html><head><title>Whork</title></head><body><h1>WHORK</h1><br/>trova lavoro</body></html>");
                 w.flush();
                 w.close();
    		}
		});
    	
    	ctx.addServletMapping("/*", "whork");
    	
    	tomcat.start();
		tomcat.getServer().await();
		
    	LOGGER.info("exiting...");
    	LOGGER.info("bye bye");
    }
}
