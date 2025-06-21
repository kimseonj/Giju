package com.bubble.giju.domain.image.service.impl;

import com.bubble.giju.domain.image.entity.Image;
import com.bubble.giju.domain.image.repository.ImageRepository;
import com.bubble.giju.domain.image.service.S3UploadService;
import com.bubble.giju.util.ImageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @InjectMocks
    private ImageServiceImpl imageService;

    @Mock
    public S3UploadService s3UploadService;

    @Mock
    public ImageRepository imageRepository;

    @Mock
    public ImageUtils imageUtils;

    @Test
    void uploadFile_shouldReturnUrlAndSaveImage() throws IOException {
        // given
        MultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        File mockResizedFile = new File("resized-test-image.jpg");
        String mockUrl = "https://s3.test.bucket/resized-test-image.jpg";


        // ImageUtils의 메서드가 static이기 때문에 mockedStatic을 사용해야함
        try (MockedStatic<ImageUtils> mockedStatic = mockStatic(ImageUtils.class)) {
            mockedStatic.when(() -> ImageUtils.resize(mockMultipartFile, 800, 800)).thenReturn(mockResizedFile);
            when(s3UploadService.upload(mockResizedFile)).thenReturn(mockUrl);

            // when
            String result = imageService.uploadFile(mockMultipartFile);

            // then
            assertEquals(mockUrl, result);
            verify(s3UploadService).upload(mockResizedFile);
            verify(imageRepository).save(any(Image.class));
        }
    }
    @Test
    void uploadFiles_shouldReturnUrlListAndSaveImages() throws IOException {
        // given
        MultipartFile file1 = new MockMultipartFile("file", "img1.jpg", "image/jpeg", "dummy1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file", "img2.jpg", "image/jpeg", "dummy2".getBytes());
        List<MultipartFile> multipartFiles = List.of(file1, file2);

        File resizedFile1 = new File("resized-img1.jpg");
        File resizedFile2 = new File("resized-img2.jpg");
        List<File> resizedFiles = List.of(resizedFile1, resizedFile2);

        List<String> urlList = List.of(
                "https://s3.test.bucket/resized-img1.jpg",
                "https://s3.test.bucket/resized-img2.jpg"
        );

        try (MockedStatic<ImageUtils> mockedStatic = mockStatic(ImageUtils.class)) {
            // mock static method for resizeAll
            mockedStatic.when(() -> ImageUtils.resizeAll(multipartFiles, 800, 800))
                    .thenReturn(resizedFiles);

            // mock s3 upload
            when(s3UploadService.uploadAll(resizedFiles)).thenReturn(urlList);

            // when
            List<String> result = imageService.uploadFiles(multipartFiles);

            // then
            assertEquals(urlList, result);
            verify(s3UploadService).uploadAll(resizedFiles);
            verify(imageRepository, times(urlList.size())).save(any(Image.class));
        }
    }


}