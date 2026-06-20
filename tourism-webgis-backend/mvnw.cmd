@ECHO OFF
SETLOCAL
SET "BASE_DIR=%~dp0"
SET "BASE_DIR=%BASE_DIR:~0,-1%"
SET "WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar"

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Maven Wrapper JAR is missing: %WRAPPER_JAR%
  ECHO Run build.bat from the project root or restore the wrapper file.
  EXIT /B 1
)

IF DEFINED JAVA_HOME (
  SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
EXIT /B %ERRORLEVEL%
