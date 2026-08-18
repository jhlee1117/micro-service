package com.microservices.gateway.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;

@AnalyzeClasses(
    packages = "com.microservices.gateway",
    importOptions = ImportOption.DoNotIncludeTests.class)
class GatewayArchitectureTest {

  @ArchTest
  static final ArchRule gateway_code_should_stay_in_gateway_packages =
      classes()
          .should()
          .resideInAnyPackage(
              "com.microservices.gateway",
              "com.microservices.gateway.config..",
              "com.microservices.gateway.security..");

  @ArchTest
  static final ArchRule security_components_should_not_depend_on_config_package =
      noClasses()
          .that()
          .resideInAPackage("..security..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.microservices.gateway.config..");

  @ArchTest
  static final ArchRule configuration_classes_should_reside_in_config_or_security_packages =
      classes()
          .that()
          .areAnnotatedWith(Configuration.class)
          .should()
          .resideInAnyPackage("..config..", "..security..");

  @ArchTest
  static final ArchRule gateway_packages_should_be_free_of_cycles =
      slices().matching("com.microservices.gateway.(*)..").should().beFreeOfCycles();
}
