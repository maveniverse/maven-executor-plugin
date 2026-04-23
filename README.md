Maven Executor Plugin
=====================

A **Maven 3 and Maven 4 (native) plugin** to execute Maven builds. The plugin allows you to execute Maven builds from 
within a Maven build, which can be useful for various use cases, such as integration testing.

## Core

Core module defines API and is "Maven free" in a way, it is completely embeddable (uses MIMA under the hood).

## Providers

Providers modules contains various providers like "executors" are.

Executor providers currently supported:
* `executor-maven-executor` uses `maven-executor` of Maven 4, that is capable to run Maven builds using Maven 3 and Maven 4 locally on host in "forked" or "embedded" mode.
* `executor-docker-exer` uses Docker CLI to execute Maven builds using [Docker Maven](https://hub.docker.com/_/maven) images.
* `executor-testcontainers` uses Testcontainers (Docker) to execute Maven builds using [Docker Maven](https://hub.docker.com/_/maven) images.

## Maven 3 Plugin

Maven 3 plugin is a plugin that may be considered as alternative to `maven-invoker-plugin`.

## Maven 4 Plugin

Maven 4 plugin is a plugin that may be considered as alternative to `maven-invoker-plugin` for Maven 4.
