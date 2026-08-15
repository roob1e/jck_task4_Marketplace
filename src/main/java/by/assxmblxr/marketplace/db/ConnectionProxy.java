package by.assxmblxr.marketplace.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * {@link InvocationHandler} used by {@link ConnectionPool#getConnection()} to build a
 * dynamic proxy over a real {@link Connection}.
 * <p>
 * All calls are forwarded transparently to the wrapped connection, except
 * {@link Connection#close()}: instead of physically closing the connection, it is
 * returned to the owning {@link ConnectionPool} for reuse.
 */
public class ConnectionProxy implements InvocationHandler {
  private final Connection real;
  private final ConnectionPool pool;

  /**
   * Creates a proxy handler for a pooled connection.
   *
   * @param real the real, unwrapped connection to delegate calls to
   * @param pool the pool that {@code real} was borrowed from, and should be returned to on close
   */
  public ConnectionProxy(Connection real, ConnectionPool pool) {
    this.real = real;
    this.pool = pool;
  }

  /**
   * Intercepts every method call on the proxied {@link Connection}.
   * {@code close()} is redirected to {@link ConnectionPool#releaseConnection(Connection)};
   * every other method is delegated to the real connection unchanged.
   *
   * @param proxy  the proxy instance the method was invoked on
   * @param method the {@link Connection} method being invoked
   * @param args   the arguments passed to the method
   * @return the result of the delegated call, or {@code null} for an intercepted {@code close()}
   * @throws Throwable the exception thrown by the delegated call, if any
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if ("close".equals(method.getName())) {
      pool.releaseConnection(real);
      return null;
    }
    try {
      return method.invoke(real, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }
}