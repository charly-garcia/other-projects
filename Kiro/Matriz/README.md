# Matriz de Usuarios - Sistema de Inventario de Aplicaciones

Sistema web empresarial para centralizar el inventario de aplicaciones corporativas y la gestión de usuarios.

## Stack Tecnológico

- **Frontend**: Angular 17+ (SPA)
- **Backend**: Spring Boot 3.2.1
- **Base de Datos**: MySQL 8.0
- **Infraestructura**: AWS (S3, CloudFront, ALB, RDS, EC2)
- **ORM**: Spring Data JPA / Hibernate 6.x
- **Migraciones**: Flyway 9.x
- **Documentación API**: SpringDoc OpenAPI 2.x (Swagger UI)
- **Testing**: JUnit 5, Mockito, jqwik (Property-Based Testing), Testcontainers
- **IaC**: Terraform

## Requisitos Previos

### Desarrollo Local
- Java 17 o superior
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+ y npm (para el frontend)
- Angular CLI 17+

### Despliegue en AWS
- Terraform 1.0+
- AWS CLI configurado
- Cuenta AWS con permisos apropiados

## Configuración de Base de Datos

1. Instalar MySQL 8.0 o superior
2. Crear usuario y base de datos (o usar las credenciales por defecto en `application.yml`):

```sql
CREATE DATABASE matriz_usuarios;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON matriz_usuarios.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

3. La aplicación creará automáticamente el esquema mediante Flyway al iniciar.

## Ejecución

### Compilar el proyecto

```bash
mvn clean install
```

### Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### Documentación API (Swagger UI)

Una vez iniciada la aplicación, acceder a:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Estructura del Proyecto

```
src/main/java/com/empresa/appinventory/
├── config/                  # Configuración Spring (CORS, OpenAPI, etc.)
├── exception/               # GlobalExceptionHandler, clases de error
├── common/
│   ├── dto/                 # PagedResponse<T>, ErrorResponse
│   └── validation/          # Anotaciones de validación personalizadas
└── module/
    ├── application/         # Módulo de Inventario de Aplicaciones
    ├── role/                # Módulo de Catálogo de Roles
    ├── area/                # Módulo de Catálogo de Áreas
    ├── company/             # Módulo de Catálogo de Compañías
    ├── supplier/            # Módulo de Catálogo de Proveedores
    └── user/                # Módulo de Gestión de Usuarios
```

## Testing

### Ejecutar todos los tests

```bash
mvn test
```

### Ejecutar tests de integración con Testcontainers

```bash
mvn verify
```

## Módulos del Sistema

1. **Inventario de Aplicaciones**: Gestión de aplicaciones corporativas
2. **Catálogo de Roles**: Gestión de roles del sistema
3. **Catálogo de Áreas**: Gestión de áreas organizacionales
4. **Catálogo de Compañías**: Gestión de compañías
5. **Catálogo de Proveedores**: Gestión de proveedores
6. **Gestión de Usuarios**: Gestión completa del ciclo de vida de usuarios

## API REST

Todos los endpoints siguen el patrón `/api/v1/{recurso}` con operaciones CRUD completas:

- `GET /api/v1/{recurso}?page=0&size=20&search=` - Listar con paginación
- `GET /api/v1/{recurso}/{id}` - Obtener detalle
- `POST /api/v1/{recurso}` - Crear registro
- `PUT /api/v1/{recurso}/{id}` - Actualizar registro
- `DELETE /api/v1/{recurso}/{id}` - Eliminar registro

## Despliegue en AWS

### Arquitectura

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTPS
       ▼
┌─────────────────────────────────────────┐
│         CloudFront CDN                  │
│  ┌────────────────┬──────────────────┐  │
│  │  Static Files  │   API Proxy      │  │
│  │  (S3 Origin)   │   (ALB Origin)   │  │
│  └────────┬───────┴────────┬─────────┘  │
└───────────┼────────────────┼────────────┘
            │                │
            ▼                ▼
    ┌───────────────┐  ┌──────────────┐
    │  S3 Bucket    │  │     ALB      │
    │  (Angular     │  │  (Backend    │
    │   Build)      │  │   API)       │
    └───────────────┘  └──────┬───────┘
                              │
                              ▼
                        ┌──────────────┐
                        │  RDS MySQL   │
                        │  (Database)  │
                        └──────────────┘
```

### Despliegue de Infraestructura

1. **Configurar variables de Terraform**:
```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Editar terraform.tfvars con tus valores
```

2. **Desplegar infraestructura**:
```bash
terraform init
terraform plan
terraform apply
```

Esto creará:
- VPC con subnets públicas y privadas
- RDS MySQL 8 (Multi-AZ opcional)
- Application Load Balancer con HTTPS
- S3 bucket para frontend
- CloudFront distribution
- Security Groups

3. **Obtener información de despliegue**:
```bash
terraform output
```

### Despliegue del Backend

Ver [DEPLOYMENT.md](./DEPLOYMENT.md) para instrucciones detalladas del backend.

### Despliegue del Frontend

#### Quick Start
```bash
# Hacer el script ejecutable
chmod +x deploy-frontend.sh

# Desplegar
./deploy-frontend.sh
```

El script automáticamente:
- ✅ Construye la aplicación Angular
- ✅ Sube los archivos a S3
- ✅ Invalida el caché de CloudFront
- ✅ Muestra la URL del frontend

#### Documentación Completa
- **Quick Start**: [FRONTEND_QUICKSTART.md](./FRONTEND_QUICKSTART.md)
- **Guía Completa**: [FRONTEND_DEPLOYMENT.md](./FRONTEND_DEPLOYMENT.md)

### Características del Despliegue Frontend

- ✅ **CDN Global**: CloudFront distribuye el contenido globalmente
- ✅ **HTTPS**: Encriptación TLS 1.2+ obligatoria
- ✅ **SPA Routing**: Soporte completo para rutas de Angular
- ✅ **API Proxy**: `/api/*` se proxea automáticamente al backend
- ✅ **Caché Optimizado**: Assets estáticos cacheados, API sin caché
- ✅ **Compresión**: gzip y brotli habilitados

### Acceso a la Aplicación

Después del despliegue:
```bash
# Obtener URL del frontend
cd terraform
terraform output frontend_url

# Ejemplo: https://d1234567890abc.cloudfront.net
```



## Licencia

Propietario - Uso Interno Corporativo
