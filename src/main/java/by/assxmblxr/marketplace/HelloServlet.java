package by.assxmblxr.marketplace;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

import by.assxmblxr.marketplace.db.ConnectionPool;
import by.assxmblxr.marketplace.storage.impl.MinioFileStorage;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
  private String message;

  public void init() {
    message = "Hello World!";
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");

    // Hello
    PrintWriter out = response.getWriter();
    out.println("<html><body>");
    out.println("<h1>" + message + "</h1>");
    out.println("</body></html>");

    // db smoke test
    try (Connection conn = ConnectionPool.getInstance().getConnection()) {
      System.out.println(conn.isValid(2));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    // image storage smoke test
    MinioFileStorage storage = new MinioFileStorage();
    InputStream in = new ByteArrayInputStream("hello minio".getBytes());
    String key = storage.save(in);
    System.out.println("MinIO object key: " + key);
    System.out.println(storage.getUrl(key));
  }

  public void destroy() {
  }
}