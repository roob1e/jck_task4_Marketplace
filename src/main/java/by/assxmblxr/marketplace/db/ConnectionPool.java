package by.assxmblxr.marketplace.db;

import by.assxmblxr.marketplace.exception.ConnectionPoolException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public final class ConnectionPool {
  private static final ConnectionPool INSTANCE = new ConnectionPool();

  private final BlockingQueue<Connection> pool;
  private final String url;
  private final String user;
  private final String password;

  private ConnectionPool() {
    url = System.getenv("DB_URL");
    user = System.getenv("DB_USER");
    password = System.getenv("DB_PASSWORD");
    if (url == null || user == null || password == null) {
      throw new ConnectionPoolException("DB_URL, DB_USER, DB_PASSWORD required");
    }

    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new ConnectionPoolException(e.getMessage(), e);
    }

    pool = new ArrayBlockingQueue<>(10);
    for (int i = 0; i < 10; i++) {
      try {
        var connection = DriverManager.getConnection(url, user, password);
        pool.add(connection);
      } catch (SQLException e) {
        throw new ConnectionPoolException(e.getMessage(), e);
      }
    }
  }

  public static ConnectionPool getInstance() {
    return INSTANCE;
  }

  public Connection getConnection() {
    try {
      Connection real = pool.take();
      return (Connection) Proxy.newProxyInstance(
          Connection.class.getClassLoader(),
          new Class<?>[] {Connection.class},
          new ConnectionProxy(real, this));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ConnectionPoolException(e.getMessage(), e);
    }
  }

  void releaseConnection(Connection real) {
    Connection toReturn = real;
    try {
      if (!real.isValid(2)) {
        log.warn("Pooled connection is no longer valid, replacing it");
        toReturn = DriverManager.getConnection(url, user, password);
      }
    } catch (SQLException e) {
      throw new ConnectionPoolException(e.getMessage(), e);
    }
    try {
      pool.put(toReturn);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ConnectionPoolException(e.getMessage(), e);
    }
  }
}