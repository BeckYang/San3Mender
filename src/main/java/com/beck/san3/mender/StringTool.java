package com.beck.san3.mender;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class StringTool {
    private static final StringTool self = new StringTool();
    private static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private final Properties nameCharMap = new Properties();
    private final Properties customCharMap = new Properties();
    private URL cacheResource;
    private long loadTime;

    private String txName(final String ...namePart) {
        final StringBuilder sb = new StringBuilder();
        for (String s : namePart) {
            if (!"0000".equals(s)) {
                String c = nameCharMap.getProperty(s);
                if (c == null) {
                    c = (String) customCharMap.getOrDefault(s, s);
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public Properties getNameCharMap() {
        return nameCharMap;
    }
    
    private void _load(final String nameMappingResource) throws Exception {
        final URL resource = getClass().getResource(nameMappingResource);
        long resourceTime = getTimestamp(resource);
        if (resource.equals(cacheResource) && resourceTime <= loadTime) {
            return;
        }
        try (final InputStream stream = resource.openStream()) {
            nameCharMap.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            cacheResource = resource;
            loadTime = resourceTime;
        }
    }

    private static long getTimestamp(final URL resource) {
        if ("file".equals(resource.getProtocol())) {
            return new File(resource.getFile()).lastModified();
        }//TODO jar
        return 0;
    }

    public static StringTool load(final String nameMappingResource) throws Exception {
        self._load(nameMappingResource);
        return self;
    }

    public static String translate(final String ...namePart) {
        return self.txName(namePart);
    }

    public static String encodeHex(byte ...data) {
        int len = data.length;
        char[] out = new char[len << 1];
        int i = 0;
        for(int var5 = 0; i < len; ++i) {
            out[var5++] = HEX_CHAR[(240 & data[i]) >>> 4];
            out[var5++] = HEX_CHAR[15 & data[i]];
        }
        return new String(out);
    }

    public static void updateCustomCharMap(String tx) throws Exception {
        final Properties properties = new Properties();
        properties.load(new StringReader(tx));
        properties.replaceAll((k, v) -> {
            final String s = (String)v;
            return s.length() > 1 ? s.substring(0,  1) : s;
        });
        self.customCharMap.clear();
        self.customCharMap.putAll(properties);
    }

}
