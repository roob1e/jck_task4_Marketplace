package by.assxmblxr.marketplace.storage;

import by.assxmblxr.marketplace.exception.FileStorageException;
import io.minio.MinioClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class MinioClientProvider {
  private static final MinioClientProvider INSTANCE = new MinioClientProvider();

  private final MinioClient client;
  private final String bucketName;
  private final String publicUrl;

  private MinioClientProvider() {
    String accessKey = System.getenv("MINIO_ACCESS_KEY");
    String secretKey = System.getenv("MINIO_SECRET_KEY");
    String endpoint = System.getenv("MINIO_ENDPOINT");
    String publicUrl = System.getenv("MINIO_PUBLIC_URL");
    if (accessKey == null || secretKey == null || endpoint == null || publicUrl == null) {
      throw new FileStorageException("MINIO_ACCESS_KEY, MINIO_SECRET_KEY, MINIO_ENDPOINT, MINIO_PUBLIC_URL required");
    }

    client = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();

    bucketName = loadBucketName();
    this.publicUrl = publicUrl;
  }

  public static MinioClientProvider getInstance() {
    return INSTANCE;
  }

  public MinioClient getClient() {
    return client;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getPublicUrl() {
    return publicUrl;
  }

  private String loadBucketName() {
    Properties properties = new Properties();
    try (InputStream in = MinioClientProvider.class
            .getClassLoader().getResourceAsStream("minio.properties")) {
      if (in == null) {
        throw new FileStorageException("minio.properties not found on classpath");
      }
      properties.load(in);
    } catch (IOException e) {
      throw new FileStorageException(e.getMessage(), e);
    }

    String name = properties.getProperty("bucket.name");
    if (name == null) {
      throw new FileStorageException("bucket.name missing from minio.properties");
    }
    return name;
  }
}
