# Temel imaj olarak hafif bir Java 21 (Alpine) sürümü kullanıyoruz
FROM eclipse-temurin:21-jdk-alpine

# Geçici dosyalar için bir volume oluşturuyoruz
VOLUME /tmp

# Bilgisayarında (target klasöründe) derlenen .jar dosyasını konteynere kopyalıyoruz
COPY target/*.jar app.jar

# Uygulamayı başlatma komutu
ENTRYPOINT ["java","-jar","/app.jar"]