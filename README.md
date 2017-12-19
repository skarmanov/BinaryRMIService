# BinaryRMIService
Тестовое задание

# Требования к окружению
Для запуска необходим установленный JDK 1.8

# Команда для сборки приложения
gradlew.bat clean build

или для Unix

gradlew clean build

# Отчет по результатам автотестов, в файле
build/reports/tests/test/index.html

# Команда для запуска приложения
java -classpath "brmi-server/build/dependent-libs/*;brmi-server/build/libs/*" ru.ksa.brmi.server.MyRmiServerApplication <port number>
