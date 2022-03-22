#!/bin/bash
pid=$(<observer.pid)
kill -SIGTERM $pid
echo 'Observer stopped'
