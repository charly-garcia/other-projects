# Plan de Implementacion: Matriz de Usuarios

## Descripcion General

Implementacion incremental del sistema Matriz de Usuarios usando Spring Boot 3 (backend), Angular 17 (frontend), MySQL 8 con Flyway (base de datos), jqwik (PBT backend), fast-check (PBT frontend) y Testcontainers (integracion). Las tareas respetan el orden de dependencias: catalogos independientes primero (Roles, Areas, Companias, Proveedores), luego Aplicaciones (depende de Roles) y finalmente Usuarios (depende de todos los anteriores).

## Tareas

- [ ] 1. Configuracion base del proyecto (Backend + Frontend + DB)
  - [x] 1.1 Inicializar proyecto Spring Boot 3
    - Crear proyecto Maven con dependencias: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, mysql-connector-j, flyway-core, flyway-mysql, springdoc-openapi-starter-webmvc-ui, lombok, jqwik (scope test), testcontainers-mysql (scope test)
    - Configurar application.yml con datasource MySQL, JPA (ddl-auto=validate), Flyway habilitado y configuracion OpenAPI
    - Crear estructura de paquetes: config/, exception/, common/dto/, common/validation/, module/role/, module/area/, module/company/, module/supplier/, module/application/, module/user/
    - _Requerimientos: 1.2, 1.3_

  - [x] 1.2 Crear migracion Flyway V1 con esquema completo de la DB
    - Crear src/main/resources/db/migration/V1__init_schema.sql con los DDL de las seis tablas: roles, areas, companies, suppliers, applications, users
    - Incluir restricciones UNIQUE: uk_roles_name, uk_areas_name, uk_companies_name, uk_suppliers_name, uk_applications_name, uk_users_email
    - Incluir FOREIGN KEY: fk_applications_role, fk_users_area, fk_users_company, fk_users_supplier, fk_users_application, fk_users_role
    - Definir columnas ENUM para user_type (Interno/Practicante/Contractor), status (ACTIVO/INACTIVO), scope (PCI/ISO/General), information_access (Secreta/Confidencial/Uso Interno) en la tabla users
    - _Requerimientos: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_

  - [x] 1.3 Implementar infraestructura de manejo de errores del Backend
    - Crear jerarquia de excepciones en exception/: AppException (base), ResourceNotFoundException, DuplicateResourceException, ReferentialIntegrityException, ValidationException, ServiceUnavailableException
    - Implementar GlobalExceptionHandler con @RestControllerAdvice con handlers para cada tipo de excepcion mapeados a los codigos HTTP correctos (404, 400, 409, 503, 500)
    - Crear ErrorResponse record en common/dto/ con campos: timestamp, status, error, message, path
    - Crear ValidationErrorResponse record con campos: timestamp, status, error, fields (Map<String,String>), path
    - Implementar PagedResponseDTO<T> generico con campos: content, totalElements, totalPages, currentPage, pageSize
    - _Requerimientos: 9.1, 9.2, 9.3, 9.4, 1.6_

  - [x] 1.4 Inicializar proyecto Angular 17 con estructura de modulos
    - Crear workspace Angular 17 con routing y SCSS habilitados
    - Instalar dependencias: @angular/material, @angular/cdk, @angular/animations; instalar fast-check como devDependency
    - Configurar provideHttpClient con interceptores, ReactiveFormsModule y BrowserAnimationsModule en app.config.ts
    - Crear estructura de directorios: src/app/core/interceptors/, src/app/core/services/, src/app/core/models/, src/app/shared/components/, src/app/shared/validators/, src/app/modules/
    - _Requerimientos: 1.1_

  - [x] 1.5 Implementar servicios core y interceptores del Frontend
    - Crear NotificationService con metodos showError(message: string) y showSuccess(message: string) usando BehaviorSubject
    - Crear LoadingService con observable isLoading$ usando BehaviorSubject<boolean>
    - Implementar HttpErrorInterceptor: interceptar respuestas con status >= 400, extraer campo message o fields del JSON, invocar NotificationService.showError()
    - Implementar LoadingInterceptor: activar isLoading=true al inicio de cada peticion HTTP y isLoading=false al completarse (incluyendo errores)
    - Registrar ambos interceptores en la configuracion de la aplicacion
    - _Requerimientos: 9.5, 9.6_

  - [x] 1.6 Implementar componentes shared reutilizables del Frontend
    - Crear NotificationComponent (toast/snackbar) que se suscribe a NotificationService y muestra mensajes de error y exito
    - Crear DataTableComponent con inputs: columns: ColumnDef[], dataSource$: Observable<PagedResponse<T>>, pageSize: number, searchable: boolean; y outputs: pageChange, searchChange, editAction, deleteAction
    - Crear ConfirmDialogComponent para confirmaciones de eliminacion con mensaje configurable
    - Definir interfaces TypeScript compartidas en core/models/: PagedResponse<T>, ErrorResponse, ColumnDef
    - _Requerimientos: 8.4, 9.5_

  - [ ]* 1.7 Escribir smoke tests de configuracion
    - Verificar que el contexto Spring arranca correctamente con Testcontainers MySQL 8
    - Verificar que las migraciones Flyway V1 se aplican sin errores al esquema vacio
    - Verificar que el esquema resultante contiene las seis tablas con sus restricciones UNIQUE y FK
    - _Requerimientos: 1.2, 1.3_

- [x] 2. Checkpoint: Verificar configuracion base
  - Asegurarse de que el proyecto Spring Boot compila y el contexto arranca con Testcontainers MySQL.
  - Asegurarse de que el proyecto Angular compila sin errores de TypeScript.
  - Preguntar al usuario si hay dudas antes de continuar.

- [ ] 3. Catalogo de Roles (Backend + Frontend)
  - [x] 3.1 Implementar capa de datos del modulo Role
    - Crear RoleEntity con campos: id, name, description, createdAt, updatedAt; anotaciones JPA (@Entity, @Table, @Column)
    - Crear RoleRepository extendiendo JpaRepository con metodo existsByNameIgnoreCase(String name) y findByNameContainingIgnoreCase(String name, Pageable pageable)
    - Crear RoleRequestDTO record con validaciones: @NotBlank name, description opcional
    - Crear RoleResponseDTO record con campos: id, name, description
    - _Requerimientos: 3.1, 3.2_

  - [x] 3.2 Implementar RoleService con logica de negocio
    - Implementar metodo createRole: verificar unicidad de nombre (lanzar DuplicateResourceException si existe), persistir y retornar RoleResponseDTO
    - Implementar metodo getRoles con paginacion y busqueda por nombre (case-insensitive, parcial)
    - Implementar metodo getRoleById: retornar RoleResponseDTO o lanzar ResourceNotFoundException
    - Implementar metodo updateRole: verificar existencia, verificar unicidad de nombre nuevo, actualizar y retornar
    - Implementar metodo deleteRole: verificar existencia, verificar que no este referenciado por ninguna aplicacion (lanzar ReferentialIntegrityException si lo esta), eliminar
    - Anotar metodos de escritura con @Transactional
    - _Requerimientos: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 3.3 Implementar RoleController con endpoints REST
    - Crear RoleController con @RestController y @RequestMapping("/api/v1/roles")
    - Implementar GET /api/v1/roles con parametros page, size, search; retornar PagedResponseDTO<RoleResponseDTO> con HTTP 200
    - Implementar GET /api/v1/roles/{id} retornando RoleResponseDTO con HTTP 200
    - Implementar POST /api/v1/roles con @Valid @RequestBody RoleRequestDTO; retornar RoleResponseDTO con HTTP 201
    - Implementar PUT /api/v1/roles/{id} con @Valid @RequestBody RoleRequestDTO; retornar RoleResponseDTO con HTTP 200
    - Implementar DELETE /api/v1/roles/{id} retornando HTTP 204
    - _Requerimientos: 3.1, 3.2, 3.3, 3.4, 8.1, 8.2, 8.3_

  - [ ]* 3.4 Escribir pruebas unitarias del modulo Role
    - Escribir tests de RoleService con Mockito: flujo feliz de create/update/delete, DuplicateResourceException en nombre duplicado, ResourceNotFoundException en ID inexistente, ReferentialIntegrityException al eliminar rol en uso
    - Escribir tests de RoleController con MockMvc: verificar codigos HTTP, serializacion JSON y manejo de errores
    - _Requerimientos: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ]* 3.5 Escribir property test P1 para round-trip de catalogos (Roles)
    - **Propiedad 1: Round-trip de creacion en catalogos**
    - Generar RoleRequestDTO aleatorio con nombre unico usando @ForAll de jqwik; POST -> GET by ID -> assertEquals sobre todos los campos
    - Incluir comentario: // Feature: app-inventory-management, Property 1: Round-trip de creacion en catalogos
    - Minimo 100 iteraciones (@Property(tries = 100))
    - **Valida: Requerimientos 3.1, 3.2, 3.3**

  - [ ]* 3.6 Escribir property test P2 para unicidad de nombres (Roles)
    - **Propiedad 2: Unicidad de nombres en catalogos**
    - Crear un rol, luego intentar crear otro con el mismo nombre; assert DuplicateResourceException y que el catalogo no cambio
    - Incluir comentario: // Feature: app-inventory-management, Property 2: Unicidad de nombres en catalogos
    - **Valida: Requerimientos 3.5**

  - [x] 3.7 Implementar modulo Angular de Roles
    - Crear RolesModule con RoleListComponent y RoleFormComponent
    - Crear RoleService Angular con metodos: getRoles(page, size, search), getRoleById(id), createRole(dto), updateRole(id, dto), deleteRole(id)
    - Implementar RoleListComponent: tabla paginada usando DataTableComponent con columnas nombre y descripcion, boton de busqueda, botones de editar y eliminar con ConfirmDialog
    - Implementar RoleFormComponent: formulario reactivo con campos name (required) y description, validacion client-side, boton deshabilitado mientras isLoading$
    - Registrar rutas /roles y /roles/new y /roles/:id/edit en el router
    - _Requerimientos: 3.7, 3.8, 8.4, 9.5, 9.6_

  - [ ]* 3.8 Escribir pruebas de componentes Angular para Roles
    - Escribir tests de RoleListComponent: verificar que renderiza columnas correctas, emite eventos de paginacion y busqueda
    - Escribir tests de RoleFormComponent: verificar que el boton de envio se deshabilita con campos invalidos y con isLoading=true
    - _Requerimientos: 3.7, 3.8_

- [ ] 4. Catalogo de Areas (Backend + Frontend)
  - [x] 4.1 Implementar capa de datos del modulo Area
    - Crear AreaEntity con campos: id, name, description, createdAt, updatedAt
    - Crear AreaRepository con metodos existsByNameIgnoreCase y findByNameContainingIgnoreCase
    - Crear AreaRequestDTO (name @NotBlank, description opcional) y AreaResponseDTO (id, name, description)
    - _Requerimientos: 4.1, 4.2_

  - [x] 4.2 Implementar AreaService con logica de negocio
    - Implementar createArea, getAreas (paginado+busqueda), getAreaById, updateArea, deleteArea
    - En deleteArea: verificar que no este referenciada por ningun usuario (lanzar ReferentialIntegrityException con mensaje "El area esta en uso y no puede eliminarse")
    - Anotar metodos de escritura con @Transactional
    - _Requerimientos: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [x] 4.3 Implementar AreaController con endpoints REST
    - Crear AreaController con @RequestMapping("/api/v1/areas")
    - Implementar los cinco endpoints CRUD con los mismos patrones que RoleController
    - _Requerimientos: 4.1, 4.2, 4.3, 4.4, 8.1, 8.2, 8.3_

  - [ ]* 4.4 Escribir pruebas unitarias del modulo Area
    - Tests de AreaService con Mockito cubriendo flujos felices y todos los casos de error
    - Tests de AreaController con MockMvc verificando codigos HTTP y JSON
    - _Requerimientos: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [ ]* 4.5 Escribir property test P1 para round-trip de catalogos (Areas)
    - **Propiedad 1: Round-trip de creacion en catalogos**
    - Generar AreaRequestDTO aleatorio con nombre unico; POST -> GET by ID -> assertEquals
    - Incluir comentario: // Feature: app-inventory-management, Property 1: Round-trip de creacion en catalogos (Area)
    - **Valida: Requerimientos 4.1, 4.2, 4.3**

  - [ ]* 4.6 Escribir property test P2 para unicidad de nombres (Areas)
    - **Propiedad 2: Unicidad de nombres en catalogos**
    - Crear un area, intentar crear otra con el mismo nombre; assert DuplicateResourceException
    - Incluir comentario: // Feature: app-inventory-management, Property 2: Unicidad de nombres en catalogos (Area)
    - **Valida: Requerimientos 4.5**

  - [x] 4.7 Implementar modulo Angular de Areas
    - Crear AreasModule con AreaListComponent y AreaFormComponent siguiendo el mismo patron que el modulo de Roles
    - Crear AreaService Angular con los cinco metodos CRUD
    - Implementar AreaListComponent con DataTableComponent (columnas: nombre, descripcion)
    - Implementar AreaFormComponent con formulario reactivo y validacion client-side
    - Registrar rutas /areas en el router
    - _Requerimientos: 4.7, 4.8, 8.4, 9.5, 9.6_

  - [ ]* 4.8 Escribir pruebas de componentes Angular para Areas
    - Tests de AreaListComponent y AreaFormComponent siguiendo el mismo patron que los tests de Roles
    - _Requerimientos: 4.7, 4.8_

- [ ] 5. Catalogo de Companias (Backend + Frontend)
  - [x] 5.1 Implementar capa de datos del modulo Company
    - Crear CompanyEntity con campos: id, name, country, createdAt, updatedAt
    - Crear CompanyRepository con metodos existsByNameIgnoreCase y findByNameContainingIgnoreCase
    - Crear CompanyRequestDTO (name @NotBlank, country @NotBlank) y CompanyResponseDTO (id, name, country)
    - _Requerimientos: 5.1, 5.2_

  - [x] 5.2 Implementar CompanyService con logica de negocio
    - Implementar createCompany, getCompanies (paginado+busqueda), getCompanyById, updateCompany, deleteCompany
    - En deleteCompany: verificar que no este referenciada por ningun usuario (lanzar ReferentialIntegrityException con mensaje "La compania esta en uso y no puede eliminarse")
    - Anotar metodos de escritura con @Transactional
    - _Requerimientos: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [x] 5.3 Implementar CompanyController con endpoints REST
    - Crear CompanyController con @RequestMapping("/api/v1/companies")
    - Implementar los cinco endpoints CRUD con los mismos patrones que RoleController
    - _Requerimientos: 5.1, 5.2, 5.3, 5.4, 8.1, 8.2, 8.3_

  - [ ]* 5.4 Escribir pruebas unitarias del modulo Company
    - Tests de CompanyService con Mockito cubriendo flujos felices y todos los casos de error
    - Tests de CompanyController con MockMvc
    - _Requerimientos: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [ ]* 5.5 Escribir property test P1 para round-trip de catalogos (Companias)
    - **Propiedad 1: Round-trip de creacion en catalogos**
    - Generar CompanyRequestDTO aleatorio con nombre unico; POST -> GET by ID -> assertEquals
    - Incluir comentario: // Feature: app-inventory-management, Property 1: Round-trip de creacion en catalogos (Company)
    - **Valida: Requerimientos 5.1, 5.2, 5.3**

  - [ ]* 5.6 Escribir property test P2 para unicidad de nombres (Companias)
    - **Propiedad 2: Unicidad de nombres en catalogos**
    - Crear una compania, intentar crear otra con el mismo nombre; assert DuplicateResourceException
    - Incluir comentario: // Feature: app-inventory-management, Property 2: Unicidad de nombres en catalogos (Company)
    - **Valida: Requerimientos 5.5**

  - [x] 5.7 Implementar modulo Angular de Companias
    - Crear CompaniesModule con CompanyListComponent y CompanyFormComponent
    - Crear CompanyService Angular con los cinco metodos CRUD
    - Implementar CompanyListComponent con DataTableComponent (columnas: nombre, pais)
    - Implementar CompanyFormComponent con formulario reactivo (name required, country required)
    - Registrar rutas /companies en el router
    - _Requerimientos: 5.7, 5.8, 8.4, 9.5, 9.6_

  - [ ]* 5.8 Escribir pruebas de componentes Angular para Companias
    - Tests de CompanyListComponent y CompanyFormComponent
    - _Requerimientos: 5.7, 5.8_

- [ ] 6. Catalogo de Proveedores (Backend + Frontend)
  - [x] 6.1 Implementar capa de datos del modulo Supplier
    - Crear SupplierEntity con campos: id, name, compliance (boolean), createdAt, updatedAt
    - Crear SupplierRepository con metodos existsByNameIgnoreCase y findByNameContainingIgnoreCase
    - Crear SupplierRequestDTO (name @NotBlank, compliance @NotNull boolean) y SupplierResponseDTO (id, name, compliance)
    - _Requerimientos: 6.1, 6.2_

  - [x] 6.2 Implementar SupplierService con logica de negocio
    - Implementar createSupplier, getSuppliers (paginado+busqueda), getSupplierById, updateSupplier, deleteSupplier
    - En deleteSupplier: verificar que no este referenciado por ningun usuario (lanzar ReferentialIntegrityException con mensaje "El proveedor esta en uso y no puede eliminarse")
    - Anotar metodos de escritura con @Transactional
    - _Requerimientos: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [x] 6.3 Implementar SupplierController con endpoints REST
    - Crear SupplierController con @RequestMapping("/api/v1/suppliers")
    - Implementar los cinco endpoints CRUD con los mismos patrones que RoleController
    - _Requerimientos: 6.1, 6.2, 6.3, 6.4, 8.1, 8.2, 8.3_

  - [ ]* 6.4 Escribir pruebas unitarias del modulo Supplier
    - Tests de SupplierService con Mockito cubriendo flujos felices y todos los casos de error
    - Tests de SupplierController con MockMvc
    - _Requerimientos: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 6.5 Escribir property test P1 para round-trip de catalogos (Proveedores)
    - **Propiedad 1: Round-trip de creacion en catalogos**
    - Generar SupplierRequestDTO aleatorio con nombre unico y compliance booleano; POST -> GET by ID -> assertEquals
    - Incluir comentario: // Feature: app-inventory-management, Property 1: Round-trip de creacion en catalogos (Supplier)
    - **Valida: Requerimientos 6.1, 6.2, 6.3**

  - [ ]* 6.6 Escribir property test P2 para unicidad de nombres (Proveedores)
    - **Propiedad 2: Unicidad de nombres en catalogos**
    - Crear un proveedor, intentar crear otro con el mismo nombre; assert DuplicateResourceException
    - Incluir comentario: // Feature: app-inventory-management, Property 2: Unicidad de nombres en catalogos (Supplier)
    - **Valida: Requerimientos 6.5**

  - [x] 6.7 Implementar modulo Angular de Proveedores
    - Crear SuppliersModule con SupplierListComponent y SupplierFormComponent
    - Crear SupplierService Angular con los cinco metodos CRUD
    - Implementar SupplierListComponent con DataTableComponent (columnas: nombre, en cumplimiento)
    - Implementar SupplierFormComponent con formulario reactivo (name required, compliance toggle/checkbox)
    - Registrar rutas /suppliers en el router
    - _Requerimientos: 6.7, 6.8, 8.4, 9.5, 9.6_

  - [ ]* 6.8 Escribir pruebas de componentes Angular para Proveedores
    - Tests de SupplierListComponent y SupplierFormComponent
    - _Requerimientos: 6.7, 6.8_

- [x] 7. Checkpoint: Verificar catalogos independientes
  - Asegurarse de que todos los tests unitarios y de propiedad de Roles, Areas, Companias y Proveedores pasan.
  - Asegurarse de que los cuatro modulos Angular compilan y los componentes renderizan correctamente.
  - Preguntar al usuario si hay dudas antes de continuar.

- [ ] 8. Inventario de Aplicaciones (Backend + Frontend)
  - [x] 8.1 Implementar capa de datos del modulo Application
    - Crear ApplicationEntity con campos: id, name, owner, url, role (ManyToOne a RoleEntity), createdAt, updatedAt
    - Crear ApplicationRepository con metodos existsByNameIgnoreCase, findByNameContainingIgnoreCase y findByRoleId(Long roleId)
    - Crear ApplicationRequestDTO record con validaciones: @NotBlank name, @NotBlank owner, @NotBlank @URL url, @NotNull Long roleId
    - Crear ApplicationResponseDTO record con campos: id, name, owner, url, roleId, roleName
    - _Requerimientos: 2.1, 2.2, 2.3_

  - [x] 8.2 Implementar ApplicationService con logica de negocio
    - Implementar createApplication: verificar unicidad de nombre, verificar existencia del roleId (lanzar ResourceNotFoundException si no existe), validar formato URL, persistir y retornar ApplicationResponseDTO con roleName resuelto
    - Implementar getApplications con paginacion y busqueda por nombre
    - Implementar getApplicationById: retornar ApplicationResponseDTO con roleName o lanzar ResourceNotFoundException
    - Implementar updateApplication: verificar existencia, unicidad de nombre nuevo, existencia del nuevo roleId, actualizar
    - Implementar deleteApplication: verificar existencia, verificar que no este referenciada por ningun usuario (lanzar ReferentialIntegrityException con mensaje "La aplicacion esta en uso y no puede eliminarse"), eliminar
    - Implementar getRolesByApplicationId: retornar lista de RoleResponseDTO para el rol asociado a la aplicacion (para el endpoint de dropdown)
    - Anotar metodos de escritura con @Transactional
    - _Requerimientos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9_

  - [x] 8.3 Implementar ApplicationController con endpoints REST
    - Crear ApplicationController con @RequestMapping("/api/v1/applications")
    - Implementar los cinco endpoints CRUD estandar
    - Implementar GET /api/v1/applications/{id}/roles retornando List<RoleResponseDTO> con HTTP 200 (para el dropdown dependiente del frontend)
    - _Requerimientos: 2.1, 2.2, 2.3, 2.4, 2.5, 7.20, 8.1, 8.2, 8.3_

  - [ ]* 8.4 Escribir pruebas unitarias del modulo Application
    - Tests de ApplicationService con Mockito: flujo feliz de create/update/delete, DuplicateResourceException en nombre duplicado, ResourceNotFoundException en roleId inexistente, ResourceNotFoundException en applicationId inexistente, ReferentialIntegrityException al eliminar aplicacion referenciada por usuario
    - Tests de ApplicationController con MockMvc: verificar codigos HTTP, JSON y endpoint /roles
    - _Requerimientos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9_

  - [ ]* 8.5 Escribir property test P3 para integridad referencial al eliminar (Roles -> Aplicaciones)
    - **Propiedad 3: Integridad referencial al eliminar entidades referenciadas**
    - Crear un rol, crear una aplicacion que lo referencia, intentar eliminar el rol; assert ReferentialIntegrityException y que el rol sigue existiendo
    - Incluir comentario: // Feature: app-inventory-management, Property 3: Integridad referencial al eliminar entidades referenciadas
    - **Valida: Requerimientos 2.9, 3.6**

  - [ ]* 8.6 Escribir property test P4 para round-trip de aplicaciones con resolucion de rol
    - **Propiedad 4: Round-trip de creacion de aplicaciones con resolucion de rol**
    - Generar ApplicationRequestDTO aleatorio con nombre unico, owner, URL valida y roleId existente; POST -> GET by ID -> assertEquals incluyendo roleName resuelto
    - Incluir comentario: // Feature: app-inventory-management, Property 4: Round-trip de creacion de aplicaciones con resolucion de rol
    - **Valida: Requerimientos 2.1, 2.3, 2.4**

  - [ ]* 8.7 Escribir property test P5 para rechazo de URLs invalidas
    - **Propiedad 5: Rechazo de URLs invalidas en aplicaciones**
    - Generar strings que no sean URLs validas (sin http:// o https://, con caracteres invalidos); intentar crear aplicacion con esos valores; assert HTTP 400 con mensaje "La URL proporcionada no tiene un formato valido"
    - Incluir comentario: // Feature: app-inventory-management, Property 5: Rechazo de URLs invalidas en aplicaciones
    - **Valida: Requerimiento 2.8**

  - [x] 8.8 Implementar modulo Angular de Aplicaciones
    - Crear ApplicationsModule con ApplicationListComponent y ApplicationFormComponent
    - Crear ApplicationService Angular con metodos: getApplications(page, size, search), getApplicationById(id), createApplication(dto), updateApplication(id, dto), deleteApplication(id), getRolesByApplication(appId)
    - Implementar ApplicationListComponent con DataTableComponent (columnas: nombre, owner, URL, rol)
    - Implementar ApplicationFormComponent con formulario reactivo: campos name, owner, url (con validacion de formato URL), y dropdown de roles cargado desde /api/v1/roles
    - _Requerimientos: 2.10, 2.11, 8.4, 9.5, 9.6_

  - [ ]* 8.9 Escribir pruebas de componentes Angular para Aplicaciones
    - Tests de ApplicationListComponent y ApplicationFormComponent
    - Verificar que la validacion de URL muestra error con valores invalidos
    - _Requerimientos: 2.10, 2.11_

- [ ] 9. Modulo de Usuarios (Backend + Frontend)
  - [x] 9.1 Implementar enumeraciones y capa de datos del modulo User
    - Crear enumeraciones Java: UserType (Interno, Practicante, Contractor), UserStatus (ACTIVO, INACTIVO), Scope (PCI, ISO, General), InformationAccess (Secreta, Confidencial, Uso_Interno)
    - Crear UserEntity con todos los campos del diseno: campos obligatorios (name, email, userType, status, startDate, scope, informationAccess) y opcionales (areaId FK, companyId FK, supplierId FK, applicationId FK, roleId FK, position, manager, endDate)
    - Crear UserRepository con metodos: existsByEmailIgnoreCase, findByNameContainingIgnoreCase (paginado), findByEmailContainingIgnoreCase (paginado)
    - Crear UserRequestDTO record con todas las validaciones: @NotBlank name, @NotBlank @Email email, @NotNull userType/status/startDate/scope/informationAccess; campos opcionales sin anotaciones de requerimiento
    - Crear UserResponseDTO record con todos los campos incluyendo nombres resueltos: areaName, companyName, supplierName, applicationName, roleName
    - _Requerimientos: 7.1, 7.2, 7.7, 7.8, 7.9, 7.10_

  - [x] 9.2 Implementar UserService con logica de negocio completa
    - Implementar createUser: validar formato email (@Email de Bean Validation), verificar unicidad de email, verificar existencia de areaId/companyId/supplierId/applicationId/roleId si se proporcionan, validar que roleId pertenece a applicationId (si ambos se proporcionan), validar que endDate no sea anterior a startDate, persistir y retornar UserResponseDTO con nombres resueltos
    - Implementar getUsers con paginacion y busqueda por nombre (case-insensitive, parcial)
    - Implementar getUserById: retornar UserResponseDTO con todos los nombres resueltos o lanzar ResourceNotFoundException
    - Implementar updateUser: mismas validaciones que createUser, actualizar y retornar
    - Implementar deleteUser: verificar existencia, eliminar
    - Anotar metodos de escritura con @Transactional
    - _Requerimientos: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 7.12, 7.13, 7.14, 7.15, 7.16, 7.19, 7.21, 7.22, 7.23_

  - [x] 9.3 Implementar UserController con endpoints REST
    - Crear UserController con @RequestMapping("/api/v1/users")
    - Implementar los cinco endpoints CRUD estandar con los mismos patrones
    - _Requerimientos: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 8.1, 8.2, 8.3_

  - [ ]* 9.4 Escribir pruebas unitarias del modulo User
    - Tests de UserService con Mockito cubriendo: flujo feliz de create/update/delete, DuplicateResourceException en email duplicado, ResourceNotFoundException en IDs de referencias inexistentes, ValidationException en email invalido, ValidationException en endDate anterior a startDate, ValidationException en roleId que no pertenece a applicationId
    - Tests de UserController con MockMvc: verificar codigos HTTP, JSON y manejo de errores de validacion
    - _Requerimientos: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 7.12, 7.13, 7.14, 7.15, 7.16_

  - [ ]* 9.5 Escribir property test P6 para round-trip de usuarios con resolucion de referencias
    - **Propiedad 6: Round-trip de creacion de usuarios con resolucion de referencias**
    - Generar UserRequestDTO valido con referencias opcionales a Area, Company y/o Supplier existentes; POST -> GET by ID -> assertEquals incluyendo nombres resueltos (areaName, companyName, supplierName)
    - Incluir comentario: // Feature: app-inventory-management, Property 6: Round-trip de creacion de usuarios con resolucion de referencias
    - **Valida: Requerimientos 7.1, 7.2, 7.4, 7.5**

  - [ ]* 9.6 Escribir property test P7 para rechazo de valores de enumeracion invalidos
    - **Propiedad 7: Rechazo de valores de enumeracion invalidos en usuarios**
    - Generar strings que no pertenezcan a los conjuntos validos de user_type, status, scope, information_access; intentar crear usuario con esos valores; assert HTTP 400
    - Incluir comentario: // Feature: app-inventory-management, Property 7: Rechazo de valores de enumeracion invalidos en usuarios
    - **Valida: Requerimientos 7.7, 7.8, 7.9, 7.10**

  - [ ]* 9.7 Escribir property test P8 para validacion de rango de fechas
    - **Propiedad 8: Validacion de rango de fechas en usuarios**
    - Generar pares (startDate, endDate) donde endDate es anterior a startDate; intentar crear usuario con ese par; assert HTTP 400 con mensaje "La fecha de baja no puede ser anterior a la fecha de alta"
    - Incluir comentario: // Feature: app-inventory-management, Property 8: Validacion de rango de fechas en usuarios
    - **Valida: Requerimiento 7.16**

  - [ ]* 9.8 Escribir property test P13 para consistencia de rol-aplicacion en usuarios
    - **Propiedad 13: Consistencia de rol-aplicacion en usuarios**
    - Generar usuarios con roleId que no pertenece a applicationId seleccionada; assert HTTP 400 con mensaje "El rol especificado no pertenece a la aplicacion seleccionada"
    - Generar usuarios con roleId valido para applicationId; assert creacion exitosa
    - Incluir comentario: // Feature: app-inventory-management, Property 13: Consistencia de rol-aplicacion en usuarios
    - **Valida: Requerimientos 7.21, 7.22, 7.23**

  - [ ]* 9.9 Escribir property test P3 para integridad referencial (Areas/Companias/Proveedores/Aplicaciones -> Usuarios)
    - **Propiedad 3: Integridad referencial al eliminar entidades referenciadas**
    - Crear usuario con referencias a Area, Company, Supplier y Application; intentar eliminar cada entidad referenciada; assert ReferentialIntegrityException y que la entidad sigue existiendo
    - Incluir comentario: // Feature: app-inventory-management, Property 3: Integridad referencial al eliminar entidades referenciadas (User dependencies)
    - **Valida: Requerimientos 4.6, 5.6, 6.6, 10.3**

  - [x] 9.10 Implementar modulo Angular de Usuarios
    - Crear UsersModule con UserListComponent y UserFormComponent
    - Crear UserService Angular con metodos: getUsers(page, size, search), getUserById(id), createUser(dto), updateUser(id, dto), deleteUser(id)
    - Implementar UserListComponent con DataTableComponent (columnas: nombre, correo, tipo, estatus, area, compania, aplicacion, rol)
    - _Requerimientos: 7.17, 8.4, 9.5_

  - [x] 9.11 Implementar UserFormComponent con dropdown en cascada aplicacion->rol
    - Implementar formulario reactivo con todos los campos obligatorios (name, email, userType, status, startDate, scope, informationAccess) y opcionales (areaId, companyId, supplierId, applicationId, roleId, position, manager, endDate)
    - Cargar dropdowns de catalogos al inicializar: areas, companies, suppliers, applications (llamadas paralelas con forkJoin)
    - Implementar logica de dropdown en cascada: suscribirse a valueChanges de applicationId; al cambiar, llamar a ApplicationService.getRolesByApplication(appId) y poblar el dropdown de roles; resetear roleId a null al cambiar la aplicacion
    - Deshabilitar el dropdown de roles hasta que se seleccione una aplicacion
    - Implementar validacion client-side de todos los campos requeridos y formato de email
    - Deshabilitar boton de envio mientras isLoading$ es true
    - _Requerimientos: 7.18, 7.19, 7.20, 7.21, 7.24, 9.6_

  - [ ]* 9.12 Escribir pruebas de componentes Angular para Usuarios
    - Tests de UserListComponent: verificar que renderiza las ocho columnas correctas
    - Tests de UserFormComponent: verificar que el dropdown de roles se deshabilita sin aplicacion seleccionada, que se resetea al cambiar la aplicacion, y que el boton de envio se deshabilita con campos invalidos
    - _Requerimientos: 7.17, 7.18, 7.20, 7.24_

- [x] 10. Checkpoint: Verificar modulos de Aplicaciones y Usuarios
  - Asegurarse de que todos los tests unitarios y de propiedad de Aplicaciones y Usuarios pasan.
  - Asegurarse de que el dropdown en cascada aplicacion->rol funciona correctamente en el formulario de usuarios.
  - Preguntar al usuario si hay dudas antes de continuar.

- [ ] 11. Paginacion, busqueda y manejo de errores global
  - [x] 11.1 Verificar y completar paginacion en todos los endpoints de listado
    - Revisar que todos los controladores (Role, Area, Company, Supplier, Application, User) aceptan parametros page (base 0, default 0) y size (default 20)
    - Verificar que todos retornan PagedResponseDTO<T> con los metadatos: totalElements, totalPages, currentPage, pageSize
    - Verificar que si page excede totalPages se retorna lista vacia con metadatos correctos
    - _Requerimientos: 8.1, 8.2, 8.5_

  - [x] 11.2 Verificar y completar busqueda por nombre en todos los endpoints
    - Revisar que todos los endpoints de listado aceptan parametro search para filtrado parcial e insensible a mayusculas/minusculas
    - Verificar que el filtro se aplica sobre el campo name de cada entidad
    - _Requerimientos: 8.3_

  - [x] 11.3 Verificar cobertura completa del GlobalExceptionHandler
    - Revisar que todos los casos de error documentados en el diseno estan cubiertos por el GlobalExceptionHandler
    - Verificar que ningun error expone stack traces o detalles internos de implementacion en la respuesta HTTP
    - Verificar que el error HTTP 503 se retorna cuando la DB no esta disponible (DataAccessException)
    - _Requerimientos: 9.1, 9.2, 9.3, 9.4, 1.6_

  - [ ]* 11.4 Escribir property test P9 para consistencia de metadatos de paginacion
    - **Propiedad 9: Consistencia de metadatos de paginacion**
    - Para cada endpoint de listado, generar N registros y combinaciones validas de page/size; verificar que totalPages = ceil(totalElements / pageSize) y que content.size() <= pageSize
    - Incluir comentario: // Feature: app-inventory-management, Property 9: Consistencia de metadatos de paginacion
    - **Valida: Requerimientos 8.1, 8.2, 8.5**

  - [ ]* 11.5 Escribir property test P10 para correcto filtro de busqueda por nombre
    - **Propiedad 10: Correcto filtro de busqueda por nombre**
    - Para cada endpoint de listado, generar un dataset de registros y un termino de busqueda T; verificar que todos los registros en content contienen T en su campo name (case-insensitive) y que ningun registro sin T aparece en los resultados
    - Incluir comentario: // Feature: app-inventory-management, Property 10: Correcto filtro de busqueda por nombre
    - **Valida: Requerimiento 8.3**

  - [ ]* 11.6 Escribir property test P11 para mapeo consistente de errores HTTP
    - **Propiedad 11: Mapeo consistente de errores HTTP**
    - Generar peticiones con campos requeridos ausentes -> assert HTTP 400 con lista de campos
    - Generar peticiones con IDs inexistentes -> assert HTTP 404 con mensaje descriptivo
    - Generar peticiones que violan integridad referencial -> assert HTTP 409 con mensaje descriptivo
    - Verificar que ninguna de estas condiciones retorna HTTP 200 ni expone detalles internos
    - Incluir comentario: // Feature: app-inventory-management, Property 11: Mapeo consistente de errores HTTP
    - **Valida: Requerimientos 9.1, 9.2, 9.3**

  - [ ]* 11.7 Escribir prueba PBT con fast-check para filtro de busqueda en Frontend (P10)
    - **Propiedad 10: Correcto filtro de busqueda por nombre (Frontend)**
    - Usando fast-check, generar datasets de registros y terminos de busqueda aleatorios; verificar que el DataTableComponent solo muestra registros que contienen el termino en su nombre
    - Incluir comentario: // Feature: app-inventory-management, Property 10: Correcto filtro de busqueda (Frontend)
    - **Valida: Requerimiento 8.3**

- [ ] 12. Pruebas de integracion con Testcontainers
  - [x] 12.1 Configurar suite de pruebas de integracion con Testcontainers MySQL
    - Crear clase base AbstractIntegrationTest con @SpringBootTest y configuracion de MySQLContainer de Testcontainers
    - Configurar datasource dinamico apuntando al contenedor MySQL levantado por Testcontainers
    - Asegurarse de que Flyway aplica las migraciones al contenedor antes de cada suite de tests
    - _Requerimientos: 1.3, 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_

  - [x] 12.2 Escribir pruebas de integracion para restricciones de DB
    - Verificar que las restricciones UNIQUE de la DB rechazan nombres duplicados a nivel de base de datos
    - Verificar que las FOREIGN KEY impiden eliminar entidades referenciadas a nivel de base de datos
    - Verificar que las restricciones de unicidad de email en users funcionan a nivel de DB
    - _Requerimientos: 10.1, 10.2, 10.5, 10.6_

  - [ ]* 12.3 Escribir property test P12 para atomicidad de transacciones
    - **Propiedad 12: Atomicidad de transacciones multi-tabla**
    - Simular un fallo en una operacion de escritura que involucra multiples tablas; verificar que el estado de la DB es identico al estado previo a la operacion (rollback completo)
    - Incluir comentario: // Feature: app-inventory-management, Property 12: Atomicidad de transacciones multi-tabla
    - **Valida: Requerimiento 10.7**

  - [ ]* 12.4 Escribir pruebas de integracion end-to-end por modulo
    - Para cada modulo (Role, Area, Company, Supplier, Application, User): escribir un test de integracion que ejecute el flujo completo CRUD contra la DB real (Testcontainers)
    - Verificar que los endpoints de paginacion y busqueda retornan los metadatos correctos con datos reales en la DB
    - _Requerimientos: 3.1-3.6, 4.1-4.6, 5.1-5.6, 6.1-6.6, 2.1-2.9, 7.1-7.23_

- [~] 13. Checkpoint: Verificar suite completa de pruebas
  - Asegurarse de que todos los tests unitarios, de propiedad y de integracion pasan.
  - Verificar que la cobertura de Backend-Service es >= 85% y Backend-Controller >= 80%.
  - Preguntar al usuario si hay dudas antes de continuar.

- [ ] 14. Infraestructura AWS
  - [x] 14.1 Crear scripts de infraestructura como codigo (IaC)
    - Crear plantilla CloudFormation o scripts Terraform para: VPC con subnets publicas y privadas, Security Groups para ALB, EC2/ECS y RDS, Amazon RDS MySQL 8 en subnet privada con Multi-AZ opcional, Application Load Balancer en subnet publica con listener HTTPS en puerto 443
    - Configurar reglas de Security Group: ALB acepta trafico HTTPS desde internet; EC2/ECS acepta trafico solo desde ALB en el puerto de la aplicacion; RDS acepta trafico solo desde EC2/ECS en puerto 3306
    - _Requerimientos: 1.4_

  - [x] 14.2 Configurar despliegue del Backend en EC2/ECS
    - Crear Dockerfile para la aplicacion Spring Boot: imagen base eclipse-temurin:21-jre-alpine, copiar JAR, exponer puerto 8080, definir ENTRYPOINT
    - Crear script de despliegue o task definition de ECS con variables de entorno para datasource (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD) inyectadas desde AWS Secrets Manager o Parameter Store
    - Configurar health check del ALB apuntando al endpoint /actuator/health de Spring Boot Actuator
    - _Requerimientos: 1.2, 1.4_

  - [x] 14.3 Configurar despliegue del Frontend en S3 + CloudFront
    - Crear bucket S3 para hosting estatico del build de Angular (ng build --configuration production)
    - Configurar distribucion CloudFront apuntando al bucket S3 con HTTPS, cache headers y redireccion de rutas SPA (error 403/404 -> index.html)
    - Configurar regla de rewrite en ALB o CloudFront para que las peticiones a /api/** se proxeen al Backend
    - _Requerimientos: 1.1, 1.4_

  - [ ]* 14.4 Escribir pruebas de humo de infraestructura
    - Verificar conectividad entre EC2/ECS y RDS (puerto 3306 accesible)
    - Verificar que el endpoint /actuator/health del Backend retorna HTTP 200
    - Verificar que el ALB responde en HTTPS con el certificado correcto
    - _Requerimientos: 1.4, 1.5_

- [~] 15. Checkpoint final: Verificar sistema completo
  - Asegurarse de que todos los tests pasan (unitarios, PBT, integracion).
  - Asegurarse de que el build de produccion de Angular y el JAR de Spring Boot se generan sin errores.
  - Asegurarse de que los scripts de infraestructura AWS son validos (terraform validate o cfn-lint).
  - Preguntar al usuario si hay dudas antes de dar por concluida la implementacion.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP mas rapido.
- Cada tarea referencia los requerimientos especificos para trazabilidad.
- Los checkpoints garantizan validacion incremental antes de avanzar al siguiente modulo.
- El orden de implementacion respeta las dependencias: Roles -> Areas/Companias/Proveedores -> Aplicaciones -> Usuarios.
- Las propiedades PBT (P1-P13) del documento de diseno se implementan como sub-tareas opcionales distribuidas cerca de la implementacion que validan.
- Todas las pruebas de propiedad deben incluir el comentario de trazabilidad: `// Feature: app-inventory-management, Property N: <texto>`.
- Las pruebas de integracion usan Testcontainers para levantar MySQL 8 en Docker; asegurarse de que Docker esta disponible en el entorno de CI.
