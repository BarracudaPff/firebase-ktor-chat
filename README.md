# Simple chat with firebase and ktor as backend

Ktor + WebSockets + Firebase => backend for simple chat with one room and reactions!

## How to run

1. Create Firebase project at 
2. Download services.json file and place it at `src/jvmMain/resources/firebase-service.json`. 
You can specify custom config path in application.conf[application.conf](src%2FjvmMain%2Fresources%2Fapplication.conf) with `service_file` key.
3. Connect realtime database to your firebase project and fill `database url` with `web key`:
```hocon
ktor {
    ...
    firebase {
        service_file = firebase-service.json
        database_url = ...firebasedatabase.app
        web_key = ...
    }
}
```

- You can get `Web API key` at Project Settings -> General -> Web API key

- You can get `Database base url` at `Realtime Database` -> `Data` -> root node starting with `https://...`

4. Run via main function or with `./gradlew run` 
