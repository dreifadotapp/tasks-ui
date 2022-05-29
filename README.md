# The "Tasks-UI"

[![Circle CI](https://circleci.com/gh/dreifadotapp/tasks-ui.svg?style=shield)](https://circleci.com/gh/dreifadotapp/tasks-ui)
[![Licence Status](https://img.shields.io/github/license/dreifadotapp/tasks-ui)](https://github.com/dreifadotapp/tasks-ui/blob/master/licence.txt)

## What it does

A basic UI for viewing and running [Tasks](https://github.com/dreifadotapp/tasks).

This is built using classic "server side" MVC pattern using the [Htt4k](https://www.http4k.org/)
toolkit, the [Spectre](https://picturepan2.github.io/spectre/index.html) CSS Framework and
the [Mustache](https://github.com/spullara/mustache.java) template engine.

The implementation is simple. There is minimal unit testing or error handing, nor is there a well-defined URL pattern.

## Running from the command line

_todo_

## Running under Docker

To build and push an image

```bash
./buildDocker.sh 
./pushImage.sh
```

To run using docker compose

```bash
docker compose up 
```

This starts the [UI](http://localhost:8080) and [Jaeger](http://localhost:16686/)