# Java JSON Schema Generator – Module javax.validation
[![Build Status](https://travis-ci.org/victools/jsonschema-module-javax-validation.svg?branch=master)](https://travis-ci.org/victools/jsonschema-module-javax-validation)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.victools/jsonschema-module-javax-validation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.victools/jsonschema-module-javax-validation)

Module for the `jsonschema-generator` – deriving JSON Schema attributes from `javax.validation.constraints` annotations.

## Features
1. Determine whether a member is not nullable, base assumption being that all fields and method return values are nullable if not annotated. Based on `@NotNull`/`@Null`/`@NotEmpty`/`@NotBlank`
2. Populate "minItems" and "maxItems" for containers (i.e. arrays and collections). Based on `@Size`/`@NotEmpty`
3. Populate "minLength" and "maxLength" for strings. Based on `@Size`/`@NotEmpty`/`@NotBlank`
4. Populate "minimum"/"exclusiveMinimum" for numbers. Based on `@Min`/`@DecimalMin`/`@Positive`/`@PositiveOrZero`
5. Populate "maximum"/"exclusiveMaximum" for numbers. Based on `@Max`/`@DecimalMax`/`@Negative`/`@NegativeOrZero`

Schema attributes derived from validation annotations on fields are also applied to their respective getter methods.
Schema attributes derived from validation annotations on getter methods are also applied to their associated fields.

## Usage
### Dependency (Maven)
```xml
<dependency>
    <groupId>com.github.victools</groupId>
    <artifactId>jsonschema-module-javax-validation</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Code
#### Passing into SchemaGeneratorConfigBuilder.with(Module)
```java
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationModule;
```
```java
JavaxValidationModule module = new JavaxValidationModule();
SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(objectMapper)
    .with(module);
```

#### Complete Example
```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationModule;
```
```java
ObjectMapper objectMapper = new ObjectMapper();
JavaxValidationModule module = new JavaxValidationModule();
SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(objectMapper, OptionPreset.PLAIN_JSON)
    .with(module);
SchemaGeneratorConfig config = configBuilder.build();
SchemaGenerator generator = new SchemaGenerator(config);
JsonNode jsonSchema = generator.generateSchema(YourClass.class);

System.out.println(jsonSchema.toString());
```
