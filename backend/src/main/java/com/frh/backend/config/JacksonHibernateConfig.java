package com.frh.backend.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonHibernateConfig {

  @Bean
  public Module hibernateModule() {
    Hibernate6Module module = new Hibernate6Module();
    module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
    module.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
    module.enable(Hibernate6Module.Feature.REPLACE_PERSISTENT_COLLECTIONS);
    return module;
  }
}
