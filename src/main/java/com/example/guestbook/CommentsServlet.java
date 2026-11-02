
package com.example.guestbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(CommentsServlet.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // GET /comments - отримання списку відгуків
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        List<Comment> comments = new ArrayList<>();

        String sql = """
                SELECT id, author, text, created_at
                FROM comments
                ORDER BY id DESC
                """;

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Comment comment = new Comment(
                        resultSet.getLong("id"),
                        resultSet.getString("author"),
                        resultSet.getString("text"),
                        resultSet.getTimestamp("created_at").toString()
                );

                comments.add(comment);
            }

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            objectMapper.writeValue(response.getWriter(), comments);

        } catch (Exception e) {
            logger.error("Failed to retrieve comments", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }

    // POST /comments - додавання нового відгуку
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String author = request.getParameter("author");
        String text = request.getParameter("text");

        // Перевірка обов'язкових полів
        if (author == null || author.trim().isEmpty()
                || text == null || text.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Author and text are required"
            );
            return;
        }

        // Перевірка довжини полів
        if (author.length() > 64 || text.length() > 1000) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Author must be <= 64 characters and text <= 1000 characters"
            );
            return;
        }

        String sql = """
                INSERT INTO comments (author, text)
                VALUES (?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, author.trim());
            statement.setString(2, text.trim());

            statement.executeUpdate();

            long id = -1;

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getLong(1);
                }
            }

            // Логування успішного додавання
            logger.info(
                    "New comment added: id={}, author={}, textLength={}",
                    id, author.trim(), text.length()
            );

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (Exception e) {
            logger.error("Failed to add comment", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }
}
