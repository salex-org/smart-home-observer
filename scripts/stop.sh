#!/bin/bash
pid=$(<observer.pid)
kill $pid
echo 'Observer stopped'
