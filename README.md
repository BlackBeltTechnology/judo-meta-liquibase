# judo-meta-liquibase

[![Build](https://github.com/BlackBeltTechnology/judo-meta-liquibase/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/BlackBeltTechnology/judo-meta-liquibase/actions/workflows/build.yml)

## Introduction

This repository contains the Liquibase meta model.

It acts as an Eclipse plugin with features and sites. Modules can be used standalone and in standard OSGi (without Eclipse).

This is an extended version of the Liquibase first-party model.

## Context

This project is a building block of the [judo-community](https://github.com/BlackBeltTechnology/judo-community) aggregator
project. In order to better understand how this module fits into our ecosystem, please check the corresponding documentation!

## Validation

This project supports two validation approaches:

1. **EVL Validation** (Epsilon Validation Language) - Traditional Epsilon-based validation using `.evl` files
2. **Java Validation** (Zeta Framework) - Native Java validation with better IDE integration and performance

Both validators run in parallel during tests to ensure validation parity. See the [validation documentation](docs/validation/README.md) for details.

## Contributing to the project

Everyone is welcome to contribute to JUDO! As a starter, please read the corresponding [CONTRIBUTING](CONTRIBUTING.md) guide for details!

## License

This project is licensed under the [Eclipse Public License - v 2.0](https://www.eclipse.org/legal/epl-2.0/).
