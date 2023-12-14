package com.demo.core.serialize;

import com.demo.core.loader.DynamicClassloader;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ClassLoaderJsonSerializer extends JsonSerializer<DynamicClassloader> {
    @Override
    public void serialize(DynamicClassloader cl, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (cl != null) {
            String name = cl.getName();
            gen.writeString(name);
        }
    }
}
