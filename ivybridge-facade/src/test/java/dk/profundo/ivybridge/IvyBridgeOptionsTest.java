package dk.profundo.ivybridge;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author emartino
 */
public class IvyBridgeOptionsTest {
    
    public IvyBridgeOptionsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testParseOptions() throws Exception {
        final IvyBridgeOptions opts = IvyBridgeOptions
                .newOptionsFromUri("http://ivy.repo.com/reporoot?branch=foo&confscope.test.unit=test");
        assertEquals("foo", opts.getBranch());
        assertEquals("http://ivy.repo.com/reporoot", opts.getIvyroot());
        assertEquals("http://ivy.repo.com/reporoot", opts.getArtroot());
        assertEquals("test", opts.getConfscope().get("test.unit"));
    }
}
