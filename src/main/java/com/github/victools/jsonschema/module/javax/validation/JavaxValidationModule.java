/*
 * Copyright 2019 VicTools.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.victools.jsonschema.module.javax.validation;

import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.MethodScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigPart;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * JSON Schema Generation Module: based on annotations from the {@code javax.validation.constraints} package.
 * <ul>
 * <li>Determine whether a member is not nullable, base assumption being that all fields and method return values are nullable if not annotated.</li>
 * <li>Populate "minItems" and "maxItems" for containers (i.e. arrays and collections).</li>
 * <li>Populate "minLength", "maxLength" and "format" for strings.</li>
 * <li>Optionally: populate "pattern" for strings.</li>
 * <li>Populate "minimum"/"exclusiveMinimum" and "maximum"/"exclusiveMaximum" for numbers.</li>
 * </ul>
 */
public class JavaxValidationModule implements Module {

    private final List<JavaxValidationOption> options;

    /**
     * Constructor.
     *
     * @param options features to enable
     */
    public JavaxValidationModule(JavaxValidationOption... options) {
        this.options = options == null ? Collections.emptyList() : Arrays.asList(options);
    }

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        this.applyToConfigPart(builder.forFields());
        this.applyToConfigPart(builder.forMethods());
    }

    /**
     * Apply the various annotation-based resolvers for the given configuration part (this is expected to be executed for both fields and methods).
     *
     * @param configPart config builder part to add configurations to
     */
    private void applyToConfigPart(SchemaGeneratorConfigPart<?> configPart) {
        configPart.withNullableCheck(this::isNullable);
        configPart.withArrayMinItemsResolver(this::resolveArrayMinItems);
        configPart.withArrayMaxItemsResolver(this::resolveArrayMaxItems);
        configPart.withStringMinLengthResolver(this::resolveStringMinLength);
        configPart.withStringMaxLengthResolver(this::resolveStringMaxLength);
        configPart.withStringFormatResolver(this::resolveStringFormat);
        configPart.withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
        configPart.withNumberExclusiveMinimumResolver(this::resolveNumberExclusiveMinimum);
        configPart.withNumberInclusiveMaximumResolver(this::resolveNumberInclusiveMaximum);
        configPart.withNumberExclusiveMaximumResolver(this::resolveNumberExclusiveMaximum);

        if (this.options.contains(JavaxValidationOption.INCLUDE_PATTERN_EXPRESSIONS)) {
            configPart.withStringPatternResolver(this::resolveStringPattern);
        }
    }

    /**
     * Retrieves the annotation instance of the given type, either from the field it self or (if not present) from its getter.
     *
     * @param <A> type of annotation
     * @param member field or method to retrieve annotation instance from (or from a field's getter or getter method's field)
     * @param annotationClass type of annotation
     * @return annotation instance (or {@code null})
     * @see MemberScope#getAnnotation(Class)
     * @see FieldScope#findGetter()
     * @see MethodScope#findGetterField()
     */
    protected <A extends Annotation> A getAnnotationFromFieldOrGetter(MemberScope<?, ?> member, Class<A> annotationClass) {
        A annotation = member.getAnnotation(annotationClass);
        if (annotation == null) {
            MemberScope<?, ?> associatedGetterOrField;
            if (member instanceof FieldScope) {
                associatedGetterOrField = ((FieldScope) member).findGetter();
            } else if (member instanceof MethodScope) {
                associatedGetterOrField = ((MethodScope) member).findGetterField();
            } else {
                associatedGetterOrField = null;
            }
            annotation = associatedGetterOrField == null ? null : associatedGetterOrField.getAnnotation(annotationClass);
        }
        return annotation;
    }

    /**
     * Determine whether a given field or method is annotated to be not nullable.
     *
     * @param member the field or method to check
     * @return whether member is annotated as nullable or not (returns null if not specified: assumption it is nullable then)
     */
    protected Boolean isNullable(MemberScope<?, ?> member) {
        Boolean result;
        if (this.getAnnotationFromFieldOrGetter(member, NotNull.class) != null
                || this.getAnnotationFromFieldOrGetter(member, NotBlank.class) != null
                || this.getAnnotationFromFieldOrGetter(member, NotEmpty.class) != null) {
            // field is specifically NOT nullable
            result = Boolean.FALSE;
        } else if (this.getAnnotationFromFieldOrGetter(member, Null.class) != null) {
            // field is specifically null (and thereby nullable)
            result = Boolean.TRUE;
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Determine a given array type's minimum number of items.
     *
     * @param member the field or method to check
     * @return specified minimum number of array items (or null)
     * @see Size
     */
    protected Integer resolveArrayMinItems(MemberScope<?, ?> member) {
        if (member.isContainerType()) {
            Size sizeAnnotation = this.getAnnotationFromFieldOrGetter(member, Size.class);
            if (sizeAnnotation != null && sizeAnnotation.min() > 0) {
                // minimum length greater than the default 0 was specified
                return sizeAnnotation.min();
            }
            if (this.getAnnotationFromFieldOrGetter(member, NotEmpty.class) != null) {
                return 1;
            }
        }
        return null;
    }

    /**
     * Determine a given array type's maximum number of items.
     *
     * @param member the field or method to check
     * @return specified maximum number of array items (or null)
     * @see Size
     */
    protected Integer resolveArrayMaxItems(MemberScope<?, ?> member) {
        if (member.isContainerType()) {
            Size sizeAnnotation = this.getAnnotationFromFieldOrGetter(member, Size.class);
            if (sizeAnnotation != null && sizeAnnotation.max() < 2147483647) {
                // maximum length below the default 2147483647 was specified
                return sizeAnnotation.max();
            }
        }
        return null;
    }

    /**
     * Determine a given text type's minimum number of characters.
     *
     * @param member the field or method to check
     * @return specified minimum number of characters (or null)
     * @see Size
     * @see NotEmpty
     * @see NotBlank
     */
    protected Integer resolveStringMinLength(MemberScope<?, ?> member) {
        if (member.getType().isInstanceOf(CharSequence.class)) {
            Size sizeAnnotation = this.getAnnotationFromFieldOrGetter(member, Size.class);
            if (sizeAnnotation != null && sizeAnnotation.min() > 0) {
                // minimum length greater than the default 0 was specified
                return sizeAnnotation.min();
            }
            if (this.getAnnotationFromFieldOrGetter(member, NotEmpty.class) != null
                    || this.getAnnotationFromFieldOrGetter(member, NotBlank.class) != null) {
                return 1;
            }
        }
        return null;
    }

    /**
     * Determine a given text type's maximum number of characters.
     *
     * @param member the field or method to check
     * @return specified minimum number of characters (or null)
     * @see Size
     */
    protected Integer resolveStringMaxLength(MemberScope<?, ?> member) {
        if (member.getType().isInstanceOf(CharSequence.class)) {
            Size sizeAnnotation = this.getAnnotationFromFieldOrGetter(member, Size.class);
            if (sizeAnnotation != null && sizeAnnotation.max() < 2147483647) {
                // maximum length below the default 2147483647 was specified
                return sizeAnnotation.max();
            }
        }
        return null;
    }

    /**
     * Determine a given text type's format.
     *
     * @param member the field or method to check
     * @return specified format (or null)
     * @see Email
     */
    protected String resolveStringFormat(MemberScope<?, ?> member) {
        if (member.getType().isInstanceOf(CharSequence.class)) {
            Email emailAnnotation = this.getAnnotationFromFieldOrGetter(member, Email.class);
            if (emailAnnotation != null) {
                // @Email annotation was found, indicate the respective format
                if (this.options.contains(JavaxValidationOption.PREFER_IDN_EMAIL_FORMAT)) {
                    // the option was set to rather return the value for the internationalised email format
                    return "idn-email";
                }
                // indicate standard internet email address format
                return "email";
            }
        }
        return null;
    }

    /**
     * Determine a given text type's pattern.
     *
     * @param member the field or method to check
     * @return specified pattern (or null)
     * @see Pattern
     */
    protected String resolveStringPattern(MemberScope<?, ?> member) {
        if (member.getType().isInstanceOf(CharSequence.class)) {
            Pattern patternAnnotation = this.getAnnotationFromFieldOrGetter(member, Pattern.class);
            if (patternAnnotation != null) {
                // @Pattern annotation was found, return its (mandatory) regular expression
                return patternAnnotation.regexp();
            }
            Email emailAnnotation = this.getAnnotationFromFieldOrGetter(member, Email.class);
            if (emailAnnotation != null && !".*".equals(emailAnnotation.regexp())) {
                // non-default regular expression on @Email annotation should also be considered
                return emailAnnotation.regexp();
            }
        }
        return null;
    }

    /**
     * Determine a number type's minimum (inclusive) value.
     *
     * @param member the field or method to check
     * @return specified inclusive minimum value (or null)
     * @see Min
     * @see DecimalMin
     * @see PositiveOrZero
     */
    protected BigDecimal resolveNumberInclusiveMinimum(MemberScope<?, ?> member) {
        Min minAnnotation = this.getAnnotationFromFieldOrGetter(member, Min.class);
        if (minAnnotation != null) {
            return new BigDecimal(minAnnotation.value());
        }
        DecimalMin decimalMinAnnotation = this.getAnnotationFromFieldOrGetter(member, DecimalMin.class);
        if (decimalMinAnnotation != null && decimalMinAnnotation.inclusive()) {
            return new BigDecimal(decimalMinAnnotation.value());
        }
        PositiveOrZero positiveAnnotation = this.getAnnotationFromFieldOrGetter(member, PositiveOrZero.class);
        if (positiveAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    /**
     * Determine a number type's minimum (exclusive) value.
     *
     * @param member the field or method to check
     * @return specified exclusive minimum value (or null)
     * @see DecimalMin
     * @see Positive
     */
    protected BigDecimal resolveNumberExclusiveMinimum(MemberScope<?, ?> member) {
        DecimalMin decimalMinAnnotation = this.getAnnotationFromFieldOrGetter(member, DecimalMin.class);
        if (decimalMinAnnotation != null && !decimalMinAnnotation.inclusive()) {
            return new BigDecimal(decimalMinAnnotation.value());
        }
        Positive positiveAnnotation = this.getAnnotationFromFieldOrGetter(member, Positive.class);
        if (positiveAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    /**
     * Determine a number type's maximum (inclusive) value.
     *
     * @param member the field or method to check
     * @return specified inclusive maximum value (or null)
     * @see Max
     * @see DecimalMax#inclusive()
     * @see NegativeOrZero
     */
    protected BigDecimal resolveNumberInclusiveMaximum(MemberScope<?, ?> member) {
        Max maxAnnotation = this.getAnnotationFromFieldOrGetter(member, Max.class);
        if (maxAnnotation != null) {
            return new BigDecimal(maxAnnotation.value());
        }
        DecimalMax decimalMaxAnnotation = this.getAnnotationFromFieldOrGetter(member, DecimalMax.class);
        if (decimalMaxAnnotation != null && decimalMaxAnnotation.inclusive()) {
            return new BigDecimal(decimalMaxAnnotation.value());
        }
        NegativeOrZero negativeAnnotation = this.getAnnotationFromFieldOrGetter(member, NegativeOrZero.class);
        if (negativeAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    /**
     * Determine a number type's maximum (exclusive) value.
     *
     * @param member the field or method to check
     * @return specified exclusive maximum value (or null)
     * @see DecimalMax#inclusive()
     * @see Negative
     */
    protected BigDecimal resolveNumberExclusiveMaximum(MemberScope<?, ?> member) {
        DecimalMax decimalMaxAnnotation = this.getAnnotationFromFieldOrGetter(member, DecimalMax.class);
        if (decimalMaxAnnotation != null && !decimalMaxAnnotation.inclusive()) {
            return new BigDecimal(decimalMaxAnnotation.value());
        }
        Negative negativeAnnotation = this.getAnnotationFromFieldOrGetter(member, Negative.class);
        if (negativeAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }
}
