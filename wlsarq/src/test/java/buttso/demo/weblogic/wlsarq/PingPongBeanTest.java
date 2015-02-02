package buttso.demo.weblogic.wlsarq;

import com.bea.core.repackaged.springframework.util.Assert;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * Simple test case to verify behavior of EJB component
 * Uses @Inject to inject bean to test
 * 
 * @author sbutton
 */
@RunWith(Arquillian.class)
public class PingPongBeanTest {

    @Inject
    PingPongBean ppb;
       
    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(PingPongBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return jar;
    }

    @Test
    public void test_ping_pong_bean() {
        Assert.notNull(ppb, "PingPongBean was not injected, is null");
        Assert.isTrue("pong".equals(ppb.ping()), "The ping didn't pong");
    }

}
