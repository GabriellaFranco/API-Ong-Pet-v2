package com.enterprise.ong_pet2.config.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        var credentials = AwsBasicCredentials.create(
                s3Properties.getAccessKey(),
                s3Properties.getSecretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(s3Properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(s3Properties.getRegion()))
                .forcePathStyle(true) // obrigatório para MinIO
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var credentials = AwsBasicCredentials.create(
                s3Properties.getAccessKey(),
                s3Properties.getSecretKey()
        );

        return S3Presigner.builder()
                .endpointOverride(URI.create(s3Properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(s3Properties.getRegion()))
                .build();
    }
}