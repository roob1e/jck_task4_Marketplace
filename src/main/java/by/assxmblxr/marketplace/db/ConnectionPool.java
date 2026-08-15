package by.assxmblxr.marketplace.db;

import by.assxmblxr.marketplace.exception.ConnectionPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Thread-safe singleton pool of pooled JDBC {@link Connection}s to the application database.
 * <p>
 * Configuration ({@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD}) is read once from
 * environment variables when the singleton is first initialized. The pool is pre-filled
 * with a fixed number of physical connections at construction time.
 * <p>
 * Connections handed out by {@link #getConnection()} are wrapped in a dynamic proxy
 * ({@link ConnectionProxy}) so that calling {@link Connection#close()} on them returns
 * the underlying connection to the pool instead of physically closing it.
 */
public final class ConnectionPool {
  private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);
  private static final ConnectionPool INSTANCE = new ConnectionPool();
  private static final int CONNECTION_POOL_SIZE = 10;

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

    pool = new ArrayBlockingQueue<>(CONNECTION_POOL_SIZE);
    for (int i = 0; i < CONNECTION_POOL_SIZE; i++) {
      try {
        var connection = DriverManager.getConnection(url, user, password);
        pool.add(connection);
      } catch (SQLException e) {
        throw new ConnectionPoolException(e.getMessage(), e);
      }
    }
  }

  /**
   * Returns the singleton instance of the pool.
   *
   * @return the shared {@link ConnectionPool} instance
   */
  public static ConnectionPool getInstance() {
    return INSTANCE;
  }

  /**
   * Borrows a connection from the pool, blocking until one becomes available.
   * <p>
   * The returned {@link Connection} is a proxy: calling {@link Connection#close()} on it
   * returns the underlying connection to the pool instead of closing the physical connection.
   *
   * @return a pooled, ready-to-use connection
   */
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

  /**
   * Returns a connection borrowed via {@link #getConnection()} back to the pool.
   * Called internally by {@link ConnectionProxy} when the proxy's {@code close()} is invoked.
   * <p>
   * If the connection is no longer valid, it is transparently replaced with a fresh one
   * so the pool never shrinks below its configured size.
   *
   * @param real the real (unwrapped) connection being released
   */
  void releaseConnection(Connection real) {
    Connection toReturn = real;
    try {
      if (!real.isValid(2)) {
        log.warn("Pooled connection is no longer valid, replacing it");
        toReturn = DriverManager.getConnection(url, user, password);
      }
    } catch (SQLException e) {
      try {
        pool.put(DriverManager.getConnection(url, user, password));
      } catch (SQLException err) {
        throw new ConnectionPoolException(err.getMessage(), err);
      } catch (InterruptedException err) {
        Thread.currentThread().interrupt();
        throw new ConnectionPoolException(err.getMessage(), err);
      }
      log.warn("Could not release connection, created a new one", e);
      return;
    }
    try {
      pool.put(toReturn);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ConnectionPoolException(e.getMessage(), e);
    }
  }

  /**
   * Closes every physical connection currently held by the pool.
   * Intended to be called once, on application shutdown (e.g. from a
   * {@code ServletContextListener#contextDestroyed}). A failure to close one
   * connection is logged and does not prevent the rest from being closed.
   */
  public void shutdown() {
    List<Connection> connections = new ArrayList<>();
    pool.drainTo(connections);
    int closed = 0;
    for (Connection connection : connections) {
      try {
        connection.close();
        closed++;
      } catch (SQLException e) {
        log.error(e.getMessage(), e);
      }
    }
    log.info("Closed {} connections", closed);
  }
}