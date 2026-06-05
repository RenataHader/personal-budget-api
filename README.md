# Personal Budget API

REST API do zarządzania budżetem osobistym. Aplikacja pozwala tworzyć konta, dodawać przychody i wydatki, automatycznie aktualizować saldo konta oraz pobierać podsumowanie finansowe.

## Technologie

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* PostgreSQL
* Flyway
* Bean Validation
* H2 do testów
* Docker / Docker Compose
* Maven

## Funkcjonalności

* zarządzanie kontami,
* dodawanie i usuwanie transakcji,
* automatyczna aktualizacja salda konta,
* filtrowanie transakcji po dacie i kategorii,
* podsumowanie przychodów i wydatków,
* walidacja danych wejściowych,
* obsługa błędów HTTP,
* testy integracyjne.

## Uruchomienie aplikacji

### Docker Compose

Najprostszy sposób uruchomienia aplikacji razem z PostgreSQL:

```bash
docker compose up --build
```

Aplikacja będzie dostępna pod adresem:

```text
http://localhost:8080
```

### Lokalnie

Najpierw uruchom bazę danych:

```bash
docker compose up -d db
```

Następnie uruchom aplikację:

```powershell
.\mvnw.cmd spring-boot:run
```

Na Linux/macOS:

```bash
./mvnw spring-boot:run
```

## Testy

Testy korzystają z bazy H2 w pamięci, więc nie wymagają uruchomionego PostgreSQL.

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

## Endpointy

### Konta

| Metoda   | Endpoint         | Opis                                     |
| -------- | ---------------- | ---------------------------------------- |
| `GET`    | `/accounts`      | lista kont                               |
| `POST`   | `/accounts`      | utworzenie konta                         |
| `GET`    | `/accounts/{id}` | szczegóły konta                          |
| `DELETE` | `/accounts/{id}` | usunięcie konta, jeśli nie ma transakcji |

Przykład utworzenia konta:

```json
{
  "name": "Konto główne"
}
```

### Transakcje

| Metoda   | Endpoint                                                        | Opis                 |
| -------- | --------------------------------------------------------------- | -------------------- |
| `GET`    | `/transactions`                                                 | lista transakcji     |
| `GET`    | `/transactions?from=2024-03-01&to=2024-03-31&category=Jedzenie` | lista z filtrami     |
| `POST`   | `/transactions`                                                 | dodanie transakcji   |
| `DELETE` | `/transactions/{id}`                                            | usunięcie transakcji |

Przykład dodania przychodu:

```json
{
  "amount": 1000,
  "type": "INCOME",
  "category": "Wynagrodzenie",
  "description": "Monthly salary",
  "transactionDate": "2024-03-01",
  "accountId": 1
}
```

Przykład dodania wydatku:

```json
{
  "amount": 200,
  "type": "EXPENSE",
  "category": "Jedzenie",
  "description": "Zakupy",
  "transactionDate": "2024-03-02",
  "accountId": 1
}
```

### Podsumowanie

| Metoda | Endpoint   | Opis                   |
| ------ | ---------- | ---------------------- |
| `GET`  | `/summary` | podsumowanie finansowe |

Przykładowa odpowiedź:

```json
{
  "totalIncome": 1000,
  "totalExpenses": 350,
  "expensesByCategory": {
    "Jedzenie": 250,
    "Transport": 100
  }
}
```

## Obsługa błędów

Obsługiwane kody HTTP:

* `200 OK`
* `201 Created`
* `204 No Content`
* `400 Bad Request`
* `404 Not Found`
* `409 Conflict`

## Struktura projektu

```text
src/main/java/com/example/budget
├── account
├── transaction
├── summary
└── error
```

## Baza danych

Schemat bazy danych jest tworzony przez Flyway.

Migracja znajduje się w:

```text
src/main/resources/db/migration/V1__init.sql
```

Aplikacja używa tabel:

* `accounts`
* `transactions`

## Decyzje projektowe

* Logika kont, transakcji i podsumowania została rozdzielona na osobne pakiety.
* Aktualizacja salda odbywa się w `TransactionService`.
* Operacje dodawania i usuwania transakcji są oznaczone jako `@Transactional`.
* Błędy są obsługiwane globalnie przez `GlobalExceptionHandler`.
* Testy integracyjne korzystają z profilu `test` i bazy H2.
