#!/bin/sh
cat stub.sh target/homokozo-1.0-SNAPSHOT.jar > "$1" && chmod +x "$1"