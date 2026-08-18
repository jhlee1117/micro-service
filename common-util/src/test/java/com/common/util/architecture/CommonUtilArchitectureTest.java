package com.common.util.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.common.util", importOptions = ImportOption.DoNotIncludeTests.class)
class CommonUtilArchitectureTest {

  @ArchTest
  static final ArchRule common_util_should_not_depend_on_application_or_spring_layers =
      noClasses()
          .that()
          .resideInAPackage("com.common.util..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.microservices..", "org.springframework..");
}
