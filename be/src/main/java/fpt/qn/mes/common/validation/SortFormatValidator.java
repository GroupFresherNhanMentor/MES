package fpt.qn.mes.common.validation;

import java.util.List;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SortFormatValidator implements ConstraintValidator<ValidSortFormat, List<String>> {

    private static final Pattern FIELD_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$");

    @Override
    public boolean isValid(List<String> sort, ConstraintValidatorContext context) {
        if (sort == null || sort.isEmpty()) return true;
        for (String entry : sort) {
            if (entry == null || entry.isBlank()) return false;
            String[] parts = entry.split(",", 2);
            if (!FIELD_PATTERN.matcher(parts[0].trim()).matches()) return false;
            if (parts.length > 1) {
                String dir = parts[1].trim();
                if (!"asc".equalsIgnoreCase(dir) && !"desc".equalsIgnoreCase(dir)) return false;
            }
        }
        return true;
    }
}
