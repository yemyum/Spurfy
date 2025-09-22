# 1. 자바 17 버전이 설치된 이미지
FROM openjdk:17-jdk-alpine

# 2. 컨테이너 내부에서 작업할 폴더
WORKDIR /app

# 3. 'mvn clean package' 명령어로 만든 JAR 파일을 컨테이너 안으로 복사
#
COPY target/spurfy-0.0.1-SNAPSHOT.jar /app/app.jar

# 4. 컨테이너가 시작될 때 JAR 파일을 실행하는 명령어를 설정
ENTRYPOINT ["java", "-jar", "app.jar"]