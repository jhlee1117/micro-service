package com.microservices.auth.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@AnalyzeClasses(
    packages = "com.microservices.auth",
    importOptions = ImportOption.DoNotIncludeTests.class)
class AuthArchitectureTest {

  @ArchTest
  static final ArchRule controllers_should_not_access_repositories_directly =
      noClasses()
          .that()
          .resideInAnyPackage("..controller..", "..oauth..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..repository..");

  @ArchTest
  static final ArchRule services_should_not_depend_on_web_controllers =
      noClasses()
          .that()
          .resideInAPackage("..service..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..controller..");

  @ArchTest
  static final ArchRule repositories_should_not_depend_on_service_or_web_layers =
      noClasses()
          .that()
          .resideInAPackage("..repository..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..controller..", "..service..", "..oauth..", "..security..");

  @ArchTest
  static final ArchRule domain_entities_should_not_depend_on_application_layers =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..controller..",
              "..service..",
              "..repository..",
              "..dto..",
              "..oauth..",
              "..security..",
              "..config..",
              "..utils..");

  @ArchTest
  static final ArchRule controllers_should_reside_in_controller_or_oauth_packages =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAnyPackage("..controller..", "..oauth..");

  @ArchTest
  static final ArchRule exception_handlers_should_reside_in_utils_package =
      classes()
          .that()
          .areAnnotatedWith(RestControllerAdvice.class)
          .should()
          .resideInAPackage("..utils..");

  @ArchTest
  static final ArchRule services_should_reside_in_service_or_security_packages =
      classes()
          .that()
          .areAnnotatedWith(Service.class)
          .should()
          .resideInAnyPackage("..service..", "..security..");

  @ArchTest
  static final ArchRule repositories_should_reside_in_repository_package =
      classes()
          .that()
          .areAnnotatedWith(Repository.class)
          .should()
          .resideInAPackage("..repository..");

  @ArchTest
  static final ArchRule core_application_packages_should_be_free_of_cycles =
      slices()
          .matching("com.microservices.auth.(*)..")
          .should()
          .beFreeOfCycles()
          .ignoreDependency(
              "com.microservices.auth.service.OAuthLoginService",
              "com.microservices.auth.oauth.OAuthLoginResult")
          .ignoreDependency(
              "com.microservices.auth.service.OAuthLoginService",
              "com.microservices.auth.oauth.OAuthPendingSignup")
          .ignoreDependency(
              "com.microservices.auth.service.OAuthLoginService",
              "com.microservices.auth.oauth.OAuthSignupCompleteRequest")
          .ignoreDependency(
              "com.microservices.auth.service.OAuthLoginService",
              "com.microservices.auth.oauth.OAuthUserProfile");
}
