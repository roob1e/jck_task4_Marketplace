package by.assxmblxr.marketplace.storage.impl;

import by.assxmblxr.marketplace.exception.FileStorageException;
import by.assxmblxr.marketplace.storage.FileStorage;
import by.assxmblxr.marketplace.storage.MinioClientProvider;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import java.io.InputStream;
import java.util.UUID;

public class MinioFileStorage implements FileStorage {
  private final MinioClientProvider minio;

  public MinioFileStorage() {
    minio = MinioClientProvider.getInstance();
  }

  @Override
  public String save(InputStream file) {
    String key = UUID.randomUUID().toString();
    PutObjectArgs args = PutObjectArgs.builder()
            .bucket(minio.getBucketName())
            .object(key)
            .stream(file, -1, 5 * 1024 * 1024)
            .build();
    MinioClient client = minio.getClient();
    try {
      client.putObject(args);
      return key;
    } catch (Exception e) {
      throw new FileStorageException(e.getMessage(), e);
    }
  }

  @Override
  public void update(InputStream file, String key) {
    PutObjectArgs args = PutObjectArgs.builder()
            .bucket(minio.getBucketName())
            .object(key)
            .stream(file, -1, 5 * 1024 * 1024)
            .build();
    MinioClient client = minio.getClient();
    try {
      client.putObject(args);
    } catch (Exception e) {
      throw new FileStorageException(e.getMessage(), e);
    }
  }

  @Override
  public void delete(String key) {
    RemoveObjectArgs args = RemoveObjectArgs.builder()
            .bucket(minio.getBucketName())
            .object(key)
            .build();
    MinioClient client = minio.getClient();
    try {
      client.removeObject(args);
    } catch (Exception e) {
      throw new FileStorageException(e.getMessage(), e);
    }
  }

  @Override
  public String getUrl(String key) {
    return minio.getPublicUrl() + "/" + minio.getBucketName() + "/" + key;
  }
}