## Release Instructions (don't use release plugin and bypass staging repo)


Check readme.sh

``` bash
release_version=1.0.0
new_version=1.0.1

./mvnw versions:set -DnewVersion=${release_version}
./mvnw clean deploy -P release-sign-artifacts
./mvnw versions:set -DnewVersion=${new_version}-SNAPSHOT
```
