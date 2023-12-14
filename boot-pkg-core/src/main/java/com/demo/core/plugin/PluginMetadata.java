package com.demo.core.plugin;

import com.demo.core.loader.DynamicClassloader;
import com.demo.core.serialize.ClassLoaderJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PluginMetadata {
    private String name;
    private URL url;
    // @JsonIgnore
    @JsonSerialize(using = ClassLoaderJsonSerializer.class)
    private DynamicClassloader classLoader;
}
