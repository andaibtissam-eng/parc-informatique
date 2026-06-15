Ce projet utilise un wrapper Maven pragmatique (`mvnw` / `mvnw.cmd`) qui :

1. exécute `mvn` si Maven est installé localement,
2. sinon bascule sur un conteneur `maven:3.9.9-eclipse-temurin-17` si Docker est disponible.

Cette approche permet de travailler sur la machine actuelle sans Maven global.
