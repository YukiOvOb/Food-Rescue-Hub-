package com.frh.backend.controller;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        Mockito.when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection("jdbc:h2:mem:testdb"))
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

        Mockito.when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(false);

        try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
            driverManagerMock.when(() -> DriverManager.getConnection("jdbc:h2:mem:testdb"))
                    .thenReturn(connection);

            String result = helloController.loginSafeJdbc("bob");

            assertEquals("user not found", result);
            Mockito.verify(preparedStatement).setString(1, "bob");
        }
    }
}
