# POS – Casa de Marcat (Spring Boot)

## Descriere generală
Acest proiect reprezintă o aplicație backend de tip **POS (Point of Sale)** – casă de marcat – dezvoltată în **Java cu Spring Boot**, având ca scop gestionarea vânzărilor, produselor, clienților, bonurilor fiscale și plăților.

Aplicația implementează un flux complet de vânzare:
- definirea produselor și a categoriilor
- deschiderea unui bon
- adăugarea produselor pe bon
- efectuarea plății
- actualizarea stocului

---

## Cerințe funcționale

1. Gestionarea categoriilor de produse (creare, listare, actualizare, ștergere)
2. Gestionarea produselor, cu preț, stoc și asociere la o categorie
3. Gestionarea promoțiilor aplicabile unuia sau mai multor produse
4. Gestionarea clienților și a vânzătorilor
5. Autentificare și autorizare pe bază de roluri (USER, ADMIN), cu conturi de utilizator asociate vânzătorilor
6. Emiterea și gestionarea bonurilor fiscale: deschidere, adăugare produse pe bon, modificare/ștergere linii, finalizare
7. Procesarea plăților (CASH/CARD) și urmărirea statusului acestora
8. Validarea datelor introduse și tratarea erorilor cu mesaje specifice pentru fiecare operație
9. Paginare și sortare pentru listele de date (produse, clienți, bonuri)
10. Interfață web cu formulare pentru toate operațiile de tip CRUD
11. Înregistrarea evenimentelor aplicației (logging) pentru operațiuni și erori

---

## Business Requirements
Aplicația respectă următoarele cerințe de business:

1. Un produs aparține unei categorii și are preț și stoc.
2. Un client poate avea mai multe bonuri.
3. Un bon este emis de un singur vânzător.
4. Un bon poate conține mai multe produse (cu cantitate).
5. Un produs poate apărea pe mai multe bonuri.
6. Stocul produselor se reduce la adăugarea pe bon.
7. Un bon poate fi plătit o singură dată.
8. Plata poate fi CASH sau CARD.
9. Un bon are status (OPEN / PAID).
10. Toate operațiile invalide sunt blocate prin validări și excepții.

---

## Flow principal (Vânzare)

1. Se creează categorii și produse
2. Se creează client și vânzător
3. Se creează un bon fiscal (status = OPEN)
4. Se adaugă produse pe bon  
   → stocul se reduce automat
5. Se efectuează plata bonului  
   → bonul devine PAID  
   → se creează o plată
6. Se pot lista detaliile bonului și plățile aferente

---

## Diagrama ER
Diagrama Entitate–Relație descrie structura bazei de date și relațiile dintre entități.

![ERD Diagram](ERD_POS.png)

### Tipuri de relații

- **`@OneToOne`**: `Vanzator` ↔ `Utilizator` (fiecare vânzător are cel mult un cont de logare)
- **`@ManyToOne` / `@OneToMany`**: `Categorie` → `Produs`, `Client` → `Bon`, `Vanzator` → `Bon`, `Bon` → `BonProdus`, `Produs` → `BonProdus`, `Bon` → `Plata`
- **`@ManyToMany`**: `Produs` ↔ `Promotie`, prin tabelul asociativ `promotie_produse` (un produs poate fi în mai multe promoții, o promoție poate acoperi mai multe produse)

---

## Entități principale
- **Categorie**
- **Produs**
- **Client**
- **Vanzator**
- **Utilizator**
- **Bon**
- **BonProdus** (entitate de legătură)
- **Plata**
- **Promotie**

---

## REST API – Endpoint-uri

### Categorii
- `POST /api/categorii`
- `GET /api/categorii`
- `GET /api/categorii/{id}`
- `PUT /api/categorii/{id}`
- `DELETE /api/categorii/{id}`

### Produse
- `POST /api/produse`
- `GET /api/produse`
- `GET /api/produse/{id}`
- `GET /api/produse/categorie/{categorieId}`
- `PUT /api/produse/{id}`
- `PUT /api/produse/{id}/stoc`
- `DELETE /api/produse/{id}`

### Clienți
- `POST /api/clients`
- `GET /api/clients`
- `GET /api/clients/{id}`
- `PUT /api/clients/{id}`
- `DELETE /api/clients/{id}`

### Vânzători
- `POST /api/vanzatori`
- `GET /api/vanzatori`
- `GET /api/vanzatori/{id}`
- `PUT /api/vanzatori/{id}`
- `DELETE /api/vanzatori/{id}`

### Utilizatori
- `POST /api/utilizatori`
- `GET /api/utilizatori`
- `GET /api/utilizatori/{id}`
- `PUT /api/utilizatori/{id}`
- `DELETE /api/utilizatori/{id}`

### Promoții
- `POST /api/promotii`
- `GET /api/promotii`
- `GET /api/promotii/{id}`
- `PUT /api/promotii/{id}`
- `DELETE /api/promotii/{id}`
- `POST /api/promotii/{id}/produse/{produsId}`
- `DELETE /api/promotii/{id}/produse/{produsId}`

### Bonuri
- `POST /api/bons`
- `GET /api/bons`
- `GET /api/bons/{bonId}`
- `PUT /api/bons/{bonId}`
- `DELETE /api/bons/{bonId}`
- `POST /api/bons/{bonId}/produse`
- `PUT /api/bons/{bonId}/produse/{bonProdusId}`
- `DELETE /api/bons/{bonId}/produse/{bonProdusId}`
- `POST /api/bons/{bonId}/pay`
- `GET /api/bons/{bonId}/plati`
- `GET /api/bons/{bonId}/plati/{plataId}`
- `PUT /api/bons/{bonId}/plati/{plataId}`
- `DELETE /api/bons/{bonId}/plati/{plataId}`

---

## Views (Thymeleaf)

Interfata web (separata de API-ul REST) e disponibila sub `/web/...`:

- `/` - pagina principala
- `/web/categorii`, `/web/produse`, `/web/clienti`, `/web/vanzatori`, `/web/utilizatori`, `/web/promotii` - lista + creare + editare + stergere pentru fiecare entitate
- `/web/bonuri` - lista bonurilor; `/web/bonuri/new` - deschide bon nou; `/web/bonuri/{id}` - pagina de detaliu cu tot fluxul de vanzare (adaugare/editare/stergere produse pe bon, plata, listare/stergere plati)

Validare server-side (Bean Validation, afisata in formular) + client-side (atribute HTML5) pe toate formularele. Erorile de business (ex. stoc insuficient) apar ca alerta pe pagina, fara sa treaca prin pagina de eroare. Pagini de eroare custom pentru 404 si 500 in `templates/error/`.

---

## Paginare si sortare

Implementat cu `Pageable` (Spring Data) pentru listele de **Produs**, **Client** si **Bon** (`/web/produse`, `/web/clienti`, `/web/bonuri`):

- sortare dupa minim 2 criterii per entitate: Produs (nume/pret), Client (nume/email), Bon (data/status) - linkuri clickable pe antetul coloanelor
- navigare intre pagini (Anterior/Urmator + numere de pagina) cu Bootstrap pagination
- dimensiune pagina configurabila (5/10/20) dintr-un selector care reincarca lista
- parametri URL: `page`, `size`, `sort`, `dir`

---

## Logging

Framework: SLF4J + Logback, configurat in `logback-spring.xml`.

- nivel `INFO` implicit (root), nivel `DEBUG` specific pentru pachetul `ro.facultate.pos`
- `logs/pos-app.log` - toate log-urile aplicatiei (INFO/DEBUG/ERROR)
- `logs/pos-error.log` - fisier separat, filtrat strict la nivel `ERROR`
- serviciile folosesc `INFO` la operatii reusite (creare/actualizare/stergere) si respingeri de business (ex. "stoc insuficient"), `DEBUG` la pasi intermediari (ex. calculul stocului/totalului)
- un `@ControllerAdvice` (`GlobalExceptionHandler`, scopat doar la `/api/...`) prinde orice exceptie neasteptata din API, o logheaza la nivel `ERROR` cu stack trace, si raspunde cu 500 - fara sa afecteze paginile de eroare custom din Views

---

## Validări

### Validări structurale (`@Valid`)
Aplicate pe DTO-uri:
- `@NotNull`
- `@NotBlank`
- `@Email`
- `@Positive`
- `@PositiveOrZero`

Acestea generează automat **400 Bad Request**.

### Validări de business
Implementate în servicii:
- produs inexistent
- categorie inexistentă
- stoc insuficient
- bon deja plătit
- bon inexistent

Acestea generează **400 / 404**, în funcție de caz.

---

## Excepții
Aplicația folosește:
- excepții custom de business
- `try/catch` în service
- transformarea excepțiilor în `ResponseStatusException`

Pentru claritate, mesajele de eroare sunt returnate către client.

---

## Configurare Multi-Environment

Aplicația are 2 profiluri Spring, fiecare cu baza lui de date:

- **`dev`** (profil implicit) — PostgreSQL local, configurat în `application-dev.yml`
- **`test`** — H2 in-memory, configurat în `application-test.yml`, cu `ddl-auto: create-drop` (schema se recreează la fiecare rulare, fără sa fie nevoie de un server pornit separat)

Profilul activ e setat implicit în `application.properties` (`spring.profiles.active=dev`). Testele automate folosesc profilul `test` prin `@ActiveProfiles("test")`.

---

## Testare

### Tipuri de teste implementate
- **Unit tests** (JUnit 5 + Mockito) pentru toate serviciile
- **Controller tests** (`@WebMvcTest`) pentru toate endpoint-urile REST
- **Integration tests** (`@SpringBootTest` + `MockMvc`, profil `test`, bază de date H2 reală, nu mock-uri) — 3 scenarii end-to-end:
  - flux complet de vânzare (categorie → produs → client → vânzător → bon → adăugare produs → plată)
  - stoc insuficient (verifică blocarea operației și starea stocului în baza de date)
  - ștergere blocată din cauza dependențelor (categorie cu produse asociate)

### Code coverage
- Măsurat cu JaCoCo, impus ca prag minim de build (`mvn verify`)
- Prag minim: 70% line coverage pe pachetul `service`
- Coverage curent: peste 90%

### Acoperire
- toate endpoint-urile REST
- toate serviciile
- cazuri pozitive (success)
- cazuri negative (400 / 404 / business rules)

---

## Spring Security

Autentificare si autorizare bazate pe roluri, implementate cu Spring Security.

### Autentificare
- `UserDetailsService` custom (`UtilizatorDetailsService`), care citeste contul din tabela `utilizatori` (nu schema JDBC implicita din Spring Security)
- Parolele sunt criptate cu `BCryptPasswordEncoder` la creare/actualizare (`UtilizatorService`); nu se mai salveaza in clar
- Contul mapeaza campul `activ` pe starea `disabled` a userului (un cont dezactivat nu se mai poate autentifica) si `rol` (`USER`/`ADMIN`) pe autoritatea `ROLE_USER`/`ROLE_ADMIN`
- Pagina de login este custom, la `/login`, cu formular Thymeleaf (username, parola, "tine-ma minte")
- Suport simultan pentru form login (pentru interfata web) si HTTP Basic (pentru clienti API/Swagger)
- La primul start al aplicatiei, daca tabela `utilizatori` este goala, se genereaza automat un cont ADMIN implicit (`admin` / `admin123`, `AdminSeeder`) astfel incat aplicatia sa fie utilizabila din prima fara acces direct la baza de date

### Autorizare pe rol
- `/web/bonuri/**` (Casierie) - accesibil pentru `USER` si `ADMIN`
- restul paginilor sub `/web/**` (Administrare: categorii, produse, clienti, vanzatori, utilizatori, promotii) - doar `ADMIN`
- `/api/**` - orice utilizator autentificat (USER sau ADMIN)
- `/`, `/login`, paginile de eroare si Swagger UI - publice

### CSRF, logout, remember-me
- protectie CSRF activa pentru `/web/**`, dezactivata pentru `/api/**` (folosit de clienti API care nu au sesiune de browser)
- token-ul CSRF este injectat automat de Thymeleaf in toate formularele existente (`th:action`), fara modificari suplimentare in template-uri
- logout functional (`POST /logout`), buton disponibil in bara de navigare pe toate paginile, invalideaza sesiunea si cookie-ul de remember-me
- remember-me disponibil ca opțiune la login (cookie valabil 14 zile)

### Testare
- toate cele 15 clase `@WebMvcTest` existente ruleaza cu `@AutoConfigureMockMvc(addFilters = false)` (testeaza doar logica MVC, nu filtrele de securitate)
- `SalesFlowIntegrationTest` (test de integrare end-to-end pe API) ruleaza cu `@WithMockUser(roles = "ADMIN")`
- `SecurityIntegrationTest` - teste dedicate, pe context Spring Boot complet, cu utilizatori reali salvati cu parola criptata: acces anonim la pagini protejate (redirect la login), acces anonim la API (401), rol gresit pe pagina de administrare (403), rol corect (200), login cu credentiale corecte/incorecte/inexistente, logout

> **Notă (Partea II):** sectiunea de mai sus descrie Spring Security asa cum a fost implementat in monolit (Partea I). Dupa spargerea in microservicii (vezi sectiunea "Arhitectura microservicii" de mai jos), Security ramane deliberat doar in `user-service` pentru sub-proiectul curent, scopat la ce serveste efectiv acest modul (`/web/utilizatori/**`, `/api/**`); `catalog-service` si `sales-service` nu au Security in acest sub-proiect. Autentificarea distribuita intre toate serviciile (JWT emis de Gateway) e planificata intr-un sub-proiect ulterior.

---

## Arhitectura microservicii (Partea II)

Aplicatia a fost impartita in 4 microservicii independente, organizate ca monorepo Maven (un POM parent + module).

| Serviciu | Port | Entitati proprii | Views Thymeleaf | Baza de date |
|---|---|---|---|---|
| **catalog-service** | 8081 | Categorie, Produs, Promotie | `/web/categorii`, `/web/produse`, `/web/promotii` | Postgres propriu (`catalog_db`) |
| **sales-service** | 8082 | Client, Vanzator, Bon, BonProdus, Plata | `/web/bonuri`, `/web/clienti`, `/web/vanzatori` | Postgres propriu (`sales_db`) |
| **user-service** | 8083 | Utilizator + Spring Security | `/web/utilizatori`, `/login` | Postgres propriu (`user_db`) |
| **notification-service** | 8084 | (schelet, fara logica inca) | - | - |
| **eureka-server** | 8761 | - | - | - |
| **config-server** | 8888 | - | - | - |

Fiecare serviciu are baza de date proprie si comunica cu celelalte exclusiv prin REST (Spring Cloud OpenFeign), fara acces direct la baza de date a altui serviciu.

### Comunicare intre servicii
- **Sales -> Catalog**: la adaugarea unui produs pe bon, Sales rezerva/decrementeaza stocul printr-un apel real catre Catalog (`POST /api/produse/{id}/ajusteaza-stoc`) inainte de a salva linia local - implementare de baza a pattern-ului **Saga**: daca salvarea locala esueaza dupa rezervarea stocului, Sales compenseaza restaurand stocul in Catalog.
- **Catalog -> Sales**: inainte de a permite stergerea unui produs, Catalog verifica prin Sales (`GET /api/bons/produse/{id}/pe-bon`) daca produsul apare pe vreun bon.
- **User -> Sales**: la creare cont, User verifica prin Sales (`GET /api/vanzatori/{id}`) ca vanzatorul exista.
- **Sales -> User**: inainte de a permite stergerea unui vanzator, Sales verifica prin User (`GET /api/utilizatori/by-vanzator/{id}`) daca vanzatorul are deja un cont asociat.

Toate cele 4 fluxuri de mai sus au fost verificate manual, live, cu cele 3 servicii ruland simultan pe porturile proprii. Feign Client-urile rezolva adresa celuilalt serviciu prin nume (`sales-service`, `catalog-service`, `user-service`), nu prin URL hardcodat - rezolvarea se face prin Eureka (vezi sectiunea de mai jos).

### Service discovery (Eureka)
- `eureka-server` (port 8761) - registry central; cele 3 servicii de business se inregistreaza automat la pornire (`spring-cloud-starter-netflix-eureka-client`) si isi reinnoiesc periodic inregistrarea (heartbeat)
- Feign Client-urile (`@FeignClient(name = "sales-service")`, fara atributul `url`) rezolva adresa reala prin Eureka + Spring Cloud LoadBalancer, nu prin configurare statica
- Dashboard-ul Eureka (`http://localhost:8761`) arata cele 3 servicii cu status `UP`
- In profilul `test`, `eureka.client.enabled=false` - testele nu depind de un Eureka pornit, raman hermetice si rapide

### Configurare centralizata (Spring Cloud Config)
- `config-server` (port 8888) - serveste configurarea pentru profilul `dev` a celor 3 servicii dintr-un backend `native` (fisiere locale in `config-server/src/main/resources/config-repo/`, cate un fisier per serviciu, numit exact ca `spring.application.name`)
- Contine configurarile sensibile centralizate (datasource: URL/user/parola Postgres) - eliminate din `application-dev.yml`-urile locale ale fiecarui serviciu
- Fiecare serviciu importa configurarea la pornire prin `spring.config.import=optional:configserver:http://localhost:8888` (prefixul `optional:` face ca serviciul sa porneasca normal si daca Config Server nu e disponibil, folosind orice configurare locala ramasa)
- **Refresh dinamic fara restart**: fiecare serviciu expune `POST /actuator/refresh`; orice bean adnotat `@RefreshScope` isi reincarca valorile din Config Server la apelul acestui endpoint, fara sa fie nevoie de restart. Demonstrat concret in `catalog-service` cu `ConfigDemoController` (`GET /api/config-demo`, proprietatea `app.mesaj-bun-venit`): se modifica valoarea in config-repo, se apeleaza `/actuator/refresh`, si endpoint-ul raspunde imediat cu noua valoare, fara restart.

### Schimbari de model de date fata de monolit
Relatiile JPA care traversau granita noii separari pe servicii nu mai pot fi relatii `@ManyToOne`/`@OneToOne` (baze de date diferite):
- `BonProdus.produs` (Sales) -> `produsId: Long` + `produsNume: String` (denormalizat la creare, la fel ca `pretUnitar`, pentru a evita un apel Feign doar pentru afisare)
- `Utilizator.vanzator` (User) -> `vanzatorId: Long`, validat prin apel REST la Sales

### Rulare locala
Necesita 3 baze Postgres create in avans:
```sql
CREATE DATABASE catalog_db;
CREATE DATABASE sales_db;
CREATE DATABASE user_db;
```

Pornire (5 terminale separate, din radacina monorepo-ului) - **Eureka si Config Server primele**, apoi serviciile de business:
```bash
./mvnw -pl eureka-server spring-boot:run
./mvnw -pl config-server spring-boot:run
./mvnw -pl catalog-service spring-boot:run
./mvnw -pl sales-service spring-boot:run
./mvnw -pl user-service spring-boot:run
```

La primul start, `user-service` creeaza automat contul ADMIN implicit (`admin`/`admin123`), inclusiv vanzatorul asociat, printr-un apel real catre `sales-service`.

### In afara scopului acestor sub-proiecte
API Gateway, load balancing cu instante multiple, securitate distribuita (JWT), Resilience4j, mesagerie (RabbitMQ) si continutul efectiv al `notification-service` sunt planificate in sub-proiecte ulterioare.

