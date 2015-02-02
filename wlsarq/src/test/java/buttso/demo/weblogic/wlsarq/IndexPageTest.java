package buttso.demo.weblogic.wlsarq;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.net.URL;
import java.util.logging.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test case to perform simple verifications of the 
 * index.html that is included as part of the @Deployment
 * 
 * @author sbutton
 */
@RunWith(Arquillian.class)
public class IndexPageTest {

    private static final Logger LOG = Logger.getLogger(IndexPageTest.class.getName());
    private static final int INDEX_PAGE_LINE_COUNT = 15;
    private static final String INDEX_PAGE_TITLE = "wlsarq";

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addAsWebResource("index.html");
        return war;
    }

    @Test
    @RunAsClient
    public void test_index_page_present(@ArquillianResource URL testURL) {
        
        LOG.info("Testing deployment @ " + testURL);
        
        WebClient webClient = null;
        try {
            webClient = new WebClient();
            HtmlPage page = webClient.getPage(testURL);
            Assert.assertTrue("Line count doesn't match", INDEX_PAGE_LINE_COUNT == page.getEndLineNumber());
        } catch (Exception e) {
            // Swallow me whole
        } finally {
            webClient.closeAllWindows();
        }
    }

    @Test
    @RunAsClient
    public void test_index_title(@ArquillianResource URL testURL) {
        
        LOG.info("Testing deployment @ " + testURL);
        
        WebClient webClient = null;

        try {
            webClient = new WebClient();
            final HtmlPage page = webClient.getPage(testURL);
            Assert.assertEquals(INDEX_PAGE_TITLE, page.getTitleText());
        } catch (Exception e) {
            // Swallowed, doh!
        } finally {
            webClient.closeAllWindows();
        }
    }
}
