#!/bin/bash
activator $@ dist
scp app-play/target/universal/shocktrade-js-*.zip dev802:~/
