# Copilot Instructions for Company Ecosystem Team Projects

## General Guidelines

-   **Model Configuration**:
    -   Default model: `Claude Sonnet 4`
-   **Conditional Sections**:
    -   Use the **Testing** section exclusively when writing tests.
    -   Omit the **Testing** section entirely for regular (non-test) requests to minimize context size and token usage.
-   **Language**:
    -   Write generated code and comments only in English.

## Language and Coding Style

### Modern Java

-   **Java Version**: Use Java 21 or the current project version
-   **Modern Features**: Apply Stream API, Optional, record classes, and other modern Java features
-   **Avoid deprecated**: Do not use deprecated methods and approaches
-   **Preffer immutable objects**: Use `final` for fields and method parameters where applicable

### Code Style

-   **Indentation**: 4 spaces
-   **Naming**:
    -   Classes and methods: CamelCase (`EntityService`, `getEntityById`)
    -   Constants: UPPER_SNAKE_CASE (`DATA_SOURCE_BEAN`, `JDBC_TEMPLATE_BEAN`)
    -   Packages: lowercase with dots
    -   Repositories: `Default*Repository` for implementations (e.g., `DefaultEntityRepository`)
    -   Services: `*Service` for business logic
    -   Controllers: `*Controller` for REST endpoints
    -   Entities: Simple names without suffixes (e.g., `Entity`, `DomainObject`)
    -   DTOs: Auto-generated with the `Dto` suffix
-   **Formatting**: Use spaces instead of tabs

### Spring Boot Conventions

-   **Configuration Properties**: Use `@ConfigurationPropertiesScan` for settings
-   **Configuration**: Define using `@Bean` methods and `@ConfigurationProperties`

## Context and Dependencies

### Technology Stack

-   **Spring Boot**: (project-dependent)
-   **Java**: 21
-   **Gradle**: 8.8+ with version catalogs
-   **Databases**: PostgreSQL (primary), domain-specific databases
-   **Database Access**: Spring JDBC
-   **Migrations**: Flyway
-   **Code Generation**: Lombok

### Standard Libraries

-   **Spring Boot Starters**: web, actuator, jdbc, validation, test
-   **Monitoring**: Micrometer for Prometheus metrics
-   **Testing**: JUnit 5, Mockito, AssertJ, Testcontainers
-   **Utilities**: Lombok (`@AllArgsConstructor`, `@Slf4j`, `@Builder`, `@With`)
-   **Logging**: SLF4J with Lombok (`@Slf4j`)
-   **Mapping**: MapStruct for object mapping
-   **Resilience**: Resilience4j for circuit breakers and retries

## Comments and Documentation

### General Rules

-   Add comments to generated code only when necessary—if a complex code fragment would be unclear without them or if a comment significantly improves readability. Avoid excessive comments.
-   Format comments according to common practices: use
    • Javadoc comments for classes, methods, and important interfaces
    • single-line or multi-line comments for explaining non-trivial logic inside a method
- In case the project has implementation chack list add a checkmark into the list when the item is completed.

### Javadoc

-   **Public classes**: Add a brief description of the purpose
-   **Public methods**: Use `@param`, `@return`, `@throws`
-   **Language**: English for Javadoc

### Internal Comments

-   **Complex logic**: Explain "why", not "what"
-   **Avoid the obvious**: Do not comment on simple operations
-   **Relevance**: Keep comments up to date
-   **Language**: English for comments

### Good Style Examples

```java
@Slf4j
@AllArgsConstructor
public class EntityTransferService {

    private final RawDataRepository rawDataRepository;
    private final EntityParser parser;

    /**
     * Transfers entities from external database to local storage.
     *
     * @param loadIdFrom the starting entity ID to transfer from
     */
    public void transferEntities(long loadIdFrom) {
        rawDataRepository.findEntities(loadIdFrom);
    }
}
```

## Testing and Quality

### Testing

-   **Test libraries**: JUnit 5, Mockito, AssertJ
-   **Testcontainers**: PostgreSQL
-   **Custom annotations**: `@SpringBootTest` with custom properties for different test scenarios

### Security

-   **Resource management**: try-with-resources for connections
-   **Logging**: Log important events, but not sensitive data
-   **Environment variables**: Use for secrets (e.g., database credentials)

## Project-Specific Conventions

### Package Structure

-   **Base package**: `com.liashenko.v.*`

### Configuration

-   **Application properties**: Use YAML format
-   **Profiles**: default
-   **Configuration Properties**: Use record classes or POJOs with `@ConfigurationProperties`

### Gradle

-   **Build optimization**: Build cache enabled for CI
-   **Java version**: 21 with toolchain

## Code Examples

### Typical Domain Service

```java
@AllArgsConstructor
public class EntityTransferService {

    private final RawDataRepository rawDataRepository;
    private final EntityService entityService;
    private final ParsedEntitySaver saver;
    private final EntityParser parser;

    public void transferEntities(long loadIdFrom) {
        List<RawEntity> rawEntities = rawDataRepository.findEntities(loadIdFrom);

        rawEntities.stream()
            .map(parser::parse)
            .forEach(saver::save);
    }
}
```

### Database Repository

```java
@AllArgsConstructor
public class DefaultEntityRepository implements EntityRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void save(Entity entity) {
        jdbcTemplate.update(INSERT_SQL, getParams(entity));
    }

    @Override
    public Optional<Entity> findById(Long id) {
        // Implementation with error handling
    }
}
```

### Configuration Properties

```java
@ConfigurationProperties("entity-transfer")
public record EntityTransferProperty(
    List<String> regions,
    int parallelism,
    int batchSize,
    DataServiceProperty dataService
) {
    public record DataServiceProperty(int fetchSize) {}
}
```

### REST Controller

```java
@RestController
@AllArgsConstructor
public class EntityController implements EntityControllerApi {

    private final EntityService entityService;
    private final EntityConverter entityConverter;

    @Override
    public EntityResponseDto getEntity(Integer id) {
        Entity entity = entityService.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entity not found: " + id));

        return entityConverter.toDto(entity);
    }
}
```

### Unit Test

```java
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EntityTransferServiceTest {

    @Mock
    private RawDataRepository rawDataRepository;

    @InjectMocks
    private EntityTransferService service;

    @Test
    void shouldTransferEntities() {
        // Test implementation
    }
}
```

### Integration Test

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class EntityControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnEntity() {
        // Integration test with real database
    }
}
```
