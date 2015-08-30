/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import java.util.Arrays;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author emartino
 *        
 */
public class DataUriTest {
    
    @Test
    public void testEmpty() {
        byte[] data = new byte[0];
        validate(data);
    }
    
    @Test
    public void testSingle() {
        byte[] data = new byte[1];
        for (int i = 0; i < 256; i++) {
            data[0] = (byte) i;
            validate(data);
        }
    }
    
    @Test
    public void testZero() {
        byte[] data = new byte[7];
        validate(data);
    }
    
    protected void validate(byte[] data) {
        byte[] roundtrip = DataUri.fromURI(DataUri.toURI(data));
        assertTrue(Arrays.equals(data, roundtrip));
    }
    
}
