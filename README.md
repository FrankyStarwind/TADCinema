# TADCinema
Proyecto de la asignatura Tecnologias Avanzadas de Desarrollo creada por Francisco Bustamante y Carlos Muñoz

## Instrucciones para el despliegue

1. Ejecutar el siguiente comando para clonar el repositorio a nuestro entorno.
> **git clone https://github.com/FrankyStarwind/TADCinema.git**

2. Ejecutamos el siguiente comando para instalar la imagen de Mongo en Docker.
> **sudo docker pull mongo**

3. Ejecutamos el siguiente comando para instalar la imagen de Maven en Docker.
> **sudo docker pull maven**

4. Una vez realizados todos los pasos anteriores, tendremos ya disponible las imágenes requeridas para el despliegue de la aplicación en Docker. Una vez con ambas imágenes, podemos ejecutar el siguiente comando:
> **sudo docker-compose up**

Crea los 2 contededores para su ejecución y debe ejecutarse siempre en el directorio donde se encuentra el proyecto (la carpeta raíz donde está el docker-compose.yml)

Dicho comando se utilizará para poder seguir los logs y poder ver cuando está lista la aplicación. Una vez realizado, ya se podrá acceder a la aplicación mediante **localhost** y el puerto **8080**.

5. Para finalizar el proceso usamos el siguiente comando, el cual eliminará los contenedores creados anteriormente:
> **sudo docker-compose down**

## docker-compose.yml

A continuación, se muestra el contenido del archivo **docker-compose.yml**

```
version: "3"

services:
    mongo:
        image: mongo
        container_name: mongodb
        volumes:
            # Montamos un volumen para MongoDB para no perder los datos de la bd
            - $HOME/mongo:/data/db
        command: --serviceExecutor adaptive

    tadcinema:
        image: maven
        container_name: tadcinema
        restart: always
        volumes:
            - .:/TADCinema
        working_dir: /TADCinema
        expose:
            - "8080"
        ports:
            - "8080:8080"
        links:
            - mongo
        # Comando para la ejecución de la aplicación
        command: mvn jetty:run
```
