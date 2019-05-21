# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## v1.0.0 – 2019-05-22
### Added
- Indicate a field/method to be nullable if `@Null` is present
- Indicate a field/method to be not nullable if `@NotNull`, `@NotEmpty` or `@NotBlank` is present
- Indicate an array's "minItems" according to `@Size` or `@NotEmpty`
- Indicate an array's "maxItems" according to `@Size`
- Indicate a string's "minLength" according to `@Size`, `@NotEmpty` or `@NotBlank`
- Indicate a string's "maxLength" according to `@Size`
- Indicate a number's "minimum" (inclusive) according to `@Min`, `@DecimalMin` or `@PositiveOrZero`
- Indicate a number's "exclusiveMinimum" according to `@DecimalMin` or `@Positive`
- Indicate a number's "maximum" (inclusive) according to `@Max`, `@DecimalMax` or `@NegativeOrZero`
- Indicate a number's "exclusiveMaximum" according to `@DecimalMax` or `@Negative`

[Unreleased]: https://github.com/victools/jsonschema-module-javax-validation/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/victools/jsonschema-module-javax-validation/releases/tag/v1.0.0
