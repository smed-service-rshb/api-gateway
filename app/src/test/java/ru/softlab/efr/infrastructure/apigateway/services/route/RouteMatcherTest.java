package ru.softlab.efr.infrastructure.apigateway.services.route;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author krenev
 * @since 14.04.2017
 */
@RunWith(JUnit4.class)
public class RouteMatcherTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCorrectRoute0() {
        String pattern = "/some-path";

        String path = "/some-path";
        testCorrectRoute(path, pattern, pattern);
    }

    @Test
    public void testCorrectRoute1() {
        String pattern = "/some-path/**";

        String path = "/some-path/qqq";
        testCorrectRoute(path, pattern, pattern);
    }

    @Test
    public void testCorrectRoute2() {
        String unmatchedPattern = "/some-path/**";
        String matchedPattern = "/some-path/qqq";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, unmatchedPattern, matchedPattern);
    }

    @Test
    public void testCorrectRoute3() {
        String unmatchedPattern = "/some-path/**";
        String matchedPattern = "/some-path/qqq";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, matchedPattern, unmatchedPattern);
    }

    @Test
    public void testCorrectRoute4() {
        String matchedPattern = "/some-path/**";
        String unmatchedPattern = "/some-path/qqqq";

        String path = "/some-path/qqq/q/asdsad/sad";
        testCorrectRoute(path, matchedPattern, matchedPattern, unmatchedPattern);
    }

    @Test
    public void testCorrectRoute5() {
        String unmatchedPattern = "/some-path/**";
        String matchedPattern = "/some-path/*qqq";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, unmatchedPattern, matchedPattern);
    }

    @Test
    public void testCorrectRoute6() {
        String unmatchedPattern = "/some-path/**";
        String matchedPattern = "/some-path/qqq*";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, unmatchedPattern, matchedPattern);
    }

    @Test
    public void testCorrectRoute7() {
        String unmatchedPattern = "/some-path/**";
        String matchedPattern = "/some-path/qq*q";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, unmatchedPattern, matchedPattern);
    }

    @Test
    public void testCorrectRoute8() {
        String unmatchedPattern = "/some-path/**";
        String matchedPattern = "/some-path/*";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, unmatchedPattern, matchedPattern);
    }

    @Test
    public void testCorrectRoute9() {
        String unmatchedPattern1 = "/some-path/**";
        String unmatchedPattern2 = "/some-path/*";
        String matchedPattern = "/some-path/qq*q";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, unmatchedPattern1, matchedPattern, unmatchedPattern2);
    }

    @Test
    public void testCorrectRoute10() {
        String unmatchedPattern1 = "/some-path/**";
        String unmatchedPattern2 = "/some-path/**";
        String matchedPattern = "/some-path/*";

        String path = "/some-path/qqq";
        testCorrectRoute(path, matchedPattern, unmatchedPattern1, unmatchedPattern2, matchedPattern);
    }

    @Test
    public void testCorrectRoute11() {
        String pattern = "/some-path/*/*";

        String path = "/some-path/qqq/eee";
        testCorrectRoute(path, pattern, pattern);
    }

    @Test
    public void testCorrectRoute12() {
        String pattern = "/some-path/*/{param}";

        String path = "/some-path/qqq/eee";
        testCorrectRoute(path, pattern, pattern);
    }

    @Test
    public void testIncorrectRoute0() {
        String pattern = "/some-path";

        String path = "/some-another-path";
        testIncorrectRoute(path, pattern);
    }

    @Test
    public void testIncorrectRoute1() {
        String pattern = "/some-path/*";

        String path = "/some-another-path";
        testIncorrectRoute(path, pattern);
    }

    @Test
    public void testIncorrectRoute2() {
        String pattern = "/some-path/*";

        String path = "/some-path/qqq/eee";
        testIncorrectRoute(path, pattern, pattern);
    }

    @Test
    public void testIncorrectRoute3() {
        String pattern = "/some-path/*/*/*";

        String path = "/some-path/qqq/eee";
        testIncorrectRoute(path, pattern, pattern);
    }

    @Test
    public void testIncorrectRoute4() {
        String pattern = "/some-path/*/";

        String path = "/some-path/qqq";
        testIncorrectRoute(path, pattern, pattern);
    }

    @Test
    public void testAmbiguousRoutes1() {
        String pattern1 = "/some-path/**";
        String pattern2 = "/some-path/**";
        String pattern3 = "/some-path/*";

        String path = "/some-path/qqq/eee";
        testAmbiguousRoute(path, pattern1, pattern2, pattern3);
    }

    @Test
    public void testAmbiguousRoutes2() {
        String pattern1 = "/some-path/{param1}";
        String pattern2 = "/some-path/{param2}";
        String pattern3 = "/some-path/*";

        String path = "/some-path/qqq";
        testAmbiguousRoute(path, pattern1, pattern2, pattern3);
    }

    @Test
    public void testAmbiguousRoutes3() {
        String pattern1 = "/some-path/qq*";
        String pattern2 = "/some-path/q*q";
        String pattern3 = "/some-path/*";

        String path = "/some-path/qqq";
        testAmbiguousRoute(path, pattern1, pattern2, pattern3);
    }

    private void testCorrectRoute(String path, String matchedPattern, String... patterns) {
        RouteMatcher routeMatcher = createRouteMatcher(patterns);

        Route route = routeMatcher.findRoute(path);

        assertNotNull(route);
        assertThat(route.getPattern(), is(matchedPattern));
    }

    private void testIncorrectRoute(String path, String... patterns) {
        RouteMatcher routeMatcher = createRouteMatcher(patterns);

        Route route = routeMatcher.findRoute(path);

        assertNull(route);
    }

    private void testAmbiguousRoute(String path, String... patterns) {
        exception.expect(AmbiguousRoutesException.class);
        RouteMatcher routeMatcher = createRouteMatcher(patterns);

        Route route = routeMatcher.findRoute(path);
    }

    private RouteMatcher createRouteMatcher(String[] patterns) {
        return new RouteMatcher(new RoutesProviderImpl(patterns));
    }
}
