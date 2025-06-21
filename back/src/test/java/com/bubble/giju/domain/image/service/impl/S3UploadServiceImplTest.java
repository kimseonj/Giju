package com.bubble.giju.domain.image.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3UploadServiceImplTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3UploadServiceImpl s3UploadService;

    @BeforeEach
    void setUp() {
        // bucket 필드는 @Value로 주입되므로 리플렉션으로 수동 주입
        try {
            var field = S3UploadServiceImpl.class.getDeclaredField("bucket");
            field.setAccessible(true);
            field.set(s3UploadService, "test-bucket");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void upload_shouldUploadFileAndReturnUrl() throws IOException {
        // given
        File tempFile = File.createTempFile("test-image", ".jpg");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("dummy data".getBytes());
        }

        String expectedUrl = "https://s3.test-bucket/test-image.jpg";

        when(amazonS3.getUrl("test-bucket", tempFile.getName()))
                .thenReturn(new URL(expectedUrl));

        // when
        String result = s3UploadService.upload(tempFile);

        // then
        assertEquals(expectedUrl, result);
        verify(amazonS3).putObject(eq("test-bucket"), eq(tempFile.getName()), any(), any(ObjectMetadata.class));
        verify(amazonS3).getUrl("test-bucket", tempFile.getName());

        // cleanup
        tempFile.delete();
    }

    @Test
    void uploadAll_shouldUploadMultipleFiles() throws IOException {
        // given
        File file1 = File.createTempFile("file1", ".jpg");
        File file2 = File.createTempFile("file2", ".jpg");
        try (FileOutputStream fos1 = new FileOutputStream(file1);
             FileOutputStream fos2 = new FileOutputStream(file2)) {
            fos1.write("dummy1".getBytes());
            fos2.write("dummy2".getBytes());
        }

        when(amazonS3.getUrl("test-bucket", file1.getName()))
                .thenReturn(new URL("https://s3.test-bucket/" + file1.getName()));
        when(amazonS3.getUrl("test-bucket", file2.getName()))
                .thenReturn(new URL("https://s3.test-bucket/" + file2.getName()));

        // when
        List<String> result = s3UploadService.uploadAll(List.of(file1, file2));

        // then
        assertEquals(2, result.size());
        verify(amazonS3, times(2)).putObject(any(), any(), any(), any());
        verify(amazonS3, times(2)).getUrl(any(), any());

        // cleanup
        file1.delete();
        file2.delete();
    }
}
