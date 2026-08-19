# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Tesseract OCR + Arabic/French language packs
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-ara \
    tesseract-ocr-fra \
    libopencv-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy tessdata to the path your app.yml expects
RUN mkdir -p /opt/tessdata
RUN cp -r /usr/share/tesseract-ocr/*/tessdata/* /opt/tessdata/ || true

# OpenCV haar cascade for face verification
RUN mkdir -p /opt/opencv
RUN find / -name "haarcascade_frontalface_default.xml" -exec cp {} /opt/opencv/ \; 2>/dev/null || true

# Document storage folder
RUN mkdir -p /var/dgi/documents

COPY --from=build /app/target/immatriculation-backend.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]