package buttso.demo.weblogic.wlsarq;

import javax.ejb.Stateless;

/**
 * Super simple EJB to verify it can be injected and called
 * from a test client
 * 
 * @author sbutton
 */
@Stateless
public class PingPongBean {
    
    public String ping() { 
        return "pong";
    }
    
}
