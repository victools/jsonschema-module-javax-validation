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
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigPart;
import com.github.victools.jsonschema.generator.TypeVariableContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
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
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Test for the {@link JavaxValidationModule}.
 */
@RunWith(JUnitParamsRunner.class)
public class JavaxValidationModuleTest {

    private SchemaGeneratorConfigBuilder configBuilder;
    private SchemaGeneratorConfigPart<Field> fieldConfigPart;
    private SchemaGeneratorConfigPart<Method> methodConfigPart;

    @Before
    public void setUp() {
        this.configBuilder = Mockito.mock(SchemaGeneratorConfigBuilder.class);
        this.fieldConfigPart = Mockito.mock(SchemaGeneratorConfigPart.class);
        this.methodConfigPart = Mockito.mock(SchemaGeneratorConfigPart.class);
        Mockito.when(this.configBuilder.forFields()).thenReturn(this.fieldConfigPart);
        Mockito.when(this.configBuilder.forMethods()).thenReturn(this.methodConfigPart);
    }

    @Test
    public void testApplyToConfigBuilder() {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        Mockito.verify(this.configBuilder).forFields();
        Mockito.verify(this.configBuilder).forMethods();

        Mockito.verify(this.fieldConfigPart).withNullableCheck(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withArrayMinItemsResolver(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withArrayMaxItemsResolver(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withStringMinLengthResolver(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withStringMaxLengthResolver(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withNumberInclusiveMinimumResolver(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withNumberExclusiveMinimumResolver(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withNumberInclusiveMaximumResolver(Mockito.any());
        Mockito.verify(this.fieldConfigPart).withNumberExclusiveMaximumResolver(Mockito.any());

        Mockito.verify(this.methodConfigPart).withNullableCheck(Mockito.any());
        Mockito.verify(this.methodConfigPart).withArrayMinItemsResolver(Mockito.any());
        Mockito.verify(this.methodConfigPart).withArrayMaxItemsResolver(Mockito.any());
        Mockito.verify(this.methodConfigPart).withStringMinLengthResolver(Mockito.any());
        Mockito.verify(this.methodConfigPart).withStringMaxLengthResolver(Mockito.any());
        Mockito.verify(this.methodConfigPart).withNumberInclusiveMinimumResolver(Mockito.any());
        Mockito.verify(this.methodConfigPart).withNumberExclusiveMinimumResolver(Mockito.any());
        Mockito.verify(this.methodConfigPart).withNumberInclusiveMaximumResolver(Mockito.any());
        Mockito.verify(this.methodConfigPart).withNumberExclusiveMaximumResolver(Mockito.any());

        Mockito.verifyNoMoreInteractions(this.configBuilder, this.fieldConfigPart, this.methodConfigPart);
    }

    Object parametersForTestNullableCheck() {
        return new Object[][]{
            {"unannotatedField", null},
            {"notNullNumber", Boolean.FALSE},
            {"notEmptyList", Boolean.FALSE},
            {"notBlankString", Boolean.FALSE},
            {"nullField", Boolean.TRUE}
        };
    }

    @Test
    @Parameters
    public void testNullableCheck(String fieldName, Boolean expectedResult) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        ArgumentCaptor<BiFunction<Field, JavaType, Boolean>> captor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withNullableCheck(captor.capture());

        Boolean result = captor.getValue().apply(TestClassForNullableCheck.class.getDeclaredField(fieldName), null);
        Assert.assertEquals(expectedResult, result);
    }

    Object parametersForTestArrayItemCountResolvers() {
        return new Object[][]{
            {"unannotatedArray", null, null},
            {"sizeTenToTwentyString", null, null},
            {"minSizeFiveArray", 5, null},
            {"maxSizeFiftyArray", null, 50},
            {"sizeTenToTwentySet", 10, 20},
            {"nonEmptyMaxSizeHundredList", 1, 100}
        };
    }

    @Test
    @Parameters
    public void testArrayItemCountResolvers(String fieldName, Integer expectedMinItems, Integer expectedMaxItems) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        Field field = TestClassForArrayItemCount.class.getDeclaredField(fieldName);
        JavaType fieldType = new JavaType(field.getGenericType(), TypeVariableContext.EMPTY_SCOPE);

        ArgumentCaptor<BiFunction<Field, JavaType, Integer>> minItemCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withArrayMinItemsResolver(minItemCaptor.capture());
        Integer minItemCount = minItemCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMinItems, minItemCount);

        ArgumentCaptor<BiFunction<Field, JavaType, Integer>> maxItemCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withArrayMaxItemsResolver(maxItemCaptor.capture());
        Integer maxItemCount = maxItemCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMaxItems, maxItemCount);
    }

    Object parametersForTestStringLengthResolvers() {
        return new Object[][]{
            {"unannotatedString", null, null},
            {"sizeTenToTwentyArray", null, null},
            {"minSizeFiveSequence", 5, null},
            {"maxSizeFiftyString", null, 50},
            {"sizeTenToTwentyString", 10, 20},
            {"nonEmptyMaxSizeHundredString", 1, 100},
            {"nonBlankString", 1, null}
        };
    }

    @Test
    @Parameters
    public void testStringLengthResolvers(String fieldName, Integer expectedMinLength, Integer expectedMaxLength) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        Field field = TestClassForStringLength.class.getDeclaredField(fieldName);
        JavaType fieldType = new JavaType(field.getGenericType(), TypeVariableContext.EMPTY_SCOPE);

        ArgumentCaptor<BiFunction<Field, JavaType, Integer>> minLengthCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withStringMinLengthResolver(minLengthCaptor.capture());
        Integer minLength = minLengthCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMinLength, minLength);

        ArgumentCaptor<BiFunction<Field, JavaType, Integer>> maxLengthCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withStringMaxLengthResolver(maxLengthCaptor.capture());
        Integer maxLength = maxLengthCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMaxLength, maxLength);
    }

    Object parametersForTestNumberMinMaxResolvers() {
        return new Object[][]{
            {"unannotatedInt", null, null, null, null},
            {"minMinusHundredLong", "-100", null, null, null},
            {"maxFiftyShort", null, null, "50", null},
            {"tenToTwentyInclusiveInteger", "10.1", null, "20.2", null},
            {"tenToTwentyExclusiveInteger", null, "10.1", null, "20.2"},
            {"positiveByte", null, BigDecimal.ZERO, null, null},
            {"positiveOrZeroBigInteger", BigDecimal.ZERO, null, null, null},
            {"negativeDecimal", null, null, null, BigDecimal.ZERO},
            {"negativeOrZeroLong", null, null, BigDecimal.ZERO, null}
        };
    }

    @Test
    @Parameters
    public void testNumberMinMaxResolvers(String fieldName, BigDecimal expectedMinInclusive, BigDecimal expectedMinExclusive,
            BigDecimal expectedMaxInclusive, BigDecimal expectedMaxExclusive) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        Field field = TestClassForNumberMinMax.class.getDeclaredField(fieldName);
        JavaType fieldType = new JavaType(field.getGenericType(), TypeVariableContext.EMPTY_SCOPE);

        ArgumentCaptor<BiFunction<Field, JavaType, BigDecimal>> minInclusiveCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberInclusiveMinimumResolver(minInclusiveCaptor.capture());
        BigDecimal minInclusive = minInclusiveCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMinInclusive, minInclusive);

        ArgumentCaptor<BiFunction<Field, JavaType, BigDecimal>> minExclusiveCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberExclusiveMinimumResolver(minExclusiveCaptor.capture());
        BigDecimal minExclusive = minExclusiveCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMinExclusive, minExclusive);

        ArgumentCaptor<BiFunction<Field, JavaType, BigDecimal>> maxInclusiveCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberInclusiveMaximumResolver(maxInclusiveCaptor.capture());
        BigDecimal maxInclusive = maxInclusiveCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMaxInclusive, maxInclusive);

        ArgumentCaptor<BiFunction<Field, JavaType, BigDecimal>> maxExclusiveCaptor = ArgumentCaptor.forClass(BiFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberExclusiveMaximumResolver(maxExclusiveCaptor.capture());
        BigDecimal maxExclusive = maxExclusiveCaptor.getValue().apply(field, fieldType);
        Assert.assertEquals(expectedMaxExclusive, maxExclusive);
    }

    private static class TestClassForNullableCheck {

        Integer unannotatedField;
        @NotNull
        Double notNullNumber;
        @NotEmpty
        List<Object> notEmptyList;
        @NotBlank
        String notBlankString;
        @Null
        Object nullField;
        @Size(min = 5)
        int[] minSizeFiveArray;
        @Size(max = 50)
        long[] maxSizeFiftyArray;
        @Size(min = 10, max = 20)
        Set<Boolean> sizeTenToTwentySet;
    }

    private static class TestClassForArrayItemCount {

        String[] unannotatedArray;
        @Size(min = 10, max = 20)
        String sizeTenToTwentyString;
        @Size(min = 5)
        int[] minSizeFiveArray;
        @Size(max = 50)
        long[] maxSizeFiftyArray;
        @Size(min = 10, max = 20)
        Set<Boolean> sizeTenToTwentySet;
        @NotEmpty
        @Size(max = 100)
        List<Double> nonEmptyMaxSizeHundredList;
    }

    private static class TestClassForStringLength {

        String unannotatedString;
        @Size(min = 10, max = 20)
        int[] sizeTenToTwentyArray;
        @Size(min = 5)
        CharSequence minSizeFiveSequence;
        @Size(max = 50)
        String maxSizeFiftyString;
        @Size(min = 10, max = 20)
        String sizeTenToTwentyString;
        @NotEmpty
        @Size(max = 100)
        String nonEmptyMaxSizeHundredString;
        @NotBlank
        String nonBlankString;
    }

    private static class TestClassForNumberMinMax {

        int unannotatedInt;
        @Min(-100L)
        long minMinusHundredLong;
        @Max(50)
        short maxFiftyShort;
        @DecimalMin("10.1")
        @DecimalMax("20.2")
        Integer tenToTwentyInclusiveInteger;
        @DecimalMin(value = "10.1", inclusive = false)
        @DecimalMax(value = "20.2", inclusive = false)
        Integer tenToTwentyExclusiveInteger;
        @Positive
        byte positiveByte;
        @PositiveOrZero
        BigInteger positiveOrZeroBigInteger;
        @Negative
        BigDecimal negativeDecimal;
        @NegativeOrZero
        Long negativeOrZeroLong;
    }
}
