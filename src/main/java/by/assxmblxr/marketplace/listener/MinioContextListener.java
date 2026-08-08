package by.assxmblxr.marketplace.listener;

import by.assxmblxr.marketplace.exception.FileStorageException;
import by.assxmblxr.marketplace.storage.MinioClientProvider;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class MinioContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    MinioClientProvider minio = MinioClientProvider.getInstance();
    MinioClient client = minio.getClient();
    var args = BucketExistsArgs.builder().bucket(minio.getBucketName()).build();
    try {
      if (!client.bucketExists(args)) {
        MakeBucketArgs makeArgs = MakeBucketArgs.builder().bucket(minio.getBucketName()).build();
        client.makeBucket(makeArgs);
      }

      String policyJson = """
              {
                "Version": "2012-10-17",
                "Statement": [
                  {
                    "Effect": "Allow",
                    "Principal": "*",
                    "Action": "s3:GetObject",
                    "Resource": "arn:aws:s3:::%s/*"
                  }
                ]
              }
              """;

      SetBucketPolicyArgs policy = SetBucketPolicyArgs.builder()
              .bucket(minio.getBucketName())
              .config(policyJson.formatted(minio.getBucketName()))
              .build();
      client.setBucketPolicy(policy);
    } catch (Exception e) {
      throw new FileStorageException(e);
    }
  }
}