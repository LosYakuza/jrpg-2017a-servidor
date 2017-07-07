travis:
[![Build Status](https://travis-ci.org/LosYakuza/jrpg-2017a-servidor.svg?branch=master)](https://travis-ci.org/LosYakuza/jrpg-2017a-servidor)

jenkins:
[![Build Status](https://jpas.com.ar/jenkins/buildStatus/icon?job=unlam-jrpg-server)](https://jpas.com.ar/jenkins/job/unlam-jrpg-server/)

# World Of Middle Earth (WOME) - servidor

## Sobre las personas

> Para ver los participantes anteriores, referirse al archivo [humans.md](humans.md)

### Docentes del Taller

* Leonardo Blautzik ([leoblautzik](https://github.com/leoblautzik))
* Julio Crispino ([jmcrispino](https://github.com/jmcrispino))
* Lucas Videla ([delucas](https://github.com/delucas))

### Integrantes del equipo

* Micaela Ramirez ([mramirez96](https://github.com/mramirez96))
* Leandro Ibaceta ([libaceta](https://github.com/libaceta))
* Juan Arana ([aranajuan](https://github.com/aranajuan))

## Sobre la tecnología

### Herramientas utilizadas

* Java 1.8
* JUnit 4

## Cómo hacer andar el proyecto

1. Clonar los 3 proyectos
* ([dominio](https://github.com/LosYakuza/jrpg-2017a-dominio))
* ([cliente](https://github.com/LosYakuza/jrpg-2017a-cliente))
* ([servidor](https://github.com/LosYakuza/jrpg-2017a-servidor))

2. Importar en eclise como Gradle Project
([plugin eclipse](https://projects.eclipse.org/projects/tools.buildship))

3. Ejecutar dominio

4. Copiar /build/libs/jrpg-dominio-0.1.jar al cliente y servidor

5. Ejecutar cliente

6. Copiar /build/libs/jrpg-cliente-0.1.jar al servidor

7. Ejecutar servidor

