package ru.softlab.efr.infrastructure.testapp.someservice.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

/**
 * Тестовый контроллер
 */
@RestController
@RequestMapping("/super-service/v1")
public class SomeController {
    private static final Resource INPUT_STREAM = new ClassPathResource("test_jasper.pdf");

    private static final String PUBLIC_RESOURCE_RESPONSE = "public resource";
    private static final String PRIVATE_RESOURCE_RESPONSE = "private resource";
    private static final String PROTECTED_RESOURCE_RESPONSE = "protected resource";
    private static final String HTTP_CLIENT_ERROR_EXCEPTION = "Http client error exception";
    private static final String HTTP_SERVER_ERROR_EXCEPTION = "Http server error exception";

    /**
     * Публичный эндпоинт
     *
     * @return http-ответ
     */
    @RequestMapping(value = "/public/resource")
    public ResponseEntity<String> publicResource() {
        return ResponseEntity.status(HttpStatus.OK).body(PUBLIC_RESOURCE_RESPONSE);

    }

    /**
     * Приватный эндпоинт
     *
     * @return http-ответ
     */
    @RequestMapping(value = "/private/resource")
    public ResponseEntity<String> privateResource() {
        return ResponseEntity.status(HttpStatus.OK).body(PRIVATE_RESOURCE_RESPONSE);

    }

    /**
     * Защищенный эндпоинт
     *
     * @return http-ответ
     */
    @RequestMapping(value = "/protected/resource")
    public ResponseEntity<String> protectedResource() {
        return ResponseEntity.status(HttpStatus.OK).body(PROTECTED_RESOURCE_RESPONSE);
    }

    /**
     * Эндпоинт загрузки файла
     *
     * @param file файл
     * @return ответ с информацией о загруженном файле
     */
    @PostMapping("public/upload")
    public UploadedFileInfo upload(@RequestParam("file") MultipartFile file) {
        return new UploadedFileInfo(file.getSize(), file.getContentType(), file.getName(), file.getOriginalFilename());
    }

    /**
     * Загрузка файла и передача объекта в 1 запросе
     *
     * @param fileInfo информация о файле
     * @param file   файл
     * @return информация о загруженном файле
     */
    @PostMapping(value = "public/upload/with-json", consumes = {"multipart/form-data"})
    public UploadedFileInfo upload(@RequestPart("fileInfo") UploadedFileInfo fileInfo, @RequestPart("file") MultipartFile file) {
        return new UploadedFileInfo(file.getSize(), file.getContentType(), fileInfo.getName(), file.getOriginalFilename());
    }

    /**
     * Скачивание pdf-файла
     *
     * @return содержимое
     * @throws IOException в случае ошибок
     */
    @GetMapping("public/download")
    public HttpEntity<InputStreamResource> download() throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(INPUT_STREAM.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + INPUT_STREAM.getFilename())
                .body(new InputStreamResource(INPUT_STREAM.getInputStream()));
    }

    /**
     * Эхо-эндпоинт
     *
     * @param message сообщение
     * @return http-ответ, содержащий исходное сообшение
     */
    @GetMapping(value = "/public/echo")
    public ResponseEntity<String> echo(@RequestParam("message") String message) {
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    /**
     * Эндпоинд тестирования нескольких параметров
     *
     * @param first  строковый параметр
     * @param second массив строковых параметров
     * @param third  массив строковых параметров
     * @return http-ответ
     */
    @GetMapping(value = "/public/three-params")
    public ResponseEntity<String> threeParams(@RequestParam("first") String first,
                                              @RequestParam("second") String[] second,
                                              @RequestParam("third") String[] third) {
        return ResponseEntity.status(HttpStatus.OK).body(String.format("%s%s%s",
                first, Arrays.toString(second), Arrays.toString(third)));
    }

    /**
     * Эндпоинт, возвращающий ошибку BAD_REQUEST
     *
     * @return ошибка 400 с контентом в теле
     */
    @RequestMapping(value = "/public/exception/client")
    public ResponseEntity<?> httpClientErrorExceptionController() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HTTP_CLIENT_ERROR_EXCEPTION);
    }

    /**
     * Эндпоинт, возвращающий ошибку
     *
     * @return ошибка 500 с контентом в теле
     */
    @RequestMapping(value = "/public/exception/server")
    public ResponseEntity<?> httpServerErrorExceptionController() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(HTTP_SERVER_ERROR_EXCEPTION);
    }
}
