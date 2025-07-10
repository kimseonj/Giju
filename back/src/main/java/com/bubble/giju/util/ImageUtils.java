package com.bubble.giju.util;

import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    /**
     * MultipartFile을 받아 지정된 크기로 리사이징 후 File로 반환
     */
    public static File resize(MultipartFile originalFile, int targetWidth, int targetHeight) throws IOException {
        // 파일 확장자 추출
        String format = getFileExtension(originalFile.getOriginalFilename());

        if (format == null || format.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        // BufferedImage 변환
        BufferedImage originalImage = convertToBufferedImage(originalFile);

        // 이미지 리사이징
        BufferedImage resizedImage = resizeImage(originalImage, targetWidth, targetHeight);

        // 임시 파일로 저장
        File outputFile = File.createTempFile("resized_", "." + format);
        ImageIO.write(resizedImage, format, outputFile);

        return outputFile;
    }

    /**
     * 여러 MultipartFile을 처리하여 리사이징된 파일 리스트 반환
     */
    public static List<File> resizeAll(List<MultipartFile> files, int targetWidth, int targetHeight) throws IOException {
        List<File> resizedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                File resized = resize(file, targetWidth, targetHeight);
                resizedFiles.add(resized);
            } else {
                throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
            }
        }

        return resizedFiles;
    }

    /**
     * MultipartFile을 BufferedImage로 변환
     */
    private static BufferedImage convertToBufferedImage(MultipartFile multipartFile) throws IOException {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
            }
            return image;
        }
    }

    /**
     * 이미지 리사이징
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * 파일 확장자 추출
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) return null;

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return null;
    }
}
