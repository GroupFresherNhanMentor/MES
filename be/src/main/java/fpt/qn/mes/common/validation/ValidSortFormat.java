package fpt.qn.mes.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SortFormatValidator.class)
public @interface ValidSortFormat {
    String message() default "Sort must be 'field' or 'field,asc' or 'field,desc'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
