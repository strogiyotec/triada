package io.triada.http;

import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public final class HttpClient {

    private final String url;

    public HttpClient(final String url) {
        this.url = url;
    }

    public File getFile(final File file) throws Exception {
        final ResponseEntity<byte[]> response = HttpClient.response(this.url);

        if (response.getStatusCode().isError()) {
            throw new IllegalStateException(
                    String.format(
                            "Http status code error : [%d]",
                            response.getStatusCodeValue()
                    )
            );
        }

        final byte[] body = response.getBody();

        FileUtils.write(file, new String(body, StandardCharsets.UTF_8), StandardCharsets.UTF_8, false);

        return file;
    }


    private static ResponseEntity<byte[]> response(final String url) {
        final RestTemplate template = new RestTemplate();
        template.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        final HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

        return template.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class,
                Collections.emptyMap()
        );
    }
}
