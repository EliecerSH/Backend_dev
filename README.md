# Sistema de Microservicios Backend - E-Commerce (Backend_dev)

Este repositorio contiene el núcleo de servicios backend para la plataforma de comercio electrónico (E-Commerce). La solución implementa una **arquitectura de microservicios desacoplados** desarrollada con **Java 17 / Spring Boot**, persistencia relacional en **PostgreSQL**, autenticación y autorización basada en tokens JWT con **Azure Active Directory (Microsoft Entra ID)**, tolerancia a fallos con **Resilience4j**, contenerización mediante **Docker & Docker Compose** y despliegue continuo automatizado en **AWS (ECR / EC2 vía SSM)** con **GitHub Actions**.

---

## 📐 Diagrama de Arquitectura

```mermaid
graph TD
    Client[Cliente Web / Móvil / Postman] -->|Bearer JWT OAuth2| MS_Usuarios[ms-usuarios :8081]
    Client -->|Público / Bearer JWT| MS_Productos[ms-productos :8082]
    Client -->|Bearer JWT OAuth2| MS_Carrito[ms-carrito :8083]

    subgraph Seguridad & Identidad
        AzureAD[Azure AD / Microsoft Entra ID]
        Client -.->|Autenticación OIDC/OAuth2| AzureAD
        MS_Usuarios -.->|Valida JWT / OID| AzureAD
        MS_Productos -.->|Valida JWT| AzureAD
        MS_Carrito -.->|Valida JWT / OID| AzureAD
    end

    subgraph Comunicación Inter-Servicios
        MS_Carrito -->|RestClient + Resilience4j Circuit Breaker| MS_Productos
    end

    subgraph Capa de Datos (PostgreSQL)
        MS_Usuarios --> DB_Usuarios[(Base de Datos Usuarios)]
        MS_Productos --> DB_Productos[(Base de Datos Productos)]
        MS_Carrito --> DB_Carrito[(Base de Datos Carrito)]
    end
```

---

## 🚀 Microservicios del Ecosistema

### 1. `ms-usuarios` (Puerto: `8081`)
- **Propósito**: Gestión integral del ciclo de vida de los usuarios y sincronización con el proveedor de identidad corporativo.
- **Responsabilidades clave**:
  - Vinculación unívoca con el identificador de Azure AD mediante el claim `oid` (`azure_oid` en base de datos).
  - Control de accesos basado en roles (`ADMIN` y `CLIENTE`). Al registrarse, el sistema asigna automáticamente el rol `CLIENTE` previniendo escalación de privilegios desde el payload.
  - Validación de pertenencia: un cliente solo puede visualizar, modificar o eliminar su propio perfil; los administradores poseen permisos de gestión global.
  - Exposición de información de claims del token activo (`/api/v1/usuarios/me`).

### 2. `ms-productos` (Puerto: `8082`)
- **Propósito**: Catálogo centralizado y control de stock de productos.
- **Responsabilidades clave**:
  - Catálogo público: permite listar productos, consultar detalles por ID y filtrar por categoría sin requerir autenticación.
  - Gestión protegida (ABM): operaciones de creación (`POST`), modificación (`PUT`) y baja (`DELETE`) restringidas a usuarios autenticados con token válido.
  - Auditoría de entidad: marcas de tiempo automáticas para fecha de creación (`creado_en`) y última actualización (`actualizado_en`).
  - Validación declarativa de datos (precio >= 0, stock >= 0, campos obligatorios).

### 3. `ms-carrito` (Puerto: `8083`)
- **Propósito**: Gestión del carro de compras en tiempo real por cada usuario autenticado.
- **Responsabilidades clave**:
  - Carrito por usuario: creación automática o recuperación del carrito vinculado al `oid` / `sub` del usuario autenticado.
  - **Integración con `ms-productos`**: invocación HTTP síncrona mediante Spring `RestClient` para verificar existencia, precio actualizado y stock disponible.
  - **Tolerancia a fallos con Circuit Breaker (`Resilience4j`)**: previene caídas en cascada si `ms-productos` no responde o experimenta degradación, activando el fallback `ServicioProductosNoDisponibleException`.
  - Cálculo transaccional de subtotales por ítem y monto total general del carrito.

---

## 📡 Matriz de Endpoints

### 👤 `ms-usuarios` (`http://localhost:8081`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/usuarios/me` | Autenticado | Retorna los claims presentes en el JWT del usuario autenticado |
| `POST` | `/api/v1/usuarios` | Autenticado | Registra el perfil del usuario (fuerza rol `CLIENTE` y asocia `azureOid`) |
| `GET` | `/api/v1/usuarios/{id}` | Propio / Admin | Obtiene los datos del perfil por ID |
| `PUT` | `/api/v1/usuarios/{id}` | Propio / Admin | Actualiza datos del perfil (los clientes no pueden cambiar su rol) |
| `DELETE` | `/api/v1/usuarios/{id}` | Propio / Admin | Elimina el perfil del usuario |
| `GET` | `/api/v1/usuarios` | Solo ADMIN | Lista todos los usuarios registrados en el sistema |

### 📦 `ms-productos` (`http://localhost:8082`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/productos` | **Público** | Lista todos los productos del catálogo |
| `GET` | `/api/v1/productos/{id}` | **Público** | Obtiene el detalle de un producto específico |
| `GET` | `/api/v1/productos/categoria/{categoria}` | **Público** | Filtra productos por categoría |
| `POST` | `/api/v1/productos` | Autenticado | Crea un nuevo producto (valida campos y stock) |
| `PUT` | `/api/v1/productos/{id}` | Autenticado | Actualiza los datos de un producto existente |
| `DELETE` | `/api/v1/productos/{id}` | Autenticado | Elimina un producto por su ID |

### 🛒 `ms-carrito` (`http://localhost:8083`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/carrito` | Autenticado | Obtiene el carrito activo del usuario autenticado (o lo crea) |
| `POST` | `/api/v1/carrito/items` | Autenticado | Agrega un producto o incrementa cantidad validando stock en `ms-productos` |
| `DELETE` | `/api/v1/carrito/items/{productoId}` | Autenticado | Elimina un producto específico del carrito |
| `DELETE` | `/api/v1/carrito` | Autenticado | Vacía todos los ítems del carrito |

---

## 🛠️ Stack Tecnológico

| Área | Tecnologías |
| :--- | :--- |
| **Lenguaje y Framework** | Java 17, Spring Boot 3 / 4.x (`spring-boot-starter-webmvc`, `spring-boot-starter-validation`) |
| **Persistencia** | Spring Data JPA, Hibernate, PostgreSQL Driver |
| **Seguridad** | Spring Security OAuth2 Resource Server (`NimbusJwtDecoder`), Azure AD JWT Validation |
| **Resiliencia & HTTP** | Resilience4j CircuitBreaker (`resilience4j-spring-boot3`), Spring `RestClient`, Apache HttpClient 5 |
| **Contenedores** | Docker (Multi-stage build con Eclipse Temurin 17 JRE Alpine), Docker Compose |
| **Cloud & CI/CD** | GitHub Actions, AWS ECR, AWS EC2, AWS Systems Manager (SSM) |

---

## ⚙️ Variables de Entorno

Copia el archivo `.env.example` como `.env` en la raíz de `Backend_dev` y configura los valores correspondientes:

```env
# Base de Datos PostgreSQL
POSTGRES_DATABASE=ecommerce_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=tu_password
DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce_db

# Azure Active Directory (Entra ID)
AZURE_ISSUER_URI=https://login.microsoftonline.com/<TENANT_ID>/v2.0
AZURE_TENANT_ID=<TU_TENANT_ID>
AZURE_CLIENT_ID=<TU_CLIENT_ID>

# Integración Inter-Servicios & CORS
PRODUCTOS_SERVICE_URL=http://localhost:8082
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

---

## 🐳 Ejecución con Docker Compose

Para construir y levantar todos los microservicios en una red compartida:

```bash
# 1. Posicionarse en el directorio del proyecto
cd Backend_dev

# 2. Asegurarse de que exista la red de Docker definida como externa
docker network create ecommerce-network

# 3. Construir las imágenes y levantar los contenedores
docker compose up -d --build
```

### Puertos en Docker:
- **`ms-usuarios`**: [http://localhost:8081](http://localhost:8081)
- **`ms-productos`**: [http://localhost:8082](http://localhost:8082)
- **`ms-carrito`**: [http://localhost:8083](http://localhost:8083)

---

## 💻 Ejecución Local para Desarrollo (Maven)

Cada microservicio puede ejecutarse individualmente utilizando su wrapper Maven:

### 1. Iniciar Base de Datos PostgreSQL
Asegúrate de contar con una instancia PostgreSQL corriendo en el puerto `5432` con la base de datos configurada en `.env`.

### 2. Iniciar Servicios
En tres terminales independientes:

```bash
# Terminal 1 - ms-usuarios (Puerto 8081)
cd ms-usuarios
./mvnw spring-boot:run

# Terminal 2 - ms-productos (Puerto 8082)
cd ms-productos
./mvnw spring-boot:run

# Terminal 3 - ms-carrito (Puerto 8083)
cd ms-carrito
./mvnw spring-boot:run
```
*(En Windows CMD / PowerShell usar `mvnw.cmd spring-boot:run`)*.

---

## 🔄 Pipeline CI/CD (GitHub Actions & AWS)

El archivo `.github/workflows/deploy.yml` define el ciclo de integración y despliegue continuo en la rama `deploy`:

1. **Checkout & Autenticación AWS**: Configura credenciales seguras para la región `us-east-1`.
2. **ECR Repositories Check**: Valida y crea automáticamente los repositorios en AWS ECR (`ms-usuarios`, `ms-productos`, `ms-carrito`).
3. **Docker Build & Push**: Construye las imágenes mediante multi-stage build y las publica en AWS ECR con etiquetas `${{ github.sha }}` y `latest`.
4. **Despliegue Remoto con AWS SSM**: Envía un comando a la instancia EC2 objetivo para:
   - Iniciar sesión en ECR.
   - Sincronizar el archivo `docker-compose.yml` en base64 hacia `/home/ec2-user/app/`.
   - Inyectar las variables de entorno desde los Secrets del repositorio.
   - Ejecutar `docker compose pull` y `docker compose up -d --remove-orphans`.
