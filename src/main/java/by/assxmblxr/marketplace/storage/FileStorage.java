package by.assxmblxr.marketplace.storage;

import java.io.InputStream;

public interface FileStorage {
  String save(InputStream file);
  boolean update(InputStream file, String key);
  boolean delete(String key);
  String getUrl(String key);
}
