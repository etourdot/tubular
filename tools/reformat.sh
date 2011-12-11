# !/bin/sh

find . -name \*.java | xargs sed -i '/{$/{N; N; s/{\n\+/{\n/}'
find . -name \*.java | xargs sed -i '/$/{N; N; s/\n\n\(\t*\)\}/\n\1}/}'
