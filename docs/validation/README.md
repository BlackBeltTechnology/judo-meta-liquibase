# Liquibase Model Validation

This project supports two validation frameworks that run in parallel:

## Validation Approaches

### EVL Validation (Epsilon)

Traditional Epsilon Validation Language (EVL) based validation using `.evl` files located in:

```
model/src/main/epsilon/validations/liquibase.evl
```

Executed via `LiquibaseEpsilonValidator.validateLiquibase()`.

### Java Validation (Zeta Framework)

Native Java-based validation using the [Zeta Validation Framework](https://github.com/BlackBeltTechnology/judo-zeta).

Key benefits:
- Better IDE integration (debugging, refactoring)
- Improved performance (~3x faster than EVL)
- Compile-time constraint name checking
- Type-safe validation rules

Executed via `LiquibaseValidator.validateLiquibase()`.

## Architecture

```
model/src/main/java/hu/blackbelt/judo/meta/liquibase/validation/
    LiquibaseValidator.java         # Main entry point
    LiquibaseModelProvider.java     # Model traversal
    LiquibaseValidationConstants.java   # Constraint name constants
    LiquibaseValidationException.java   # Validation failure exception
    rules/
        ChangeSetValidations.java   # Validation rules for ChangeSets
```

## Usage

### Basic Validation

```java
import hu.blackbelt.judo.meta.liquibase.validation.LiquibaseValidator;

// Simple validation
LiquibaseValidator.validateLiquibase(log, liquibaseModel);
```

### Validation with Expected Results

```java
// For testing - specify expected errors and warnings
LiquibaseValidator.validateLiquibase(
    log,
    liquibaseModel,
    List.of("ConstraintName1", "ConstraintName2"),  // expectedErrors
    List.of("CritiqueName1"),                       // expectedWarnings
    false  // parallel execution
);
```

### Dual Validation Testing

Tests use parameterized tests to run against both validators:

```java
public class MyValidationTest extends AbstractLiquibaseValidationTest {

    @ParameterizedTest(name = "testConstraint [{0}]")
    @EnumSource(ValidatorType.class)
    void testConstraint(ValidatorType type) throws Exception {
        this.validatorType = type;
        initModel();

        // Build test model...

        runValidation(
            List.of("ExpectedConstraintName"),
            Collections.emptyList()
        );
    }
}
```

## Adding New Validation Rules

1. Add constant to `LiquibaseValidationConstants.java`:

```java
public static final String CONSTRAINT_MY_RULE = "MyRuleName";
```

2. Create or update a validations class in `rules/` package:

```java
@ValidationContext(MyElement.class)
public class MyElementValidations {

    @Constraint(
        name = LiquibaseValidationConstants.CONSTRAINT_MY_RULE,
        message = "Element must satisfy condition"
    )
    public ValidationRule myRule() {
        return (element, ctx) -> {
            MyElement self = (MyElement) element;
            return self.isValid()
                ? ValidationResult.pass()
                : ValidationResult.fail("Element failed validation");
        };
    }
}
```

3. Register in `LiquibaseValidator.java`:

```java
registry.register(MyElementValidations.class);
```

4. Add EVL equivalent in `liquibase.evl` (for parity testing):

```evl
context MyElement {
    constraint MyRuleName {
        check: self.isValid()
        message: "Element must satisfy condition"
    }
}
```

## Performance

Performance comparison on a model with ~12,000 elements:

| Validator | Time (ms) | Speedup |
|-----------|-----------|---------|
| EVL       | ~300 ms   | 1x      |
| Java      | ~100 ms   | 3x      |
| Java (parallel) | ~50 ms | 6x |

## Related Documentation

- [Zeta Validation Framework](https://github.com/BlackBeltTechnology/judo-zeta)
- [ESM Validation Implementation](https://github.com/BlackBeltTechnology/judo-meta-esm/blob/develop/docs/validation/README.md)
