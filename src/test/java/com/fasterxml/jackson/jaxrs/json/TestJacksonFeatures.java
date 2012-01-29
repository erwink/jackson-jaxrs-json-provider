package com.fasterxml.jackson.jaxrs.json;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationConfig;

import com.fasterxml.jackson.jaxrs.json.annotation.JacksonFeatures;

/**
 * Tests for [Issue-2], Addition of {@link JacksonFeatures}.
 */
public class TestJacksonFeatures extends JaxrsTestBase
{
    @JacksonFeatures(serializationEnable={ SerializationConfig.Feature.WRAP_ROOT_VALUE })
    public void writeConfig() { }

        
    @JacksonFeatures(deserializationDisable={ DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES })
    public void readConfig() { }

    static class Bean {
        public int a = 3;
    }
    
    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    // [Issue-2], serialization
    public void testWriteConfigs() throws Exception
    {
        JacksonJsonProvider prov = new JacksonJsonProvider();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bean bean = new Bean();
        Method m = getClass().getDeclaredMethod("writeConfig");
        JacksonFeatures feats = m.getAnnotation(JacksonFeatures.class);
        assertNotNull(feats); // just a sanity check

        // when wrapping enabled, we get:
        prov.writeTo(bean, bean.getClass(), bean.getClass(), new Annotation[] { feats },
                MediaType.APPLICATION_JSON_TYPE, null, out);
        assertEquals("{\"Bean\":{\"a\":3}}", out.toString("UTF-8"));

        // but without, not:
        out.reset();
        prov.writeTo(bean, bean.getClass(), bean.getClass(), new Annotation[] { },
                MediaType.APPLICATION_JSON_TYPE, null, out);
        assertEquals("{\"a\":3}", out.toString("UTF-8"));
    }

    // [Issue-2], deserialization
    public void testReadConfigs() throws Exception
    {
        JacksonJsonProvider prov = new JacksonJsonProvider();
        Method m = getClass().getDeclaredMethod("readConfig");
        JacksonFeatures feats = m.getAnnotation(JacksonFeatures.class);
        assertNotNull(feats); // just a sanity check

        // ok: here let's verify that we can disable exception throwing unrecognized things
        @SuppressWarnings("unchecked")
        Class<Object> raw = (Class<Object>)(Class<?>)Bean.class;
        Object ob = prov.readFrom(raw, raw,
                new Annotation[] { feats },
                MediaType.APPLICATION_JSON_TYPE, null,
                new ByteArrayInputStream("{ \"foobar\" : 3 }".getBytes("UTF-8")));
        assertNotNull(ob);

        // but without setting, get the exception
        try {
            prov.readFrom(raw, raw,
                    new Annotation[] { },
                    MediaType.APPLICATION_JSON_TYPE, null,
                    new ByteArrayInputStream("{ \"foobar\" : 3 }".getBytes("UTF-8")));
            fail("Should have caught an exception");
        } catch (JsonMappingException e) {
            verifyException(e, "Unrecognized field");
        }
    }
    
}
