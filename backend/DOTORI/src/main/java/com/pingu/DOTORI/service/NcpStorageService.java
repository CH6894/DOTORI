package com.pingu.DOTORI.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@ConditionalOnProperty(name = "ncp.storage.enabled", havingValue = "true", matchIfMissing = false)
public class NcpStorageService {
    private final AmazonS3 s3Client;
    private final String bucketName;

    public NcpStorageService(
            @Value("${ncp.storage.endpoint}") String endpoint,
            @Value("${ncp.storage.region}") String region,
            @Value("${ncp.storage.access-key}") String accessKey,
            @Value("${ncp.storage.secret-key}") String secretKey,
            @Value("${ncp.storage.bucket}") String bucketName
    ) {
        this.bucketName = bucketName;
        
        // 디버깅용 로그
        System.out.println("=== NCP Storage 설정 ===");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Region: " + region);
        System.out.println("Access Key: " + accessKey.substring(0, Math.min(10, accessKey.length())) + "...");
        System.out.println("Secret Key: " + secretKey.substring(0, Math.min(10, secretKey.length())) + "...");
        System.out.println("Bucket: " + bucketName);
        System.out.println("========================");
        
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public String uploadFile(MultipartFile file, String folder) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String objectKey = folder + "/" + filename;
        
        System.out.println("=== NCP 업로드 시작 ===");
        System.out.println("파일명: " + file.getOriginalFilename());
        System.out.println("파일 크기: " + file.getSize());
        System.out.println("폴더: " + folder);
        System.out.println("생성된 파일명: " + filename);
        System.out.println("Object Key: " + objectKey);
        System.out.println("버킷: " + bucketName);
        
        try {
            // 버킷 존재 여부 확인
            if (!s3Client.doesBucketExist(bucketName)) {
                System.out.println("버킷이 존재하지 않습니다: " + bucketName);
                throw new RuntimeException("버킷이 존재하지 않습니다: " + bucketName);
            }
            System.out.println("버킷 존재 확인 완료: " + bucketName);
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // NCP Object Storage 업로드
            System.out.println("업로드 시작...");
            s3Client.putObject(bucketName, objectKey, file.getInputStream(), metadata);
            System.out.println("업로드 완료!");

            // 업로드 후 접근 가능한 URL 반환
            String url = String.format("https://%s.kr.object.ncloudstorage.com/%s",
                    bucketName, objectKey);
            System.out.println("생성된 URL: " + url);
            System.out.println("=== NCP 업로드 완료 ===");
            return url;
        } catch (Exception e) {
            System.out.println("=== NCP 업로드 실패 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("NCP 업로드 실패: " + e.getMessage(), e);
        }
    }
}
