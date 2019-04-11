package io.triada.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Triada http client
 */
public final class HttpTriadaClient {

    /**
     * Default read timeout
     */
    public static final int READ_TIMEOUT = 2_000;

    /**
     * Url to send request
     */
    private final String url;

    /**
     * Ctor
     *
     * @param url url path
     */
    public HttpTriadaClient(final String url) {
        this.url = url;
    }

    /**
     * This method write response stream to given file
     *
     * @param file To write
     * @return File with new content from http call
     * @throws Exception if failed
     */
    public File getFile(final File file) throws Exception {
        final ResponseEntity<byte[]> response = HttpTriadaClient.response(this.url);
        HttpTriadaClient.validate(response);
        final byte[] body = response.getBody();
        FileUtils.write(
                file,
                new String(
                        body,
                        StandardCharsets.UTF_8
                ),
                StandardCharsets.UTF_8,
                false
        );
        return file;
    }

    /**
     * @param timeout Read timeout
     * @return Json response
     * @throws Exception if failed
     */
    public JsonObject get(final int timeout) throws Exception {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        final RestTemplate template = new RestTemplate(factory);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        final HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        final ResponseEntity<String> response = template.exchange(this.url, HttpMethod.GET, entity, String.class, Collections.emptyMap());
        return new JsonParser().parse(response.getBody()).getAsJsonObject();
    }

    /**
     * Call get method with default time out
     *
     * @return JsonObject as response
     * @throws Exception if failed
     */
    public JsonObject get() throws Exception {
        return this.get(READ_TIMEOUT);
    }

    /**
     * @param file with content
     * @return JsonObject response
     * @throws Exception if failed
     */
    public JsonObject putFile(final File file) throws Exception {
        final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        final ResponseEntity<String> response = HttpTriadaClient.response(this.url, content);
        HttpTriadaClient.validate(response);
        return new JsonParser().parse(response.getBody() == null ? "{}" : response.getBody()).getAsJsonObject();
    }


    /**
     * @param url to Send OCTET_STREAM request
     * @return File content as byte array
     */
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

    /**
     * @param url     to Send
     * @param content Plain text to send
     * @return File content as byte array
     */
    private static ResponseEntity<String> response(final String url, final String content) {
        final RestTemplate template = new RestTemplate();
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        final HttpEntity<String> entity = new HttpEntity<>(content, httpHeaders);

        return template.exchange(
                url,
                HttpMethod.PUT,
                entity,
                String.class,
                Collections.emptyMap()
        );
    }

    /**
     * Validate Response code
     *
     * @param response to validate
     */
    private static void validate(final ResponseEntity<?> response) {
        if (response.getStatusCode().isError()) {
            throw new IllegalStateException(
                    String.format(
                            "Http status code error : [%d]",
                            response.getStatusCodeValue()
                    )
            );
        }
    }
}
