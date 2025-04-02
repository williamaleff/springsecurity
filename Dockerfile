FROM openjdk:21-jdk-slim
RUN mkdir /app
RUN mkdir -p /app/uploads
WORKDIR /app

# Copia os arquivos do SDK para dentro do container
COPY sdk /


RUN mkdir -p /usr/local/NITGEN/

# Copia os arquivos do SDK para o container.
# Supondo que sua estrutura local seja "sdk/eNBSP/Lib/NBioBSPJNI.jar" e demais arquivos, isso os coloca em /usr/local/NITGEN/eNBSP/Lib/
COPY sdk/ /usr/local/NITGEN/

# Instala o JAR da SDK no repositório do Maven
RUN apt-get update && apt-get install -y maven

# Opcional: listar os arquivos para verificar se o JAR está no lugar certo
RUN ls -l /usr/local/NITGEN/eNBSP/Lib/

# Registra o JAR da SDK no repositório local do Maven
RUN mvn install:install-file -Dfile=/usr/local/NITGEN/eNBSP/Lib/NBioBSPJNI.jar \
                             -DgroupId=com.nitgen \
                             -DartifactId=NBioBSPJNI \
                             -Dversion=1.0 \
                             -Dpackaging=jar

# Define a variável de ambiente para o linker encontrar as bibliotecas nativas
ENV LD_LIBRARY_PATH=/usr/local/NITGEN/eNBSP/bin

COPY target/*.jar /app/app.jar

CMD ["java", "-Djava.library.path=/usr/local/NITGEN/eNBSP/bin","-jar","/app/app.jar"]