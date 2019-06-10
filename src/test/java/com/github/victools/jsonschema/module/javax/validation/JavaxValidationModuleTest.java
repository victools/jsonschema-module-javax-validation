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

import com.github.victools.jsonschema.generator.ConfigFunction;
import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.MethodScope;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigPart;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
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
    private SchemaGeneratorConfigPart<FieldScope> fieldConfigPart;
    private SchemaGeneratorConfigPart<MethodScope> methodConfigPart;

    @Before
    public void setUp() {
        this.configBuilder = Mockito.mock(SchemaGeneratorConfigBuilder.class);
        this.fieldConfigPart = Mockito.spy(new SchemaGeneratorConfigPart<>());
        this.methodConfigPart = Mockito.spy(new SchemaGeneratorConfigPart<>());
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
            {"notNullOnGetterNumber", Boolean.FALSE},
            {"notEmptyList", Boolean.FALSE},
            {"notEmptyOnGetterList", Boolean.FALSE},
            {"notBlankString", Boolean.FALSE},
            {"notBlankOnGetterString", Boolean.FALSE},
            {"nullField", Boolean.TRUE},
            {"nullGetter", Boolean.TRUE}
        };
    }

    @Test
    @Parameters(method = "parametersForTestNullableCheck")
    public void testNullableCheckOnField(String fieldName, Boolean expectedResult) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        ArgumentCaptor<ConfigFunction<FieldScope, Boolean>> captor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withNullableCheck(captor.capture());
        TestType testType = new TestType(TestClassForNullableCheck.class);
        FieldScope field = testType.getMemberField(fieldName);

        Boolean result = captor.getValue().apply(field);
        Assert.assertEquals(expectedResult, result);
    }

    @Test
    @Parameters(method = "parametersForTestNullableCheck")
    public void testNullableCheckOnMethod(String fieldName, Boolean expectedResult) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        ArgumentCaptor<ConfigFunction<MethodScope, Boolean>> captor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.methodConfigPart).withNullableCheck(captor.capture());
        TestType testType = new TestType(TestClassForNullableCheck.class);
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        MethodScope method = testType.getMemberMethod(methodName);

        Boolean result = captor.getValue().apply(method);
        Assert.assertEquals(expectedResult, result);
    }

    Object parametersForTestArrayItemCountResolvers() {
        return new Object[][]{
            {"unannotatedArray", null, null},
            {"sizeTenToTwentyString", null, null},
            {"sizeTenToTwentyOnGetterString", null, null},
            {"minSizeFiveArray", 5, null},
            {"minSizeFiveOnGetterArray", 5, null},
            {"maxSizeFiftyArray", null, 50},
            {"maxSizeFiftyOnGetterArray", null, 50},
            {"sizeTenToTwentySet", 10, 20},
            {"sizeTenToTwentyOnGetterSet", 10, 20},
            {"nonEmptyMaxSizeHundredList", 1, 100},
            {"nonEmptyMaxSizeHundredOnGetterList", 1, 100}
        };
    }

    @Test
    @Parameters
    public void testArrayItemCountResolvers(String fieldName, Integer expectedMinItems, Integer expectedMaxItems) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        TestType testType = new TestType(TestClassForArrayItemCount.class);
        FieldScope field = testType.getMemberField(fieldName);

        ArgumentCaptor<ConfigFunction<FieldScope, Integer>> minItemCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withArrayMinItemsResolver(minItemCaptor.capture());
        Integer minItemCount = minItemCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMinItems, minItemCount);

        ArgumentCaptor<ConfigFunction<FieldScope, Integer>> maxItemCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withArrayMaxItemsResolver(maxItemCaptor.capture());
        Integer maxItemCount = maxItemCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMaxItems, maxItemCount);
    }

    Object parametersForTestStringLengthResolvers() {
        return new Object[][]{
            {"unannotatedString", null, null},
            {"sizeTenToTwentyArray", null, null},
            {"sizeTenToTwentyOnGetterArray", null, null},
            {"minSizeFiveSequence", 5, null},
            {"minSizeFiveOnGetterSequence", 5, null},
            {"maxSizeFiftyString", null, 50},
            {"maxSizeFiftyOnGetterString", null, 50},
            {"sizeTenToTwentyString", 10, 20},
            {"sizeTenToTwentyOnGetterString", 10, 20},
            {"nonEmptyMaxSizeHundredString", 1, 100},
            {"nonEmptyMaxSizeHundredOnGetterString", 1, 100},
            {"nonBlankString", 1, null},
            {"nonBlankOnGetterString", 1, null}
        };
    }

    @Test
    @Parameters
    public void testStringLengthResolvers(String fieldName, Integer expectedMinLength, Integer expectedMaxLength) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        TestType testType = new TestType(TestClassForStringLength.class);
        FieldScope field = testType.getMemberField(fieldName);

        ArgumentCaptor<ConfigFunction<FieldScope, Integer>> minLengthCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withStringMinLengthResolver(minLengthCaptor.capture());
        Integer minLength = minLengthCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMinLength, minLength);

        ArgumentCaptor<ConfigFunction<FieldScope, Integer>> maxLengthCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withStringMaxLengthResolver(maxLengthCaptor.capture());
        Integer maxLength = maxLengthCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMaxLength, maxLength);
    }

    Object parametersForTestNumberMinMaxResolvers() {
        return new Object[][]{
            {"unannotatedInt", null, null, null, null},
            {"minMinusHundredLong", "-100", null, null, null},
            {"minMinusHundredOnGetterLong", "-100", null, null, null},
            {"maxFiftyShort", null, null, "50", null},
            {"maxFiftyOnGetterShort", null, null, "50", null},
            {"tenToTwentyInclusiveInteger", "10.1", null, "20.2", null},
            {"tenToTwentyInclusiveOnGetterInteger", "10.1", null, "20.2", null},
            {"tenToTwentyExclusiveInteger", null, "10.1", null, "20.2"},
            {"tenToTwentyExclusiveOnGetterInteger", null, "10.1", null, "20.2"},
            {"positiveByte", null, BigDecimal.ZERO, null, null},
            {"positiveOnGetterByte", null, BigDecimal.ZERO, null, null},
            {"positiveOrZeroBigInteger", BigDecimal.ZERO, null, null, null},
            {"positiveOrZeroOnGetterBigInteger", BigDecimal.ZERO, null, null, null},
            {"negativeDecimal", null, null, null, BigDecimal.ZERO},
            {"negativeOnGetterDecimal", null, null, null, BigDecimal.ZERO},
            {"negativeOrZeroLong", null, null, BigDecimal.ZERO, null},
            {"negativeOrZeroOnGetterLong", null, null, BigDecimal.ZERO, null}
        };
    }

    @Test
    @Parameters
    public void testNumberMinMaxResolvers(String fieldName, BigDecimal expectedMinInclusive, BigDecimal expectedMinExclusive,
            BigDecimal expectedMaxInclusive, BigDecimal expectedMaxExclusive) throws Exception {
        new JavaxValidationModule().applyToConfigBuilder(this.configBuilder);

        TestType testType = new TestType(TestClassForNumberMinMax.class);
        FieldScope field = testType.getMemberField(fieldName);

        ArgumentCaptor<ConfigFunction<FieldScope, BigDecimal>> minInclusiveCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberInclusiveMinimumResolver(minInclusiveCaptor.capture());
        BigDecimal minInclusive = minInclusiveCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMinInclusive, minInclusive);

        ArgumentCaptor<ConfigFunction<FieldScope, BigDecimal>> minExclusiveCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberExclusiveMinimumResolver(minExclusiveCaptor.capture());
        BigDecimal minExclusive = minExclusiveCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMinExclusive, minExclusive);

        ArgumentCaptor<ConfigFunction<FieldScope, BigDecimal>> maxInclusiveCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberInclusiveMaximumResolver(maxInclusiveCaptor.capture());
        BigDecimal maxInclusive = maxInclusiveCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMaxInclusive, maxInclusive);

        ArgumentCaptor<ConfigFunction<FieldScope, BigDecimal>> maxExclusiveCaptor = ArgumentCaptor.forClass(ConfigFunction.class);
        Mockito.verify(this.fieldConfigPart).withNumberExclusiveMaximumResolver(maxExclusiveCaptor.capture());
        BigDecimal maxExclusive = maxExclusiveCaptor.getValue().apply(field);
        Assert.assertEquals(expectedMaxExclusive, maxExclusive);
    }

    private static class TestClassForNullableCheck {

        Integer unannotatedField;
        @NotNull
        Double notNullNumber;
        Double notNullOnGetterNumber;
        @NotEmpty
        List<Object> notEmptyList;
        List<Object> notEmptyOnGetterList;
        @NotBlank
        String notBlankString;
        String notBlankOnGetterString;
        @Null
        Object nullField;
        Object nullGetter;

        public Integer getUnannotatedField() {
            return this.unannotatedField;
        }

        public Double getNotNullNumber() {
            return this.notNullNumber;
        }

        @NotNull
        public Double getNotNullOnGetterNumber() {
            return this.notNullOnGetterNumber;
        }

        public List<Object> getNotEmptyList() {
            return this.notEmptyList;
        }

        @NotEmpty
        public List<Object> getNotEmptyOnGetterList() {
            return this.notEmptyOnGetterList;
        }

        public String getNotBlankString() {
            return this.notBlankString;
        }

        @NotBlank
        public String getNotBlankOnGetterString() {
            return this.notBlankOnGetterString;
        }

        public Object getNullField() {
            return this.nullField;
        }

        @Null
        public Object getNullGetter() {
            return this.nullGetter;
        }
    }

    private static class TestClassForArrayItemCount {

        String[] unannotatedArray;
        @Size(min = 10, max = 20)
        String sizeTenToTwentyString;
        String sizeTenToTwentyOnGetterString;
        @Size(min = 5)
        int[] minSizeFiveArray;
        int[] minSizeFiveOnGetterArray;
        @Size(max = 50)
        long[] maxSizeFiftyArray;
        long[] maxSizeFiftyOnGetterArray;
        @Size(min = 10, max = 20)
        Set<Boolean> sizeTenToTwentySet;
        Set<Boolean> sizeTenToTwentyOnGetterSet;
        @NotEmpty
        @Size(max = 100)
        List<Double> nonEmptyMaxSizeHundredList;
        List<Double> nonEmptyMaxSizeHundredOnGetterList;

        @Size(min = 10, max = 20)
        public String getSizeTenToTwentyString() {
            return this.sizeTenToTwentyString;
        }

        @Size(min = 5)
        public int[] getMinSizeFiveOnGetterArray() {
            return this.minSizeFiveOnGetterArray;
        }

        @Size(max = 50)
        public long[] getMaxSizeFiftyOnGetterArray() {
            return this.maxSizeFiftyOnGetterArray;
        }

        @Size(min = 10, max = 20)
        public Set<Boolean> getSizeTenToTwentyOnGetterSet() {
            return this.sizeTenToTwentyOnGetterSet;
        }

        @NotEmpty
        @Size(max = 100)
        public List<Double> getNonEmptyMaxSizeHundredOnGetterList() {
            return this.nonEmptyMaxSizeHundredOnGetterList;
        }
    }

    private static class TestClassForStringLength {

        String unannotatedString;
        @Size(min = 10, max = 20)
        int[] sizeTenToTwentyArray;
        int[] sizeTenToTwentyOnGetterArray;
        @Size(min = 5)
        CharSequence minSizeFiveSequence;
        CharSequence minSizeFiveOnGetterSequence;
        @Size(max = 50)
        String maxSizeFiftyString;
        String maxSizeFiftyOnGetterString;
        @Size(min = 10, max = 20)
        String sizeTenToTwentyString;
        String sizeTenToTwentyOnGetterString;
        @NotEmpty
        @Size(max = 100)
        String nonEmptyMaxSizeHundredString;
        String nonEmptyMaxSizeHundredOnGetterString;
        @NotBlank
        String nonBlankString;
        String nonBlankOnGetterString;

        @Size(min = 10, max = 20)
        public int[] getSizeTenToTwentyOnGetterArray() {
            return this.sizeTenToTwentyOnGetterArray;
        }

        @Size(min = 5)
        public CharSequence getMinSizeFiveOnGetterSequence() {
            return this.minSizeFiveOnGetterSequence;
        }

        @Size(max = 50)
        public String getMaxSizeFiftyOnGetterString() {
            return this.maxSizeFiftyOnGetterString;
        }

        @Size(min = 10, max = 20)
        public String getSizeTenToTwentyOnGetterString() {
            return this.sizeTenToTwentyOnGetterString;
        }

        @NotEmpty
        @Size(max = 100)
        public String getNonEmptyMaxSizeHundredOnGetterString() {
            return this.nonEmptyMaxSizeHundredOnGetterString;
        }

        @NotBlank
        public String getNonBlankOnGetterString() {
            return this.nonBlankOnGetterString;
        }
    }

    private static class TestClassForNumberMinMax {

        int unannotatedInt;
        @Min(-100L)
        long minMinusHundredLong;
        long minMinusHundredOnGetterLong;
        @Max(50)
        short maxFiftyShort;
        short maxFiftyOnGetterShort;
        @DecimalMin("10.1")
        @DecimalMax("20.2")
        Integer tenToTwentyInclusiveInteger;
        Integer tenToTwentyInclusiveOnGetterInteger;
        @DecimalMin(value = "10.1", inclusive = false)
        @DecimalMax(value = "20.2", inclusive = false)
        Integer tenToTwentyExclusiveInteger;
        Integer tenToTwentyExclusiveOnGetterInteger;
        @Positive
        byte positiveByte;
        byte positiveOnGetterByte;
        @PositiveOrZero
        BigInteger positiveOrZeroBigInteger;
        BigInteger positiveOrZeroOnGetterBigInteger;
        @Negative
        BigDecimal negativeDecimal;
        BigDecimal negativeOnGetterDecimal;
        @NegativeOrZero
        Long negativeOrZeroLong;
        Long negativeOrZeroOnGetterLong;

        @Min(-100L)
        public long getMinMinusHundredOnGetterLong() {
            return minMinusHundredOnGetterLong;
        }

        @Max(50)
        public short getMaxFiftyOnGetterShort() {
            return maxFiftyOnGetterShort;
        }

        @DecimalMin("10.1")
        @DecimalMax("20.2")
        public Integer getTenToTwentyInclusiveOnGetterInteger() {
            return tenToTwentyInclusiveOnGetterInteger;
        }

        @DecimalMin(value = "10.1", inclusive = false)
        @DecimalMax(value = "20.2", inclusive = false)
        public Integer getTenToTwentyExclusiveOnGetterInteger() {
            return tenToTwentyExclusiveOnGetterInteger;
        }

        @Positive
        public byte getPositiveOnGetterByte() {
            return positiveOnGetterByte;
        }

        @PositiveOrZero
        public BigInteger getPositiveOrZeroOnGetterBigInteger() {
            return positiveOrZeroOnGetterBigInteger;
        }

        @Negative
        public BigDecimal getNegativeOnGetterDecimal() {
            return negativeOnGetterDecimal;
        }

        @NegativeOrZero
        public Long getNegativeOrZeroOnGetterLong() {
            return negativeOrZeroOnGetterLong;
        }
    }
}
