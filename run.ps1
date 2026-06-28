$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
$env:JAVA_TOOL_OPTIONS = "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
$env:OPENAI_API_KEY = Read-Host "Digite sua OPENAI_API_KEY"
mvn.cmd spring-boot:run
