package thedonkey.rest;

import java.math.BigDecimal;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A special matcher that compares two numbers which can be of different type. E.g. it can compare
 * an int to a long.
 */
public class NumberMatcher<T extends Number> extends TypeSafeMatcher<T> {
  private final T expected;

  private NumberMatcher(T expected) {
    this.expected = expected;
  }

  @Override
  public boolean matchesSafely(T actual) {

    BigDecimal expectedBigDecimal = new BigDecimal(expected.toString());
    BigDecimal actualBigDecimal = new BigDecimal(actual.toString());

    return expectedBigDecimal.compareTo(actualBigDecimal) == 0;
  }

  @Override
  public void describeMismatchSafely(T actual, Description mismatchDescription) {
    mismatchDescription.appendValue(actual).appendText(" was ").appendText(String.valueOf(matchesSafely(actual)))
        .appendText(" ").appendValue(expected);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a value ");
    description.appendText(" ").appendValue(expected);
  }

  /**
   * Creates a matcher of {@link Number} object that matches when the examined object is equal to
   * the specified value, as reported by the <code>compareTo</code> method of the <b>examined</b>
   * object after they were converted to {@link BigDecimal}.
   * <p/>
   * For example:
   * 
   * <pre>
   * assertThat(1L, comparesEqualTo(1))
   * </pre>
   * 
   * @param value the value which, when passed to the compareTo method of the examined object,
   *        should return zero
   * 
   */
  @Factory
  public static <T extends Number> Matcher<T> comparesEqualTo(T value) {
    return new NumberMatcher<T>(value);
  }
}
