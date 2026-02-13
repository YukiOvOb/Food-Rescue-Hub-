package com.frh.backend.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * Seeds local MySQL data once on first startup so local demos have predictable baseline data.
 */
@Component
@Profile("local")
public class LocalDatabaseSeeder {

  private static final Logger log = LoggerFactory.getLogger(LocalDatabaseSeeder.class);

  private final DataSource dataSource;
  private final ResourceLoader resourceLoader;
  private final boolean seedEnabled;
  private final String seedScript;

  public LocalDatabaseSeeder(
      DataSource dataSource,
      ResourceLoader resourceLoader,
      @Value("${app.local.seed.enabled:true}") boolean seedEnabled,
      @Value("${app.local.seed.script:classpath:data.sql}") String seedScript) {
    this.dataSource = dataSource;
    this.resourceLoader = resourceLoader;
    this.seedEnabled = seedEnabled;
    this.seedScript = seedScript;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void seedIfNeeded() {
    if (!seedEnabled) {
      log.info("Local DB seed is disabled (app.local.seed.enabled=false).");
      return;
    }

    if (!isBusinessDataEmpty()) {
      log.info("Local DB already contains data. Skipping local seed.");
      return;
    }

    runSeedScript();
  }

  private boolean isBusinessDataEmpty() {
    String sql = "SELECT COUNT(*) FROM consumer_profiles";

    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {

      if (!resultSet.next()) {
        throw new IllegalStateException("Seed pre-check did not return a row.");
      }

      long count = resultSet.getLong(1);
      log.info("Local seed pre-check: consumer_profiles row count={}", count);
      return count == 0;
    } catch (SQLException ex) {
      throw new IllegalStateException("Failed to inspect database before local seed.", ex);
    }
  }

  private void runSeedScript() {
    Resource resource = resourceLoader.getResource(seedScript);
    if (!resource.exists()) {
      throw new IllegalStateException("Local seed script not found: " + seedScript);
    }

    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.setContinueOnError(false);
    populator.addScript(resource);
    DatabasePopulatorUtils.execute(populator, dataSource);

    log.info("Local DB seed completed using script: {}", seedScript);
  }
}
