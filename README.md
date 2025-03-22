# Reservei - Sistema de restaurantes
Projeto FIAP de sistema da reserva de restaurante

TÍTULO DO PROJETO: Reservei

Autores:

●	Marcelo Nidal
●	Evaldo Fires
●	Fernando Monin
●	Fabio Aquino
●	Vinícius Campos

## Sumário
1. [Sobre o Projeto](#sobre-o-projeto)
2. [Executando o projeto com Docker](#executando-o-projeto-com-docker)
3. [Instruções de execução](#executando-a-aplicação)
4. [Deploy em nuvem grátis](#deploy-em-cloud-pública-gratuita)
5. [Deploy em nuvem paga](#deploy-em-cloud-pública-paga)

## Sobre o projeto
#### Doc Técnica
https://reservei.onrender.com/swagger-ui/index.html#/

## Executando o projeto com Docker
### Baixar e rodar ou apenas rodar
A imagem docker será criada conforme instruções no [DockerFile](Dockerfile)  

### Executando projeto local
Criar Imagem docker do projeto: docker build -t reservei-app .  
Executar a Imagem docker localmente: docker run -p 8080:8080 reservei-app  
Verificar container criado: http://localhost:8080/swagger-ui/index.html#/

### Executando projeto via imagem no dockerhub
#### Criando Imagem no docker hub
Tagueando imagem: docker tag reservei-app:latest majorv22/reservei-app:1.0  
Fazendo deploy no dockerhub: docker push majorv22/reservei-app:1.0


------------REUTILIZANDO IMGEM-----------------------  

Após subir a imagem no dockerhub de maneira pública, basta utilizar a imagem pública para subir a aplicação
docker run majorv22/reservei-app:1.0

## Executando a aplicação
```textmate
Criar Estado

```
```textmate
Criar Cidade

```
```textmate
Criar Restaurante

```
```textmate
Fazer Reserva

```
## Deploy em Cloud Pública Gratuita
URL: https://reservei.onrender.com/swagger-ui/index.html#/

## Deploy em Cloud Pública Paga
URL: TBD

