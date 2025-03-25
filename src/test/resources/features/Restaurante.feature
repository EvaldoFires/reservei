# language: pt
Funcionalidade: Restaurantes

  Cenário: Buscar Restaurante pelo Nome
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    Quando buscar restaurante por nome
    Então restaurante é retornado com sucesso

  Cenário: Buscar Restaurante pelo Id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    Quando buscar restaurante por id
    Então restaurante é retornado com sucesso

  Cenário: Buscar Restaurantes pela Cozinha
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    Quando buscar restaurantes por cozinha
    Então restaurantes são retornados com sucesso

  Cenário: Salvar Restaurante
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    Quando salvar um novo restaurante
    Então o restaurante é salvo com sucesso

  Cenário: Alterar Restaurante
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    Quando alterar restaurante
    Então o restaurante é alterado com sucesso

  Cenário: Deletar Restaurante por id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    Quando deletar o restaurante por id
    Então o restaurante é deletado com sucesso
