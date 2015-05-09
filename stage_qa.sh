#!/bin/bash
activator clean dist
scp /Users/ldaniels/git/shocktrade/shocktrade-js/target/universal/shocktrade-js-*.zip dev802:~/
