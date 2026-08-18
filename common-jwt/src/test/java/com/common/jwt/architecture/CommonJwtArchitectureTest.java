package com.common.jwt.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.common.jwt", importOptions = ImportOption.DoNotIncludeTests.class)
class CommonJwtArchitectureTest {

  @ArchTest
  static final ArchRule common_jwt_should_not_depend_on_application_services =
      noClasses()
          .that()
          .resideInAPackage("com.common.jwt..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("com.microservices..");

  @ArchTest
  static final ArchRule filters_should_not_depend_on_configuration_factories =
      noClasses()
          .that()
          .resideInAPackage("..filter..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..config..");

  @ArchTest
  static final ArchRule authentication_core_should_not_depend_on_filter_or_config_packages =
      noClasses()
          .that()
          .resideInAPackage("..authentication..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..filter..", "..config..");

  @ArchTest
  static final ArchRule jwt_packages_should_be_free_of_cycles =
      slices().matching("com.common.jwt.(*)..").should().beFreeOfCycles();
}
