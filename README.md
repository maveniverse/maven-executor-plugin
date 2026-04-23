Maven Executor Suite
====================

A **Maven 3 and Maven 4 (native) suite** to execute Maven builds. The plugin allows you to execute Maven builds from 
within a Maven build, which can be useful for various use cases, such as integration testing.

The main goal of this project is to provide a flexible and extensible framework for executing Maven builds, with 
support for both Maven 3 and Maven 4. Another major goal is to provide "sensible defaults" for executing Maven builds, 
like offer **complete isolation from the user or host OS environment**, something that "vanilla" Maven plugins like
Surefire and Invoker do not provide.

## Core

Core module defines API and is "Maven free" in a way, it is completely embeddable (uses MIMA under the hood).

## Providers

Providers modules contains various providers like "executors" are.

Executor providers currently supported:
* `executor-maven-executor` uses `maven-executor` of Maven 4, that is capable to run Maven builds using locally installed Maven 3 and Maven 4 on host in "forked" or "embedded" mode.
* `executor-docker-exe` uses Docker CLI to execute Maven builds using [Docker Maven](https://hub.docker.com/_/maven) images; requires Docker installed.
* `executor-testcontainers` uses Testcontainers (Docker) to execute Maven builds using [Docker Maven](https://hub.docker.com/_/maven) images; requires Docker installed.

## Maven 3 Plugin

Maven 3 plugin is a plugin that may be considered as alternative to `maven-invoker-plugin`.

## Maven 4 Plugin

Maven 4 plugin is a plugin that may be considered as alternative to `maven-invoker-plugin` for Maven 4.
