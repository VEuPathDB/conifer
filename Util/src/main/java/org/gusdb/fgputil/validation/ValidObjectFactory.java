package org.gusdb.fgputil.validation;

/**
 * Provides a way to declare that an object is valid or invalid.
 * 
 * @author rdoherty
 *
 */
public class ValidObjectFactory {

  private static abstract class Valid<T extends Validateable> {

    private final T _validatedObject;

    private Valid(T validatedObject) {
      _validatedObject = validatedObject;
    }

    public T getObject() {
      return _validatedObject;
    }
  }

  public static class SyntacticallyValid<T extends Validateable> extends Valid<T> {
    private SyntacticallyValid(T validatedObject) {
      super(validatedObject);
    }
  }

  public static class SemanticallyValid<T extends Validateable> extends SyntacticallyValid<T> {
    private SemanticallyValid(T validatedObject) {
      super(validatedObject);
    }
  }

  /**
   * Attempts to wrap a Validateable object in a SyntacticallyValid wrapper.  If the passed object is not
   * syntactically valid, throws an exception.
   * 
   * @param validatedObject syntactically valid object
   * @return wrapper around passed object
   * @throws ValidObjectWrappingException if passed object is not syntactically valid
   */
  public static <T extends Validateable> SyntacticallyValid<T> getSyntacticallyValid(T validatedObject) {
    ValidationBundle validation = validatedObject.getValidationBundle();
    if (validation.getStatus().equals(ValidationStatus.VALID)) {
      switch(validation.getLevel()) {
        case SYNTACTIC: return new SyntacticallyValid<>(validatedObject);
        case SEMANTIC:  return new SemanticallyValid<>(validatedObject);
        default: /* drop through to exception */
      }
    }
    throw new ValidObjectWrappingException();
  }

  /**
   * Attempts to wrap a Validateable object in a SemanticallyValid wrapper.  If the passed object is not
   * semantically valid, throws an exception.
   * 
   * @param validatedObject semantically valid object
   * @return wrapper around passed object
   * @throws ValidObjectWrappingException if passed object is not semantically valid
   */
  public static <T extends Validateable> SemanticallyValid<T> getSemanticallyValid(T validatedObject) {
    ValidationBundle validation = validatedObject.getValidationBundle();
    if (validation.getStatus().equals(ValidationStatus.VALID) &&
        validation.getLevel().equals(ValidationLevel.SEMANTIC)) {
      return new SemanticallyValid<>(validatedObject);
    }
    throw new ValidObjectWrappingException();
  }
}
