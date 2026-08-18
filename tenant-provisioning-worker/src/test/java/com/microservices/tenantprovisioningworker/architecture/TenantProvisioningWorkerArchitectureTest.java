package com.microservices.tenantprovisioningworker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@AnalyzeClasses(
    packages = "com.microservices.tenantprovisioningworker",
    importOptions = ImportOption.DoNotIncludeTests.class)
class TenantProvisioningWorkerArchitectureTest {

  @ArchTest
  static final ArchRule listeners_should_not_access_infrastructure_configuration_directly =
      noClasses()
          .that()
          .resideInAPackage("..listener..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..config..");

  @ArchTest
  static final ArchRule services_should_not_depend_on_listener_or_config_packages =
      noClasses()
          .that()
          .resideInAPackage("..service..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..listener..", "..config..");

  @ArchTest
  static final ArchRule event_messages_should_not_depend_on_worker_layers =
      noClasses()
          .that()
          .resideInAPackage("..event..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..listener..", "..service..", "..config..");

  @ArchTest
  static final ArchRule services_should_reside_in_service_package =
      classes().that().areAnnotatedWith(Service.class).should().resideInAPackage("..service..");

  @ArchTest
  static final ArchRule configuration_classes_should_reside_in_config_package =
      classes()
          .that()
          .areAnnotatedWith(Configuration.class)
          .should()
          .resideInAPackage("..config..");

  @ArchTest
  static final ArchRule worker_packages_should_be_free_of_cycles =
      slices()
          .matching("com.microservices.tenantprovisioningworker.(*)..")
          .should()
          .beFreeOfCycles();
}
