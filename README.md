# 🚀 Word of the Day API (Spring Boot)

This is a simple Spring Boot project that calls the **[Random Word API](https://random-word-api.herokuapp.com/word)** to fetch random words.

---

## 📦 Requirements

* **Java 17+** (or 21 if you use the latest LTS)
* **Maven 3.8+**

---

## ⚙️ Build the Project

From the project root (where `pom.xml` is located), run:

```bash
mvn clean package
```

This will generate a JAR file in the `target/` directory:

```
target/word-of-the-day-api-0.0.1-SNAPSHOT.jar
```

---

## ▶️ Run the Project

Run the generated JAR with:

```bash
java -jar target/word-of-the-day-api-0.0.1-SNAPSHOT.jar
```

By default, the app will start on **[http://localhost:8080](http://localhost:8080)**.

---

## 🌐 Endpoints

### Fetch Random Words

```http
GET /random-words?number=5
```

#### Example Request:

```bash
curl "http://localhost:8080/"
```

#### Example Response:

```json
["apple","river","school","music","predecessor"]
```

---

## ⚡ Run on a Custom Port

You can override the port when running the JAR:

```bash
java -jar target/word-of-the-day-api-0.0.1-SNAPSHOT.jar --server.port=9090
```

Now the app will run at [http://localhost:9090](http://localhost:9090).

---

## 📂 Project Structure

```
src/main/java/com/mycompany/wordofthedayapi/
│
├── WordOfTheDayApiApplication.java
├── config/WebClientConfig.java
├── controller/WordController.java
└── service/WordService.java

src/main/resources/
└── application.properties
```

---

## 📝 License

This project is for educational purposes and is free to use.

---

## 👨‍💻 Author

**Shuaib Bin Reza**
