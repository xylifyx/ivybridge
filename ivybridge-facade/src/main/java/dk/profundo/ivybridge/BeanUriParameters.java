/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * @author emartino
 *        
 */
public class BeanUriParameters {
    
    private final Object bean;
    private final BeanInfo beanInfo;
    
    public BeanUriParameters(Object bean) throws IntrospectionException {
        this.bean = bean;
        this.beanInfo = Introspector.getBeanInfo(bean.getClass());
    }
    
    public void fromParameters(Map<String, String> map)
            throws IllegalArgumentException {
        for (Entry<String, String> e : map.entrySet()) {
            try {
                String name = e.getKey();
                String stringValue = e.getValue();
                
                if (name.contains(".")) {
                    String prefix = name.substring(0, name.indexOf('.'));
                    String suffix = name.substring(name.indexOf('.') + 1);
                    
                    PropertyDescriptor pd = getPropertyDescriptor(prefix);
                    if (pd == null) {
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> submap = (Map<String, Object>) pd.getReadMethod().invoke(
                            bean, new Object[0]);
                    if (submap == null) {
                        submap = new TreeMap<>();
                        pd.getWriteMethod().invoke(bean, submap);
                    }
                    Object value = parse(stringValue, String.class);
                    submap.put(suffix, value);
                    // fromParameters(submap,suffix,stringValue);
                }
                else {
                    fromParameters(name, stringValue);
                }
            }
            catch (RuntimeException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "option parsing", ex);
            }
        }
    }
    
    private void fromParameters(String name, String stringValue)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        PropertyDescriptor pd = getPropertyDescriptor(name);
        if (pd == null) {
            return;
        }
        Object value = parse(stringValue, pd.getPropertyType());
        
        if (value == null) {
            return;
        }
        
        Method writeMethod = pd.getWriteMethod();
        if (writeMethod == null) {
            return;
        }
        writeMethod.invoke(bean, value);
    }
    
    private Object parse(String stringValue, Class<?> pt) {
        Object value;
        if (pt == String.class) {
            value = stringValue;
        }
        else if (pt == Integer.class || pt == Integer.TYPE) {
            value = Integer.parseInt(stringValue);
        }
        else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unsupported type: {0}", pt);
            return null;
        }
        return value;
    }
    
    private PropertyDescriptor getPropertyDescriptor(String name) {
        PropertyDescriptor[] propertyDescriptors = beanInfo
                .getPropertyDescriptors();
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getName().equals(name)) {
                return pd;
            }
        }
        return null;
    }
    
    public Map<String, String> toParameters() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        PropertyDescriptor[] propertyDescriptors = beanInfo
                .getPropertyDescriptors();
        Map<String, String> parameters = new TreeMap<>();
        for (PropertyDescriptor pd : propertyDescriptors) {
            String name = pd.getName();
            
            Class<?> type = pd.getPropertyType();
            Method readMethod = pd.getReadMethod();
            Method writeMethod = pd.getWriteMethod();
            if (readMethod == null || writeMethod == null) {
                continue;
            }
            Object value = readMethod.invoke(bean, new Object[0]);
            if (value != null) {
                toParameters(parameters, name, type, value);
            }
        }
        return parameters;
    }
    
    protected void toParameters(Map<String, String> parameters, String name,
            Class<?> type, Object value) {
        if (type == String.class || type == Integer.class
                || type == Integer.TYPE) {
            parameters.put(name, String.valueOf(value));
        }
        else if (Map.class.isAssignableFrom(type)) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String subname = name + "." + e.getKey();
                Object subvalue = e.getValue();
                if (subvalue != null) {
                    Class<?> subtype = subvalue.getClass();
                    toParameters(parameters, subname, subtype, subvalue);
                }
            }
        }
    }
    
    public final static String ROOT = "__ROOT__";
    
    public static Map<String, String> parseUri(String uriString) throws URISyntaxException {
        URI uri = new URI(uriString);
        String rawQuery = uri.getRawQuery();
        Map<String, String> parameters = parseParameters(rawQuery);
        @SuppressWarnings("unused")
        URI root = new URI(uri.getScheme(),
                uri.getUserInfo(), uri.getHost(), uri.getPort(),
                uri.getPath(), null, null);
                
        // parameters.put(ROOT, root.toASCIIString());
        return parameters;
    }
    
    static Map<String, String> parseParameters(String rawQuery) {
        final List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(rawQuery, Charset.forName("UTF-8"));
        Map<String, String> parameters = new TreeMap<>();
        for (NameValuePair p : nameValuePairs) {
            parameters.put(p.getName(), p.getValue());
        }
        return parameters;
    }
    
}
