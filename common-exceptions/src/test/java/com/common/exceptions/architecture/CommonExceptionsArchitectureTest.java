package com.common.exceptions.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.common", importOptions = ImportOption.DoNotIncludeTests.class)
class CommonExceptionsArchitectureTest {

  @ArchTest
  static final ArchRule common_exception_types_should_not_depend_on_services =
      noClasses()
          .that()
          .resideInAnyPackage("..exceptions..", "..response..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.microservices..", "org.springframework..");

  @ArchTest
  static final ArchRule common_packages_should_be_free_of_cycles =
      slices().matching("com.common.(*)..").should().beFreeOfCycles();
}
