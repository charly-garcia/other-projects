# Documento de Requerimientos

## Introducción

El sistema **Matriz de Usuarios** es una aplicación web empresarial desarrollada con Angular (frontend), Spring Boot (backend), MySQL (base de datos) e infraestructura en AWS. Su propósito es centralizar el inventario de aplicaciones corporativas y los catálogos de soporte (Roles, Áreas, Compañías, Proveedores) junto con la gestión completa de usuarios. Todos los módulos exponen operaciones CRUD completas (Alta, Baja y Modificación).

---

## Glosario

- **System**: El sistema Matriz de Usuarios en su conjunto.
- **Frontend**: La aplicación Angular que sirve la interfaz de usuario.
- **Backend**: El servicio Spring Boot que expone la API REST.
- **DB**: La base de datos MySQL que persiste la información.
- **Application_Inventory**: Módulo que gestiona el catálogo de aplicaciones corporativas.
- **Role_Catalog**: Módulo que gestiona los roles disponibles en el sistema.
- **Area_Catalog**: Módulo que gestiona las áreas organizacionales.
- **Company_Catalog**: Módulo que gestiona las compañías registradas.
- **Supplier_Catalog**: Módulo que gestiona los proveedores.
- **User_Module**: Módulo que gestiona los usuarios del sistema.
- **API**: Interfaz REST expuesta por el Backend.
- **Record**: Entidad persistida en la DB (aplicación, rol, área, compañía, proveedor o usuario).
- **User_Type**: Clasificación del usuario: `Interno`, `Practicante` o `Contractor`.
- **User_Status**: Estado del usuario: `ACTIVO` o `INACTIVO`.
- **Scope**: Alcance de acceso del usuario: `PCI`, `ISO` o `General`.
- **Information_Access**: Nivel de acceso a la información: `Secreta`, `Confidencial` o `Uso Interno`.
- **Compliance**: Indicador booleano que señala si un proveedor cumple con las políticas corporativas.

---

## Requerimientos

---

### Requerimiento 1: Arquitectura General del Sistema

**User Story:** Como arquitecto de soluciones, quiero que el sistema esté construido sobre Angular, Spring Boot, MySQL y AWS, para garantizar escalabilidad, mantenibilidad y alineación con los estándares corporativos.

#### Criterios de Aceptación

1. THE System SHALL exponer una interfaz de usuario implementada en Angular 17 o superior.
2. THE Backend SHALL implementar una API REST con Spring Boot 3 o superior.
3. THE Backend SHALL conectarse a una instancia de MySQL 8 o superior como DB.
4. THE System SHALL desplegarse en infraestructura AWS (EC2, RDS o equivalentes gestionados).
5. WHEN el Frontend realiza una petición a la API, THE Backend SHALL responder en un formato JSON con código HTTP apropiado.
6. IF la DB no está disponible, THEN THE Backend SHALL retornar un error HTTP 503 con un mensaje descriptivo.

---

### Requerimiento 2: CRUD de Inventario de Aplicaciones

**User Story:** Como administrador, quiero gestionar el inventario de aplicaciones corporativas con sus datos principales, para mantener un registro actualizado de todas las aplicaciones en uso.

#### Criterios de Aceptación

1. THE Application_Inventory SHALL permitir crear un registro con los campos: nombre de la aplicación, owner, URL y rol (referencia al Role_Catalog).
2. THE Application_Inventory SHALL permitir consultar la lista completa de aplicaciones registradas.
3. WHEN se solicita el detalle de una aplicación, THE Application_Inventory SHALL retornar todos sus campos incluyendo el nombre del rol asociado.
4. THE Application_Inventory SHALL permitir modificar cualquier campo de un registro existente.
5. THE Application_Inventory SHALL permitir eliminar un registro existente.
6. IF se intenta crear una aplicación con un nombre duplicado, THEN THE Application_Inventory SHALL retornar un error de validación con el mensaje "El nombre de la aplicación ya existe".
7. IF se intenta crear una aplicación con un rol que no existe en el Role_Catalog, THEN THE Application_Inventory SHALL retornar un error de validación con el mensaje "El rol especificado no existe".
8. IF se intenta crear una aplicación con una URL con formato inválido, THEN THE Application_Inventory SHALL retornar un error de validación con el mensaje "La URL proporcionada no tiene un formato válido".
9. WHEN se elimina un rol del Role_Catalog que está referenciado por al menos una aplicación, THE Application_Inventory SHALL retornar un error de integridad referencial con el mensaje "El rol está en uso y no puede eliminarse".
10. THE Frontend SHALL mostrar la lista de aplicaciones en una tabla paginada con columnas: nombre, owner, URL y rol.
11. THE Frontend SHALL proveer formularios de alta y modificación con validación de campos requeridos antes de enviar la petición al Backend.

---

### Requerimiento 3: CRUD de Catálogo de Roles

**User Story:** Como administrador, quiero gestionar el catálogo de roles, para que las aplicaciones puedan asociarse a un rol corporativo definido.

#### Criterios de Aceptación

1. THE Role_Catalog SHALL permitir crear un registro con los campos: nombre del rol y descripción.
2. THE Role_Catalog SHALL permitir consultar la lista completa de roles registrados.
3. THE Role_Catalog SHALL permitir modificar el nombre y la descripción de un rol existente.
4. THE Role_Catalog SHALL permitir eliminar un rol existente.
5. IF se intenta crear un rol con un nombre duplicado, THEN THE Role_Catalog SHALL retornar un error de validación con el mensaje "El nombre del rol ya existe".
6. IF se intenta eliminar un rol que está referenciado por al menos una aplicación en el Application_Inventory, THEN THE Role_Catalog SHALL retornar un error de integridad referencial con el mensaje "El rol está en uso y no puede eliminarse".
7. THE Frontend SHALL mostrar la lista de roles en una tabla paginada con columnas: nombre y descripción.
8. THE Frontend SHALL proveer formularios de alta y modificación con validación de campos requeridos antes de enviar la petición al Backend.

---

### Requerimiento 4: CRUD de Catálogo de Áreas

**User Story:** Como administrador, quiero gestionar el catálogo de áreas organizacionales, para que los usuarios puedan asociarse al área correspondiente.

#### Criterios de Aceptación

1. THE Area_Catalog SHALL permitir crear un registro con los campos: nombre del área y descripción.
2. THE Area_Catalog SHALL permitir consultar la lista completa de áreas registradas.
3. THE Area_Catalog SHALL permitir modificar el nombre y la descripción de un área existente.
4. THE Area_Catalog SHALL permitir eliminar un área existente.
5. IF se intenta crear un área con un nombre duplicado, THEN THE Area_Catalog SHALL retornar un error de validación con el mensaje "El nombre del área ya existe".
6. IF se intenta eliminar un área que está referenciada por al menos un usuario en el User_Module, THEN THE Area_Catalog SHALL retornar un error de integridad referencial con el mensaje "El área está en uso y no puede eliminarse".
7. THE Frontend SHALL mostrar la lista de áreas en una tabla paginada con columnas: nombre y descripción.
8. THE Frontend SHALL proveer formularios de alta y modificación con validación de campos requeridos antes de enviar la petición al Backend.

---

### Requerimiento 5: CRUD de Catálogo de Compañías

**User Story:** Como administrador, quiero gestionar el catálogo de compañías, para que los usuarios puedan asociarse a la compañía a la que pertenecen.

#### Criterios de Aceptación

1. THE Company_Catalog SHALL permitir crear un registro con los campos: nombre de la compañía y país.
2. THE Company_Catalog SHALL permitir consultar la lista completa de compañías registradas.
3. THE Company_Catalog SHALL permitir modificar el nombre y el país de una compañía existente.
4. THE Company_Catalog SHALL permitir eliminar una compañía existente.
5. IF se intenta crear una compañía con un nombre duplicado, THEN THE Company_Catalog SHALL retornar un error de validación con el mensaje "El nombre de la compañía ya existe".
6. IF se intenta eliminar una compañía que está referenciada por al menos un usuario en el User_Module, THEN THE Company_Catalog SHALL retornar un error de integridad referencial con el mensaje "La compañía está en uso y no puede eliminarse".
7. THE Frontend SHALL mostrar la lista de compañías en una tabla paginada con columnas: nombre y país.
8. THE Frontend SHALL proveer formularios de alta y modificación con validación de campos requeridos antes de enviar la petición al Backend.

---

### Requerimiento 6: CRUD de Catálogo de Proveedores

**User Story:** Como administrador, quiero gestionar el catálogo de proveedores, para registrar qué proveedores están en cumplimiento con las políticas corporativas.

#### Criterios de Aceptación

1. THE Supplier_Catalog SHALL permitir crear un registro con los campos: nombre del proveedor y en cumplimiento (valor booleano: Sí / No).
2. THE Supplier_Catalog SHALL permitir consultar la lista completa de proveedores registrados.
3. THE Supplier_Catalog SHALL permitir modificar el nombre y el indicador de cumplimiento de un proveedor existente.
4. THE Supplier_Catalog SHALL permitir eliminar un proveedor existente.
5. IF se intenta crear un proveedor con un nombre duplicado, THEN THE Supplier_Catalog SHALL retornar un error de validación con el mensaje "El nombre del proveedor ya existe".
6. IF se intenta eliminar un proveedor que está referenciado por al menos un usuario en el User_Module, THEN THE Supplier_Catalog SHALL retornar un error de integridad referencial con el mensaje "El proveedor está en uso y no puede eliminarse".
7. THE Frontend SHALL mostrar la lista de proveedores en una tabla paginada con columnas: nombre y en cumplimiento.
8. THE Frontend SHALL proveer formularios de alta y modificación con validación de campos requeridos antes de enviar la petición al Backend.

---

### Requerimiento 7: CRUD de Usuarios

**User Story:** Como administrador, quiero gestionar el ciclo de vida completo de los usuarios del sistema, para mantener un directorio actualizado con toda la información relevante de cada persona.

#### Criterios de Aceptación

1. THE User_Module SHALL permitir crear un registro con los siguientes campos obligatorios: nombre del usuario, correo electrónico, tipo de usuario, estatus del usuario, fecha de alta, alcance y acceso a la información.
2. THE User_Module SHALL permitir crear un registro con los siguientes campos opcionales: área (referencia al Area_Catalog), compañía (referencia al Company_Catalog), proveedor (referencia al Supplier_Catalog), puesto, jefe y fecha de baja.
3. THE User_Module SHALL permitir consultar la lista completa de usuarios registrados.
4. WHEN se solicita el detalle de un usuario, THE User_Module SHALL retornar todos sus campos incluyendo los nombres de área, compañía y proveedor asociados.
5. THE User_Module SHALL permitir modificar cualquier campo de un usuario existente.
6. THE User_Module SHALL permitir eliminar un registro de usuario existente.
7. THE User_Module SHALL aceptar únicamente los valores `Interno`, `Practicante` o `Contractor` para el campo tipo de usuario.
8. THE User_Module SHALL aceptar únicamente los valores `ACTIVO` o `INACTIVO` para el campo estatus del usuario.
9. THE User_Module SHALL aceptar únicamente los valores `PCI`, `ISO` o `General` para el campo alcance.
10. THE User_Module SHALL aceptar únicamente los valores `Secreta`, `Confidencial` o `Uso Interno` para el campo acceso a la información.
11. IF se intenta crear un usuario con un correo electrónico con formato inválido, THEN THE User_Module SHALL retornar un error de validación con el mensaje "El correo electrónico no tiene un formato válido".
12. IF se intenta crear un usuario con un correo electrónico duplicado, THEN THE User_Module SHALL retornar un error de validación con el mensaje "El correo electrónico ya está registrado".
13. IF se intenta crear un usuario con un área que no existe en el Area_Catalog, THEN THE User_Module SHALL retornar un error de validación con el mensaje "El área especificada no existe".
14. IF se intenta crear un usuario con una compañía que no existe en el Company_Catalog, THEN THE User_Module SHALL retornar un error de validación con el mensaje "La compañía especificada no existe".
15. IF se intenta crear un usuario con un proveedor que no existe en el Supplier_Catalog, THEN THE User_Module SHALL retornar un error de validación con el mensaje "El proveedor especificado no existe".
16. IF se proporciona una fecha de baja y esta es anterior a la fecha de alta, THEN THE User_Module SHALL retornar un error de validación con el mensaje "La fecha de baja no puede ser anterior a la fecha de alta".
17. THE Frontend SHALL mostrar la lista de usuarios en una tabla paginada con columnas: nombre, correo electrónico, tipo de usuario, estatus, área, compañía, aplicación y rol.
18. THE Frontend SHALL proveer formularios de alta y modificación con validación de campos requeridos y listas desplegables para los campos de tipo enumerado y referencias a catálogos, antes de enviar la petición al Backend.
19. THE User_Module SHALL permitir asociar una aplicación (referencia al Application_Inventory) al usuario como campo opcional.
20. WHEN se selecciona una aplicación en el formulario de usuario, THE Frontend SHALL cargar dinámicamente solo los roles asociados a esa aplicación en el campo de rol.
21. THE User_Module SHALL permitir asociar un único rol (referencia al Role_Catalog a través de la aplicación seleccionada) al usuario como campo opcional.
22. IF se intenta crear un usuario con una aplicación que no existe, THEN THE User_Module SHALL retornar error "La aplicación especificada no existe".
23. IF se intenta crear un usuario con un rol que no pertenece a la aplicación seleccionada, THEN THE User_Module SHALL retornar error "El rol especificado no pertenece a la aplicación seleccionada".
24. IF se cambia la aplicación en el formulario, THEN THE Frontend SHALL resetear el campo de rol.

---

### Requerimiento 8: Paginación y Búsqueda en Listados

**User Story:** Como usuario del sistema, quiero poder paginar y filtrar los registros de cualquier módulo, para encontrar información de forma eficiente sin cargar todos los datos a la vez.

#### Criterios de Aceptación

1. THE API SHALL soportar parámetros de paginación `page` (número de página, base 0) y `size` (registros por página, valor por defecto 20) en todos los endpoints de listado.
2. THE API SHALL retornar en la respuesta de listado los metadatos: `totalElements`, `totalPages`, `currentPage` y `pageSize`.
3. THE API SHALL soportar un parámetro de búsqueda `search` en los endpoints de listado para filtrar registros por nombre de forma parcial e insensible a mayúsculas/minúsculas.
4. THE Frontend SHALL mostrar controles de paginación que permitan navegar entre páginas y seleccionar el tamaño de página.
5. IF se solicita una página que excede el total de páginas disponibles, THEN THE API SHALL retornar una lista vacía con los metadatos de paginación correctos.

---

### Requerimiento 9: Validación General y Manejo de Errores

**User Story:** Como desarrollador, quiero que el sistema maneje errores de forma consistente y descriptiva, para facilitar el diagnóstico de problemas y mejorar la experiencia del usuario.

#### Criterios de Aceptación

1. WHEN el Backend recibe una petición con campos requeridos ausentes, THE Backend SHALL retornar un error HTTP 400 con un cuerpo JSON que liste los campos faltantes.
2. WHEN el Backend recibe una petición con un identificador de recurso que no existe, THE Backend SHALL retornar un error HTTP 404 con un mensaje descriptivo.
3. WHEN ocurre un error de integridad referencial en la DB, THE Backend SHALL retornar un error HTTP 409 con un mensaje descriptivo.
4. WHEN ocurre un error interno no controlado en el Backend, THE Backend SHALL retornar un error HTTP 500 con un mensaje genérico sin exponer detalles internos de la implementación.
5. THE Frontend SHALL mostrar los mensajes de error retornados por el Backend en un componente de notificación visible para el usuario.
6. THE Frontend SHALL deshabilitar el botón de envío de formularios mientras una petición al Backend está en curso, para evitar envíos duplicados.

---

### Requerimiento 10: Persistencia e Integridad de Datos

**User Story:** Como administrador de base de datos, quiero que el esquema de datos garantice la integridad referencial entre módulos, para evitar registros huérfanos o inconsistencias.

#### Criterios de Aceptación

1. THE DB SHALL definir claves foráneas entre la tabla de aplicaciones y la tabla de roles.
2. THE DB SHALL definir claves foráneas entre la tabla de usuarios y las tablas de áreas, compañías y proveedores.
3. THE DB SHALL definir una clave foránea opcional entre la tabla de usuarios y la tabla de aplicaciones (campo `application_id`).
4. THE DB SHALL definir una clave foránea opcional entre la tabla de usuarios y la tabla de roles (campo `role_id`).
5. THE DB SHALL definir restricciones de unicidad sobre los campos de nombre en todas las tablas de catálogos.
6. THE DB SHALL definir restricciones de unicidad sobre el campo correo electrónico en la tabla de usuarios.
7. THE Backend SHALL ejecutar todas las operaciones de escritura que involucren múltiples tablas dentro de una transacción de base de datos, de modo que IF alguna operación falla, THEN THE Backend SHALL revertir todos los cambios de esa transacción.
