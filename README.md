# API Hotel - Configuração do Banco de Dados

1. **Crie o Banco de Dados:**

    No pgAdmin/DBeaver, crie um banco chamado `api_hotel_db`. Não crie tabelas.

2. **Crie o Arquivo Local:** 

    Na pasta `src/main/resources`, crie o arquivo `application-local.properties`.

3. **Insira as Credenciais:** 
 
    Adicione o código abaixo no `application-local.properties`, alterando usuário e senha:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/api_hotel_db
    spring.datasource.username=seu_usuario_aqui
    spring.datasource.password=sua_senha_aqui
    ```
4. **Atualize o `.gitignore`:** 
 
    Adicione `application-local.properties` no arquivo `.gitignore` para não subir as senhas para o GitHub.

5. **Rode o Projeto:** 

   Execute a aplicação (`ApiHotelApplication.java`). O Spring criará as tabelas automaticamente.