#!/bin/bash

# docker garbage collection
docker images -q -f "dangling=true" | xargs -r docker rmi -f