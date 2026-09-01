# Trinity Financiero API

API REST para administrar clientes, cuentas bancarias y transacciones financieras.

## Inicio rápido con Docker

Necesitas tener instalado Docker Desktop. Desde la raíz del proyecto ejecuta:

```powershell
docker compose up --build -d
```

La aplicación queda disponible en `http://localhost:8080` y PostgreSQL en el puerto `5432`.

Para detener los servicios:

```powershell
docker compose down
```

Los datos de PostgreSQL se conservan en el volumen `pgdata`, incluso después de detener los contenedores.

## Documentación interactiva

Con la aplicación en ejecución puedes consultar y probar los endpoints desde Swagger UI:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI (JSON): `http://localhost:8080/api-docs`

Selecciona un endpoint, pulsa **Try it out**, completa los datos y usa **Execute** para enviar la solicitud.

## Endpoints principales

| Recurso | Operaciones |
| --- | --- |
| Clientes | `POST /api/clients`, `GET /api/clients`, `GET /api/clients/{id}`, `PUT /api/clients/{id}`, `DELETE /api/clients/{id}` |
| Cuentas | `POST /api/accounts`, `GET /api/accounts/{id}`, `GET /api/accounts/client/{clientId}`, `PATCH /api/accounts/{id}/status` |
| Transacciones | `POST /api/transactions`, `GET /api/transactions/account/{accountId}` |

Ejemplo para crear un cliente:

```json
{
  "identificationType": "CC",
  "identificationNumber": "4040404040",
  "firstName": "María",
  "lastName": "López",
  "email": "maria.lopez@example.com",
  "birthDate": "1995-06-20"
}
```

## Configuración de base de datos

El archivo `docker-compose.yml` crea una base PostgreSQL con estos valores para desarrollo:

| Variable | Valor |
| --- | --- |
| Base de datos | `financial_db` |
| Usuario | `admin` |
| Contraseña | `admin123` |

No uses estas credenciales en un entorno productivo.
