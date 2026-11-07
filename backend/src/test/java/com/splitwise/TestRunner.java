package com.splitwise;

import com.splitwise.strategy.DebtSimplificationServiceTest;
import com.splitwise.strategy.SplitStrategyTest;

import java.lang.reflect.Method;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("RUNNING SPLITWISE PRODUCTION ALGORITHM TEST SUITE");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        Class<?>[] testClasses = {
                SplitStrategyTest.class,
                DebtSimplificationServiceTest.class
        };

        for (Class<?> testClass : testClasses) {
            System.out.println("\nTesting Class: " + testClass.getSimpleName());
            Method beforeEach = null;
            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(org.junit.jupiter.api.BeforeEach.class)) {
                    beforeEach = m;
                    beforeEach.setAccessible(true);
                    break;
                }
            }

            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                    try {
                        m.setAccessible(true);
                        Object instance = testClass.getDeclaredConstructor().newInstance();
                        if (beforeEach != null) {
                            beforeEach.invoke(instance);
                        }
                        m.invoke(instance);
                        System.out.println("  [PASS] " + m.getName());
                        passed++;
                    } catch (Exception e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        System.err.println("  [FAIL] " + m.getName() + " -> " + cause.getMessage());
                        cause.printStackTrace();
                        failed++;
                    }
                }
            }
        }

        System.out.println("\n=================================================");
        System.out.printf("TOTAL TESTS: %d | PASSED: %d | FAILED: %d%n", (passed + failed), passed, failed);
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
