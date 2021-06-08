# CRS conversion service

### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

**Required to run service**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `KEYVAULT_URI` | ex `https://foo-keyvault.vault.azure.net/` | URI of KeyVault that holds application secrets | no | output of infrastructure deployment |
| `ENTITLEMENT_URL` | ex `https://foo-entitlements.azurewebsites.net` | Entitlements API endpoint | no | output of infrastructure deployment |
| `appinsights_key` | `********` | API Key for App Insights | yes | output of infrastructure deployment |
| `aad_client_id` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `spring_application_name` | `crs-conversion-service` | Name of application. Needed by App Insights | no | -- |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username` |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID` | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password` |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-tenant-id` |
| `server_port` | ex `8080` | Port of the server | no | -- |
| `ACCEPT_HTTP` | `true` | TEMPORARY UNTIL HTTPS | no | -- |
| `azure_istioauth_enabled` | `true` | Flag to Disable AAD auth | no | -- |
| `server.servlet.contextPath` | `/api/crs/converter/` | Servlet context path | no | - |
| `cosmosdb_database` | ex `foo-db` | The name of the CosmosDB database | no | output of infrastructure deployment |
| `service_domain_name` | ex `contoso.com` | The name of the domain for which the service will run | no | output of infrastructure deployment |
| `SIS_DATA` | ex `E:\crs-converter\apachesis_setup\` | Apache SIS setup | no | [apachesis](../../../apachesis_setup/README.md) |
| `ESRI_DATA_PATH` | ex `\crs-conversion-service` | -- | no | -- |
