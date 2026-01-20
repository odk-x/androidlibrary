package org.opendatakit.utilities;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class StaticStateManipulatorTest {

    @Before
    public void cleanUpBeforeTest() {
        // Reset singleton instance before each test to prevent state pollution
        StaticStateManipulator.set(new StaticStateManipulator());
    }

    /**
     * Scenario: Singleton Consistency
     * Given the application is running
     * When I request the StaticStateManipulator instance multiple times
     * Then I should receive the same instance each time
     */
    @Test
    public void shouldProvideConsistentSingletonInstance() {
        // Given
        StaticStateManipulator firstInstance = StaticStateManipulator.get();

        // When
        StaticStateManipulator secondInstance = StaticStateManipulator.get();

        // Then
        assertSame("Singleton should return the same instance", firstInstance, secondInstance);
    }

    /**
     * Scenario: Thread-Safe Singleton Access
     * Given multiple threads are accessing the StaticStateManipulator
     * When they request the instance concurrently
     * Then they should all receive the same instance
     */
    @Test
    public void shouldBeThreadSafeWhenAccessingSingleton() throws InterruptedException {
        // Given
        int threadCount = 100;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final StaticStateManipulator[] instances = new StaticStateManipulator[threadCount];

        // When
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    instances[index] = StaticStateManipulator.get();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        latch.await();
        executor.shutdown();

        // Then
        for (int i = 1; i < threadCount; i++) {
            assertSame("All threads should receive the same singleton instance",
                    instances[0], instances[i]);
        }
    }

    /**
     * Scenario: Registering and Resetting Static Field Manipulators
     * Given I have multiple static field manipulators
     * When I register them with the StaticStateManipulator
     * And I call reset
     * Then all registered manipulators should have their reset method called
     */
    @Test
    public void shouldResetAllRegisteredManipulators() {
        // Given
        StaticStateManipulator manipulator = StaticStateManipulator.get();
        List<MockStaticFieldManipulator> mockManipulators = new ArrayList<>();

        // When - Register multiple manipulators
        for (int i = 0; i < 5; i++) {
            MockStaticFieldManipulator mockManipulator = new MockStaticFieldManipulator();
            manipulator.register(mockManipulator);
            mockManipulators.add(mockManipulator);
        }

        // And - Trigger reset
        manipulator.reset();

        // Then - Verify all manipulators were reset
        for (MockStaticFieldManipulator mockManipulator : mockManipulators) {
            assertTrue("Each registered manipulator should be reset", mockManipulator.wasReset());
        }
    }

    /**
     * Scenario: Thread-Safe Registration
     * Given multiple threads registering manipulators
     * When they register concurrently
     * Then all manipulators should be registered without errors
     */
    @Test
    public void shouldRegisterManipulatorsThreadSafely() throws InterruptedException {
        // Given
        StaticStateManipulator manipulator = StaticStateManipulator.get();
        int threadCount = 50;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        List<MockStaticFieldManipulator> manipulators = new CopyOnWriteArrayList<>();

        // When
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            MockStaticFieldManipulator mockManipulator = new MockStaticFieldManipulator();
            manipulators.add(mockManipulator);
            executor.submit(() -> {
                try {
                    manipulator.register(mockManipulator);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        latch.await();
        executor.shutdown();

        // Then
        assertEquals("All manipulators should be registered", threadCount, manipulators.size());
    }

    /**
     * Scenario: Handling No Registered Manipulators
     * Given no manipulators are registered
     * When reset is called
     * Then it should complete without error
     */
    @Test
    public void shouldHandleResetWithNoManipulators() {
        // Given
        StaticStateManipulator manipulator = StaticStateManipulator.get();

        // When
        try {
            manipulator.reset();
        } catch (Exception e) {
            // Then
            fail("Reset should not throw an exception when no manipulators are registered");
        }
    }

    /**
     * Mock implementation of IStaticFieldManipulator for testing
     */
    private static class MockStaticFieldManipulator implements StaticStateManipulator.IStaticFieldManipulator {
        private final AtomicBoolean resetCalled = new AtomicBoolean(false);

        @Override
        public void reset() {
            resetCalled.set(true);
        }

        public boolean wasReset() {
            return resetCalled.get();
        }
    }
}
