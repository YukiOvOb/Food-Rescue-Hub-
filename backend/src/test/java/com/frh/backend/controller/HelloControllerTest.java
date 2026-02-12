package com.frh.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class HelloControllerTest {

  private final HelloController helloController = new HelloController();

  @Test
  void hello_returnsExpectedMessage() {

    Map<String, String> response = helloController.hello();

    assertEquals("Hello from Spring Boot!", response.get("message"));
  }

  @Test
  void loginSafeJdbc_returnsUserFound_whenUserExists() throws Exception {

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(true);

    try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
      driverManagerMock
          .when(() -> DriverManager.getConnection("jdbc:h2:mem:testdb"))
          .thenReturn(connection);

      String result = helloController.loginSafeJdbc("alice");

      assertEquals("user found", result);
      Mockito.verify(preparedStatement).setString(1, "alice");
    }
  }

  @Test
  void loginSafeJdbc_returnsUserNotFound_whenUserDoesNotExist() throws Exception {

    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(false);

    try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
      driverManagerMock
          .when(() -> DriverManager.getConnection("jdbc:h2:mem:testdb"))
          .thenReturn(connection);

      String result = helloController.loginSafeJdbc("bob");

      assertEquals("user not found", result);
      Mockito.verify(preparedStatement).setString(1, "bob");
    }
  }

  @Test
  void loginSafeJdbc_returnsUserNotFound_whenUsernameIsBlank() {
    String result = helloController.loginSafeJdbc("   ");

    assertEquals("user not found", result);
  }

  @Test
  void loginSafeJdbc_returnsUserNotFound_whenUsernameIsNull() {
    String result = helloController.loginSafeJdbc(null);

    assertEquals("user not found", result);
  }

  @Test
  void loginSafeJdbc_returnsUserNotFound_whenSqlExceptionOccurs() throws Exception {
    SQLException sqlException = new SQLException("DB down");

    try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
      driverManagerMock
          .when(() -> DriverManager.getConnection("jdbc:h2:mem:testdb"))
          .thenThrow(sqlException);

      String result = helloController.loginSafeJdbc("alice");

      assertEquals("user not found", result);
    }
  }

  @Test
  void checkPassword_executesPrivateMethod() throws Exception {
    Method method = HelloController.class.getDeclaredMethod("checkPassword");
    method.setAccessible(true);
    method.invoke(helloController);
  }
}
