# Usa Windows Server Core como base
FROM mcr.microsoft.com/windows/servercore:ltsc2022

# Baixa e instala o OpenJDK 21 manualmente
RUN powershell -Command "& { \
    Invoke-WebRequest -Uri 'https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip' -OutFile 'C:\openjdk.zip'; \
    Expand-Archive -Path 'C:\openjdk.zip' -DestinationPath 'C:\openjdk'; \
    Remove-Item -Path 'C:\openjdk.zip'; \
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\openjdk\jdk-21', [System.EnvironmentVariableTarget]::Machine); \
    [System.Environment]::SetEnvironmentVariable('PATH', $Env:PATH + ';C:\openjdk\jdk-21\bin', [System.EnvironmentVariableTarget]::Machine) \
}"

# Cria diretórios necessários
RUN mkdir C:\app
RUN mkdir C:\app\uploads
WORKDIR C:\app

# Copia os arquivos do SDK para o container
COPY sdk/ C:\sdk\

# Instala o Maven manualmente
RUN powershell -Command "& { \
    Invoke-WebRequest -Uri 'https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile 'C:\maven.zip'; \
    Expand-Archive -Path 'C:\maven.zip' -DestinationPath 'C:\maven'; \
    Remove-Item -Path 'C:\maven.zip'; \
    [System.Environment]::SetEnvironmentVariable('PATH', $Env:PATH + ';C:\maven\apache-maven-3.9.6\bin', [System.EnvironmentVariableTarget]::Machine) \
}"

# Registra o JAR da SDK no repositório local do Maven
RUN powershell -Command "& { \
    C:\maven\apache-maven-3.9.6\bin\mvn install:install-file -Dfile=C:\sdk\NBioBSPJNI.jar -DgroupId=com.nitgen -DartifactId=NBioBSPJNI -Dversion=1.0 -Dpackaging=jar \
}"

# Define a variável de ambiente para o linker encontrar as bibliotecas nativas
ENV PATH="C:\sdk\bin;${PATH}"

# Copia o JAR da aplicação
COPY target/*.jar C:\app\app.jar

# Comando de execução da aplicação
CMD ["java", "-Djava.library.path=C:\\sdk\\bin", "-jar", "C:\\app\\app.jar"]
