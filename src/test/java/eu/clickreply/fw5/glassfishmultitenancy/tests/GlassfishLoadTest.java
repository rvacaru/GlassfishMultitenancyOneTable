package eu.clickreply.fw5.glassfishmultitenancy.tests;

import eu.clickreply.fw5.glassfishmultitenancy.ContextHolderBean;
import eu.clickreply.fw5.glassfishmultitenancy.FileUtils;
import eu.clickreply.fw5.glassfishmultitenancy.Kid;
import eu.clickreply.fw5.glassfishmultitenancy.CtxKidProcessor;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Class CRUDing a simple entity into a MySQL table using a discriminator column so we can have 2 tenants.
 * The application uses Arquillian to deploy/test the app against GlassFish 4.1
 * @author r.vacaru
 */
public class GlassfishLoadTest extends Arquillian{
    
    @EJB
    CtxKidProcessor kidProcessor;
    
    @EJB
    ContextHolderBean ctxHolder;

    private static final int NO_DATASRC = 2;
    private static final String CURR_TENANT = "currentTenant";
    private static final String[] TENANTS = {"tenant1", "tenant2"};
    private static Duration testDuration, persistDuration, mergeDuration, selectRemoveDuration;

    private static final String fPath = "C:\\Users\\r.vacaru\\Documents\\NetBeansProjects\\GlassfishMultitenancyOneTable\\TestDurations.txt";

    @Deployment
    public static Archive<?> createDeploymentPackage() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackage(Kid.class.getPackage())
                .addAsManifestResource("META-INF/beans.xml", "beans.xml") //to use for CDI beans
                .addAsManifestResource("META-INF/persistence.xml", "persistence.xml");
    }

    @Test(enabled = true)
    public void doTest() {
        FileUtils.write(fPath, "START LOAD TESTS SUITE---------------------------------------------" + LocalDateTime.now() + "\n\n");
        paramTest(5, "_1");
        FileUtils.write(fPath, "END   LOAD TESTS SUITE---------------------------------------------" + LocalDateTime.now() + "\n\n");
    }

    private void paramTest(int scale, String name) {
        FileUtils.write(fPath, "Start of Test" + name + " " + LocalDateTime.now() + "\n");
 
        cleanUpAllDataSources();
        Kid[][] kids = createKidsArray(scale);
        Instant startTest = Instant.now();

        Instant startPersist = Instant.now();
        persistKids(kids, scale);
        Instant endPersist = Instant.now();
        myAssertEqualsNoOrder(kids);

        Instant startMerge = Instant.now();
        mergeKids(kids, scale);
        Instant endMerge = Instant.now();
        myAssertEqualsNoOrder(kids);

        Instant startSelect = Instant.now();
        selectAndRemoveAllKids();
        Instant endSelect = Instant.now();
        myAssertDBAreEmpty();

        Instant endTest = Instant.now();
        testDuration = Duration.between(startTest, endTest);
        persistDuration = Duration.between(startPersist, endPersist);
        mergeDuration = Duration.between(startMerge, endMerge);
        selectRemoveDuration = Duration.between(startSelect, endSelect);

        FileUtils.write(fPath, String.format("Duration of PERSIST T%s:   [ %d minutes, %d secs, %d millis ]\n", name,
                persistDuration.toMinutes(), persistDuration.getSeconds(), persistDuration.toMillis()));
        FileUtils.write(fPath, String.format("Duration of MERGE T%s:     [ %d minutes, %d secs, %d millis ]\n", name,
                mergeDuration.toMinutes(), mergeDuration.getSeconds(), mergeDuration.toMillis()));
        FileUtils.write(fPath, String.format("Duration of SEL_DEL T%s:   [ %d minutes, %d secs, %d millis ]\n", name,
                selectRemoveDuration.toMinutes(), selectRemoveDuration.getSeconds(), selectRemoveDuration.toMillis()));
        FileUtils.write(fPath, String.format("Duration of TOTAL T%s:     [ %d minutes, %d secs, %d millis ]\n", name,
                testDuration.toMinutes(), testDuration.getSeconds(), testDuration.toMillis()));
        FileUtils.write(fPath, String.format("End of Test%s, Scale: %d, %s  \n\n", name, scale, LocalDateTime.now()));
    }

    private Kid[][] createKidsArray(int scale) {
        Kid[][] kids = new Kid[NO_DATASRC][scale];
        for (int j = 0; j < NO_DATASRC; j++) {
            for (int i = 0; i < scale; i++) {
                kids[j][i] = new Kid("persistKidName_" + j + i, Integer.valueOf("" + j + i));
            }
        }
        return kids;
    }

    private void persistKids(Kid[][] kids, int scale) {
        for (int j = 0; j < NO_DATASRC; j++) {
            ctxHolder.putOrReplace(CURR_TENANT, TENANTS[j]);
            for (int i = 0; i < scale; i++) {
                kidProcessor.perist(kids[j][i]);
            }
        }
    }

    private void mergeKids(Kid[][] kids, int scale) {
        String mergedName = "mergedName_";
        for (int j = 0; j < NO_DATASRC; j++) {
            ctxHolder.putOrReplace(CURR_TENANT, TENANTS[j]);
            for (int i = 0; i < scale; i++) {
                kids[j][i].setName(mergedName + j + i);
                kidProcessor.merge(kids[j][i]);
            }
        }
    }

    private void selectAndRemoveAllKids() {
        for (String t : TENANTS) {
            ctxHolder.putOrReplace(CURR_TENANT, t);
            List<Kid> kids = (List<Kid>) kidProcessor.selectAll();
            kids.forEach(p -> kidProcessor.remove(p)); //lamda expr! =P
        }
    }

    private void myAssertEqualsNoOrder(Kid[][] kids) {
        for (int j = 0; j < NO_DATASRC; j++) {
            ctxHolder.putOrReplace(CURR_TENANT, TENANTS[j]);
            assertEqualsNoOrder(kids[j], kidProcessor.selectAll().toArray());
        }
    }

    private void myAssertDBAreEmpty() {
        for (String t : TENANTS) {
            ctxHolder.putOrReplace(CURR_TENANT, t);
            assertTrue(kidProcessor.selectAll().isEmpty());
        }
    }

    /**
     * XXX @Before* methods are executed twice: both in and out of the
     * container. See:
     * http://stackoverflow.com/questions/6817674/testng-injection-fails-when-using-any-before-annotation-but-works-without
     * https://issues.jboss.org/browse/ARQ-104
     */
    
    @BeforeClass
    private void cleanUpAllDataSources() {
        if (kidProcessor != null) { //without this u get a NPE when it's executed out of the container
            for (String t : TENANTS) {
                ctxHolder.putOrReplace(CURR_TENANT, t);
                kidProcessor.removeAll();
            }
            System.out.println("@BeforeTest -> CleanUp Performed =)");
        }
    }
}
