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

import com.github.victools.jsonschema.generator.JavaType;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigPart;
import com.github.victools.jsonschema.generator.impl.ReflectionTypeUtils;
import java.lang.reflect.AccessibleObject;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * With this module, the base assumption for the nullable indication is that all fields and methods are nullable unless annotated otherwise.
 */
public class JavaxValidationModule implements Module {

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        this.applyToConfigPart(builder.forFields());
        this.applyToConfigPart(builder.forMethods());
    }

    /**
     * Apply the various annotation-based resolvers for the given configuration part (this expected to be executed for both fields and methods).
     *
     * @param configPart config builder part to add configurations to
     */
    private void applyToConfigPart(SchemaGeneratorConfigPart<? extends AccessibleObject> configPart) {
        configPart.withNullableCheck(this::isNullable);
        configPart.withArrayMinItemsResolver(this::resolveArrayMinItems);
        configPart.withArrayMaxItemsResolver(this::resolveArrayMaxItems);
        configPart.withStringMinLengthResolver(this::resolveStringMinLength);
        configPart.withStringMaxLengthResolver(this::resolveStringMaxLength);
        configPart.withNumberInclusiveMinimumResolver(this::resolveNumberInclusiveMinimum);
        configPart.withNumberExclusiveMinimumResolver(this::resolveNumberExclusiveMinimum);
        configPart.withNumberInclusiveMaximumResolver(this::resolveNumberInclusiveMaximum);
        configPart.withNumberExclusiveMaximumResolver(this::resolveNumberExclusiveMaximum);
    }

    /**
     * Determine whether a given field or method is annotated to be not nullable.
     *
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return whether the field/method is specifically annotated as nullable or not (returns null if not specified: assumption it is nullable then)
     */
    private Boolean isNullable(AccessibleObject fieldOrMethod, JavaType type) {
        Boolean result;
        if (fieldOrMethod.isAnnotationPresent(NotNull.class)
                || fieldOrMethod.isAnnotationPresent(NotBlank.class)
                || fieldOrMethod.isAnnotationPresent(NotEmpty.class)) {
            // field is specifically NOT nullable
            result = Boolean.FALSE;
        } else if (fieldOrMethod.isAnnotationPresent(Null.class)) {
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
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified minimum number of array items (or null)
     * @see Size
     */
    private Integer resolveArrayMinItems(AccessibleObject fieldOrMethod, JavaType type) {
        if (ReflectionTypeUtils.isArrayType(type)) {
            Size sizeAnnotation = fieldOrMethod.getAnnotation(Size.class);
            if (sizeAnnotation != null && sizeAnnotation.min() > 0) {
                // minimum length greater than the default 0 was specified
                return sizeAnnotation.min();
            }
            if (fieldOrMethod.isAnnotationPresent(NotEmpty.class)) {
                return 1;
            }
        }
        return null;
    }

    /**
     * Determine a given array type's maximum number of items.
     *
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified maximum number of array items (or null)
     * @see Size
     */
    private Integer resolveArrayMaxItems(AccessibleObject fieldOrMethod, JavaType type) {
        if (ReflectionTypeUtils.isArrayType(type)) {
            Size sizeAnnotation = fieldOrMethod.getAnnotation(Size.class);
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
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified minimum number of characters (or null)
     * @see Size
     * @see NotEmpty
     * @see NotBlank
     */
    private Integer resolveStringMinLength(AccessibleObject fieldOrMethod, JavaType type) {
        Class<?> rawType = ReflectionTypeUtils.getRawType(type.getResolvedType());
        if (CharSequence.class.isAssignableFrom(rawType)) {
            Size sizeAnnotation = fieldOrMethod.getAnnotation(Size.class);
            if (sizeAnnotation != null && sizeAnnotation.min() > 0) {
                // minimum length greater than the default 0 was specified
                return sizeAnnotation.min();
            }
            if (fieldOrMethod.isAnnotationPresent(NotEmpty.class)
                    || fieldOrMethod.isAnnotationPresent(NotBlank.class)) {
                return 1;
            }
        }
        return null;
    }

    /**
     * Determine a given text type's maximum number of characters.
     *
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified minimum number of characters (or null)
     * @see Size
     */
    private Integer resolveStringMaxLength(AccessibleObject fieldOrMethod, JavaType type) {
        Class<?> rawType = ReflectionTypeUtils.getRawType(type.getResolvedType());
        if (CharSequence.class.isAssignableFrom(rawType)) {
            Size sizeAnnotation = fieldOrMethod.getAnnotation(Size.class);
            if (sizeAnnotation != null && sizeAnnotation.max() < 2147483647) {
                // maximum length below the default 2147483647 was specified
                return sizeAnnotation.max();
            }
        }
        return null;
    }

    /**
     * Determine a number type's minimum (inclusive) value.
     *
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified inclusive minimum value (or null)
     * @see Min
     * @see DecimalMin
     * @see PositiveOrZero
     */
    private BigDecimal resolveNumberInclusiveMinimum(AccessibleObject fieldOrMethod, JavaType type) {
        Min minAnnotation = fieldOrMethod.getAnnotation(Min.class);
        if (minAnnotation != null) {
            return new BigDecimal(minAnnotation.value());
        }
        DecimalMin decimalMinAnnotation = fieldOrMethod.getAnnotation(DecimalMin.class);
        if (decimalMinAnnotation != null && decimalMinAnnotation.inclusive()) {
            return new BigDecimal(decimalMinAnnotation.value());
        }
        PositiveOrZero positiveAnnotation = fieldOrMethod.getAnnotation(PositiveOrZero.class);
        if (positiveAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    /**
     * Determine a number type's minimum (exclusive) value.
     *
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified exclusive minimum value (or null)
     * @see DecimalMin
     * @see Positive
     */
    private BigDecimal resolveNumberExclusiveMinimum(AccessibleObject fieldOrMethod, JavaType type) {
        DecimalMin decimalMinAnnotation = fieldOrMethod.getAnnotation(DecimalMin.class);
        if (decimalMinAnnotation != null && !decimalMinAnnotation.inclusive()) {
            return new BigDecimal(decimalMinAnnotation.value());
        }
        Positive positiveAnnotation = fieldOrMethod.getAnnotation(Positive.class);
        if (positiveAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    /**
     * Determine a number type's maximum (inclusive) value.
     *
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified inclusive maximum value (or null)
     * @see Max
     * @see DecimalMax#inclusive()
     * @see NegativeOrZero
     */
    private BigDecimal resolveNumberInclusiveMaximum(AccessibleObject fieldOrMethod, JavaType type) {
        Max maxAnnotation = fieldOrMethod.getAnnotation(Max.class);
        if (maxAnnotation != null) {
            return new BigDecimal(maxAnnotation.value());
        }
        DecimalMax decimalMaxAnnotation = fieldOrMethod.getAnnotation(DecimalMax.class);
        if (decimalMaxAnnotation != null && decimalMaxAnnotation.inclusive()) {
            return new BigDecimal(decimalMaxAnnotation.value());
        }
        NegativeOrZero negativeAnnotation = fieldOrMethod.getAnnotation(NegativeOrZero.class);
        if (negativeAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    /**
     * Determine a number type's maximum (exclusive) value.
     *
     * @param fieldOrMethod the field or method to check
     * @param type field's or method return value's type
     * @return specified exclusive maximum value (or null)
     * @see DecimalMax#inclusive()
     * @see Negative
     */
    private BigDecimal resolveNumberExclusiveMaximum(AccessibleObject fieldOrMethod, JavaType type) {
        DecimalMax decimalMaxAnnotation = fieldOrMethod.getAnnotation(DecimalMax.class);
        if (decimalMaxAnnotation != null && !decimalMaxAnnotation.inclusive()) {
            return new BigDecimal(decimalMaxAnnotation.value());
        }
        Negative negativeAnnotation = fieldOrMethod.getAnnotation(Negative.class);
        if (negativeAnnotation != null) {
            return BigDecimal.ZERO;
        }
        return null;
    }
}
