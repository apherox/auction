package com.auction.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

@AnalyzeClasses(packages = "com.auction")
class LayeredArchitectureTest {
    @Test
    void controllersShouldNotAccessRepositoryClassesDirectly() {
        // Import all classes from the com.auction package and its sub-packages
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: Controller classes should not directly access repository classes
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .check(importedClasses);
    }

    @Test
    void controllersShouldNotDependOnModels() {
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: Controller classes should not depend on classes from the model package
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("com.auction.model..")
                .check(importedClasses);
    }

    @Test
    void servicesShouldNotAccessControllers() {
        // Import all classes from the com.auction package and its sub-packages
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: No classes in the 'service' package should depend on classes in the 'controller' package
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..api.controller..")
                .check(importedClasses);
    }
}