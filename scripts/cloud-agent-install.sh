#!/usr/bin/env bash
# Idempotent Cloud Agent bootstrap for the YINILOW2 workspace.
# Prepares the React/Vite frontend and the Vert.x backend so both can run locally.
set -euo pipefail

MAVEN_VERSION="3.9.9"
MAVEN_HOME="/opt/apache-maven-${MAVEN_VERSION}"

# The default base image ships Node 22 and a JDK but no Maven, and the
# repository's backend/Dockerfile pins Maven 3.9 (whose default compiler plugin
# honors maven.compiler.release=17). Match that here; the apt package is 3.8.x
# and silently downgrades the compiler to source/target 5.
current_mvn_version="$(mvn -version 2>/dev/null | sed -n '1s/Apache Maven \([0-9.]*\).*/\1/p' || true)"
if [ "${current_mvn_version}" != "${MAVEN_VERSION}" ]; then
  echo "Installing Apache Maven ${MAVEN_VERSION}"
  tmp_tarball="$(mktemp --suffix=.tar.gz)"
  curl -fsSL -o "${tmp_tarball}" \
    "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
  sudo rm -rf "${MAVEN_HOME}"
  sudo tar -xzf "${tmp_tarball}" -C /opt
  sudo ln -sfn "${MAVEN_HOME}" /opt/maven
  sudo ln -sfn /opt/maven/bin/mvn /usr/local/bin/mvn
  rm -f "${tmp_tarball}"
fi
mvn -version

# Frontend dependencies.
npm install

# Prebuild the backend uber-jar so the api terminal starts quickly and the
# Maven dependency cache is warmed into ~/.m2.
( cd backend && mvn -B -DskipTests package )

echo "Cloud Agent bootstrap complete."
