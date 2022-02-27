/*
 * Copyright (c) 2022 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.sabi;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Checking on architectural constraints
 *
 * @author Stefan Schubert
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ArchitectureTest {

    private static final String PACKAGE_PREFIX = "de.bluewhale.sabi.";
    private static final String PACKAGE_PREFIX_WITH_WILDCARD = ".";

    private static JavaClasses classesFromSabi = new ClassFileImporter().importPackages(PACKAGE_PREFIX)
            .that(DescribedPredicate.not(JavaClass.Predicates.simpleNameEndingWith("Test")))
            .that(DescribedPredicate.not(JavaClass.Predicates.simpleNameEndingWith("TestSuite")));

    DescribedPredicate<JavaClass> isJavaClass = new DescribedPredicate<>("is Java class") {
        @Override
        public boolean apply(JavaClass clazz) {
            return clazz.getPackageName().startsWith("java");
        }
    };
    DescribedPredicate<JavaClass> isFrameworkClass = new DescribedPredicate<>("is Java class") {
        @Override
        public boolean apply(JavaClass clazz) {
            boolean result = clazz.getPackageName().startsWith("org.") ||
                    clazz.getPackageName().startsWith("com.") ||
                    clazz.getPackageName().startsWith("io.swagger");
            return result;
        }
    };
    DescribedPredicate<JavaClass> isSabiClass = new DescribedPredicate<>("is any Sabi class") {
        @Override
        public boolean apply(JavaClass clazz) {
            return clazz.getPackageName().startsWith(PACKAGE_PREFIX);
        }
    };

    DescribedPredicate<JavaClass> isSabiBoundaryClass = new DescribedPredicate<>("is any Boundary class") {
        @Override
        public boolean apply(JavaClass clazz) {
            boolean result = clazz.getPackageName().startsWith("de.bluewhale.sabi.model") ||
                    clazz.getPackageName().startsWith("de.bluewhale.sabi.exception");
            return result;
        }
    };

    @Test
    public void test_onion_architecture_inside_one_component_using_layers() {
        // arrange
        Layer coreDataLayer = new Layer("JPA", PACKAGE_PREFIX_WITH_WILDCARD + ".persistence..");
        Layer serviceLayer = new Layer("Services", PACKAGE_PREFIX_WITH_WILDCARD + ".services..");
        Layer securityLayer = new Layer("Security", PACKAGE_PREFIX_WITH_WILDCARD + ".security..");
        Layer apiLayer = new Layer("API", PACKAGE_PREFIX_WITH_WILDCARD + ".rest..");
        Layer utilLayer = new Layer("Utilities", PACKAGE_PREFIX_WITH_WILDCARD + ".util..");

        Architectures.LayeredArchitecture layeredArchitecture = Architectures.layeredArchitecture()
                .withOptionalLayers(true)
                .layer(coreDataLayer.name).definedBy(coreDataLayer.pkg)
                .layer(serviceLayer.name).definedBy(serviceLayer.pkg)
                .layer(securityLayer.name).definedBy(securityLayer.pkg)
                .layer(apiLayer.name).definedBy(apiLayer.pkg)
                .layer(utilLayer.name).definedBy(utilLayer.pkg);

        // act, assert
        layeredArchitecture
                .whereLayer(apiLayer.name).mayNotBeAccessedByAnyLayer()
                .whereLayer(apiLayer.name).mayOnlyAccessLayers(serviceLayer.name)
                .whereLayer(serviceLayer.name).mayOnlyAccessLayers(coreDataLayer.name,utilLayer.name, securityLayer.name)
                // ignore all dependencies to java..
                .ignoreDependency(isSabiClass, isJavaClass)
                // ignore all dependencies to spring
                .ignoreDependency(isSabiClass, isFrameworkClass)
                // ignore all dependencies to boundary artefact
                .ignoreDependency(isSabiClass, isSabiBoundaryClass)
                .because("we want to enforce the onion architecure inside each component")
                .check(classesFromSabi);
    }

    class Layer {
        String name;
        String pkg;

        Layer(String name, String pkg) {
            this.name = name;
            this.pkg = pkg;
        }
    }

}