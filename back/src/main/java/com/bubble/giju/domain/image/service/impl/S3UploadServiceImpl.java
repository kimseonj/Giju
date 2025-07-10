package com.bubble.giju.domain.image.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.bubble.giju.domain.image.service.S3UploadService;
import com.bubble.giju.util.ImageUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class S3UploadServiceImpl implements S3UploadService {

    private final AmazonS3 amazonS3;
    @Value("${aws.s3.bucket}")
    private String bucket;

    /*
    * AWS의 S3를 이용하여 파일 하나를 저장하는 메서드
    * */
    @Override
    public String upload(File file) throws IOException {
        String fileName = file.getName(); // UUID + 확장자 조합 추천
        String contentType = ImageUtils.getFileExtension(fileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.length());
        metadata.setContentType(contentType);

        try (FileInputStream inputStream = new FileInputStream(file)) {
            amazonS3.putObject(bucket, fileName, inputStream, metadata);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /*
     * AWS의 S3를 이용하여 파일 여러개를 저장하는 메서드
     * */
    @Override
    public List<String> uploadAll(List<File> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (File file : files) {
            urls.add(upload(file));
        }
        return urls;
    }


}