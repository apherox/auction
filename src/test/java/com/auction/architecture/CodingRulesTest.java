package com.auction.architecture;

import com.auction.api.Api;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.auction")
class CodingRulesTest {


    @Test
    void controllerClassesShouldBeAnnotatedWithRestController() {
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: Controller classes should be annotated with @RestController
        classes()
                .that().resideInAPackage("..controller..")
                .and().areNotInterfaces()
                .should().beAnnotatedWith(RestController.class)
                .check(importedClasses);
    }

    @Test
    void controllerClassesShouldExtendInterfacesAnnotatedWithApiAnnotation() {
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: Controller classes should implement interface with name ending with 'Api' and a prefix
        // with name same as the controller without the 'Controller' suffix
        ArchRuleDefinition.classes()
                .that().resideInAPackage("..controller..")
                .and().areNotInterfaces()
                .and().haveSimpleNameEndingWith("Controller")
                .should(new ArchCondition<>("implement API interface") {
                    @Override
                    public void check(final JavaClass javaClass, final ConditionEvents events) {
                        final Set<JavaType> interfaces = javaClass.getInterfaces();
                        boolean failure = true;
                        for (final JavaType i : interfaces) {
                            final String classSimpleName = javaClass.getSimpleName();
                            if (i.toErasure().getSimpleName().equals(classSimpleName.substring(0, classSimpleName.length() - "Controller".length()) + "Api")) {
                                failure = false;
                                break;
                            }
                        }
                        if (failure) {
                            events.add(new SimpleConditionEvent(javaClass, false, javaClass.getFullName() + " does not implement API interface"));
                        }
                    }
                })
                .check(importedClasses);

    }

    @Test
    void apiInterfacesShouldBeAnnotatedWithApiInterface() {
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        ArchRuleDefinition.classes()
                .that().resideInAPackage("..controller..")
                .and().areInterfaces()
                .and().doNotHaveSimpleName("Api")
                .should().beAnnotatedWith(Api.class)
                .andShould().haveSimpleNameEndingWith("Api")
                .check(importedClasses);
    }

    @Test
    void serviceClassesShouldBeAnnotatedWithService() {
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: Service classes should be annotated with @Service
        classes()
                .that().resideInAPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .check(importedClasses);
    }

    @Test
    void repositoryClassesShouldExtendJPARepository() {
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: Repository classes should extend JPARepository
        classes()
                .that().resideInAPackage("..repository..")
                .should().beInterfaces()
                .andShould().beAssignableTo(JpaRepository.class)
                .check(importedClasses);
    }

    @Test
    void mapperClassesShouldBeInMapperPackage() {
        JavaClasses importedClasses = new ClassFileImporter().importPaths(Paths.get("build/classes/java/main"));

        // Rule: Mapper classes should reside in the mapper package
        classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .should().resideInAPackage("..mapper..")
                .check(importedClasses);
    }

}