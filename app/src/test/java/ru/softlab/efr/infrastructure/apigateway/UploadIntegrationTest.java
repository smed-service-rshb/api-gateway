package ru.softlab.efr.infrastructure.apigateway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import ru.softlab.efr.infrastructure.apigateway.config.TestApplicationConfiguration;

import java.io.IOException;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestApplicationConfiguration.class)
public class UploadIntegrationTest extends IntegrationTestBase {
    private static final ClassPathResource UPLOAD_RESOURCE = new ClassPathResource("log4j.properties");
    private static final ClassPathResource DOWNLOAD_RESOURCE = new ClassPathResource("test_jasper.pdf");

    @Test
    public void testDownload() throws IOException {
        RestTemplate template = new RestTemplate();
        ResponseEntity<byte[]> result = template.exchange(apiGatewayPath("/super-service/v1/public/download"), HttpMethod.GET, null, byte[].class);
        assertNotNull("Response", result);
        assertThat("Status code", result.getStatusCode(), is(HttpStatus.OK));
        assertThat("ContentType", result.getHeaders().getContentType(), is(MediaType.APPLICATION_PDF));
        assertThat("ContentLength", result.getHeaders().getContentLength(), is(DOWNLOAD_RESOURCE.contentLength()));
        assertThat("ContentDisposition", result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION), is("attachment; filename=test_jasper.pdf"));
        assertThat("Content", result.getBody(), is(StreamUtils.copyToByteArray(DOWNLOAD_RESOURCE.getInputStream())));
    }

    @Test
    public void testSimpleUpload() throws Exception {
        String fieldName = "file";
        MultiValueMap<String, Object> multipartMap = new LinkedMultiValueMap<>();
        multipartMap.add(fieldName, UPLOAD_RESOURCE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<?> request = new HttpEntity<>(multipartMap, headers);

        RestTemplate template = new RestTemplate();
        ResponseEntity<UploadedFileInfo> result = template.exchange(apiGatewayPath("/super-service/v1/public/upload"), HttpMethod.POST, request, UploadedFileInfo.class);
        checkUploadResponse(fieldName, result);
    }

    @Test
    public void testUploadWithJson() throws Exception {
        String uploadFieldName = "file";
        MultiValueMap<String, Object> multipartMap = new LinkedMultiValueMap<>();
        multipartMap.add(uploadFieldName, UPLOAD_RESOURCE);
        String fileInfoFieldName = "fileInfo";
        UploadedFileInfo fileInfo = new UploadedFileInfo();
        fileInfo.setName("some-file.name.pdf");
        multipartMap.add(fileInfoFieldName, fileInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<?> request = new HttpEntity<>(multipartMap, headers);

        RestTemplate template = new RestTemplate();
        ResponseEntity<UploadedFileInfo> result = template.exchange(apiGatewayPath("/super-service/v1/public/upload/with-json"), HttpMethod.POST, request, UploadedFileInfo.class);
        checkUploadResponse(fileInfo.getName(), result);
    }


    private void checkUploadResponse(String fileName, ResponseEntity<UploadedFileInfo> result) throws IOException {
        assertNotNull("Response is not null", result);
        assertThat("Status code is OK", result.getStatusCode(), is(HttpStatus.OK));
        UploadedFileInfo body = result.getBody();
        assertNotNull("Response body is not null", body);
        assertThat(body.getSize(), is(UPLOAD_RESOURCE.contentLength()));
        assertThat(body.getContentType(), is(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        assertThat(body.getName(), is(fileName));
        assertThat(body.getOriginalFilename(), is(UPLOAD_RESOURCE.getFilename()));
    }
}
