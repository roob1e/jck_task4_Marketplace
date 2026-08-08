package by.assxmblxr.marketplace.storage;

import java.io.InputStream;

public interface FileStorage {
  String save(InputStream file);
  void update(InputStream file, String key);
  void delete(String key);
  String getUrl(String key);
}
