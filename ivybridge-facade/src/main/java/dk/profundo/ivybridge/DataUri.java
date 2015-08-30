/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import javax.xml.bind.DatatypeConverter;

/**
 * @author emartino
 *        
 */
public class DataUri {
    private static final String DATA_BINARY_OCTET_STREAM_BASE64 = "data:binary/octet-stream;base64,";
    
    public static String toURI(byte[] data) {
        return DATA_BINARY_OCTET_STREAM_BASE64 +
                DatatypeConverter.printBase64Binary(data);
    }
    
    public static byte[] fromURI(String uri) {
        if (!uri.startsWith(DATA_BINARY_OCTET_STREAM_BASE64)) {
            throw new IllegalArgumentException("uri must start with " + DATA_BINARY_OCTET_STREAM_BASE64);
        }
        String base64data = uri.substring(DATA_BINARY_OCTET_STREAM_BASE64.length());
        return DatatypeConverter.parseBase64Binary(base64data);
    }
}
