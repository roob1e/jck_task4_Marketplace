package by.assxmblxr.marketplace.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

public class ConnectionProxy implements InvocationHandler {
  private final Connection real;
  private final ConnectionPool pool;

  public ConnectionProxy(Connection real, ConnectionPool pool) {
    this.real = real;
    this.pool = pool;
  }

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