#!/usr/bin/env bash

cd "$(dirname "$0")"

for fn in ./*.ipynb;
do
    jupyter nbconvert --config=./render_config.py --execute  $fn --to html;
done;
