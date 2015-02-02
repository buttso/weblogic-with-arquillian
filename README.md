# weblogic-with-arquillian

*Author: Steve Button*  

*Date: Feb 2015*

This project contains a simple Maven project that demonstrates using Arquillian with the WebLogic Container Adapters to deploy and run unit tests. It shows the bare-bones of what needs to be added and what a test case looks like.

http://arquillian.org/blog/tags/wls/

**As of January 2015** the WebLogic Server artefacts and plugin can be retrieved from the     Oracle Maven Repository - see [http://maven.oracle.com](http://maven/.oracle.com)



## Configuring Arquillian in the POM

    <project>
      ...
      <dependencyManagement>
        <dependencies>
          <dependency>
            <groupId>org.jboss.arquillian</groupId>
            <artifactId>arquillian-bom</artifactId>
            <version>1.1.5.Final</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
        </dependencies>
      </dependencyManagement>
    
      <dependencies>
      ...
        <!-- ###################################### -->
        <!-- #### Arquillian Test  Dependencies ### -->
        <!-- ###################################### -->
        <dependency>
          <groupId>org.jboss.arquillian.junit</groupId>
          <artifactId>arquillian-junit-container</artifactId>
          <scope>test</scope>
        </dependency>        
        <dependency>
          <groupId>org.jboss.arquillian.container</groupId>
          <artifactId>arquillian-wls-remote-12.1.2</artifactId>
          <version>1.0.0.Alpha3</version>
          <scope>test</scope>
        </dependency>      
      </dependencies>
      
      ...
    </project>

The container to use for testing is specified through the dependencies that are included.  In this case the arquillian-wls-remote-12.1.2 adapter is used to deployment and testing of a remote WebLogic Server instance.

    <dependency>
      <groupId>org.jboss.arquillian.container</groupId>
      <artifactId>arquillian-wls-remote-12.1.2</artifactId>
      <version>1.0.0.Alpha3</version>
      <scope>test</scope>
    </dependency>      
    
To define where the server is running and how to connect to it, another test resource file **arquillian.xml** is used to define settings for the container adapters to use.  The details of the target WebLogic Server instance are shown below.  

For the **arquillian-wls-remote-12.1.2 adapter**, being a remote adapter that is managed independently of the test executions, the server must be running before the testing takes place. The remote server can be also local to the environment where the tests are being run.

The **arquillian-wls-managed-12.1.2 adapter** can also be used to control the lifecyle of a local WebLogic Server instance, starting it as part of the test executiona nd stopping it when the tests are complete.


    <?xml version="1.0"?>
    <arquillian
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns="http://jboss.org/schema/arquillian"
      xsi:schemaLocation="http://jboss.org/schema/arquillian
                http://jboss.org/schema/arquillian/arquillian_1_0.xsd">
      <!--
      <engine>
        <property name="deploymentExportPath">target/</property>
      </engine>
      -->
    
      <container qualifier="weblogic" default="true">
        <defaultProtocol type="Servlet 3.0" />    
        <configuration>
          <property name="wlHome">/Users/sbutton/Oracle/Middleware/wlserver</property>
          <property name="adminUrl">t3://localhost:7001</property>
          <property name="adminUserName">weblogic</property>
          <property name="adminPassword">welcome1</property>
          <property name="target">AdminServer</property>
        </configuration>
      </container>
    </arquillian>

  
## Creating a Unit Test

In the Arquillian world a unit test can programatically construct a deployment archive using the **ShrinkWrap** API, which is then deployed and used to execute the test

For testing web/html applications a client library such as **HtmlUnit** can be used to open pages, parse and present information, interact with forms and click buttons.

This project has a very simple unit test that verifies the static index.html page has been deployed, that it matches the known page line count and the page title is correct.

Arquillian will handle packaging the archive, deploying it to the configured container, invoking the test methods and returning the results.

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


In the test case a feature of Arquillian is being used that injects a reference to the URL of the deployed application that the test case can use to access the web application content within the archive.  This is really helpful since the archive is dynamically constructed with a generated module name and theregore has a potentially different context-root on each execution.  The ``@ArquillianResource`` annotation directs Arquillian to inject the URL for the deployed application for the unit tests to access and test web applications.

Alternatively, you can specifically name the deployment unit using `@Deployment(name="wlarq")` or you could create a container specific deployment descriptor in which the context-root is specifed, such as weblogic.xml, and include that in the deployment archive that is created.

    <weblogic-web-app>
      <context-root>/wlsarq</context-root>
    </weblogic-web-app>

## Executing the Unit Test

The unit tests are executed from the maven test goal and the results reported upon completion.

    [sbutton] wlsarq $ mvn test
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building wlsarq 1.0
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ wlsarq ---
    [INFO] Using 'UTF-8' encoding to copy filtered resources.
    [INFO] Copying 0 resource
    [INFO] 
    [INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ wlsarq ---
    [INFO] Nothing to compile - all classes are up to date
    [INFO] 
    [INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ wlsarq ---
    [INFO] Using 'UTF-8' encoding to copy filtered resources.
    [INFO] Copying 1 resource
    [INFO] Copying 1 resource
    [INFO] 
    [INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ wlsarq ---
    [INFO] Nothing to compile - all classes are up to date
    [INFO] 
    [INFO] --- maven-surefire-plugin:2.18:test (default-test) @ wlsarq ---
    [INFO] Surefire report directory: /Users/sbutton/Projects/Java/weblogic-with-arquillian/wlsarq/target/surefire-reports

    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running buttso.demo.weblogic.wlsarq.IndexPageTest
    Feb 02, 2015 11:34:08 PM buttso.demo.weblogic.wlsarq.IndexPageTest test_index_page_present
    INFO: Testing deployment @ http://192.168.0.13:7001/f6a033f4-07a9-462b-8e0b-2946dd19d577/
    Feb 02, 2015 11:34:09 PM buttso.demo.weblogic.wlsarq.IndexPageTest test_index_title
    INFO: Testing deployment @ http://192.168.0.13:7001/f6a033f4-07a9-462b-8e0b-2946dd19d577/
    Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.201 sec - in buttso.demo.weblogic.wlsarq.IndexPageTest
    Running buttso.demo.weblogic.wlsarq.PingPongBeanTest
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.975 sec - in     buttso.demo.weblogic.wlsarq.PingPongBeanTest

    Results :    

    Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 6.772 s
    [INFO] Finished at: 2015-02-02T23:34:11+10:30
    [INFO] Final Memory: 12M/331M
    [INFO] ------------------------------------------------------------------------












