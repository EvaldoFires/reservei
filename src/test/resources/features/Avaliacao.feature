#language: pt

Funcionalidade: Avaliação

  Cenário: Buscar Avaliação pelo Id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    E que avaliação já foi salva
    Quando buscar avaliação por id
    Então a avaliação é retornada com sucesso

  Cenário: Salvar Avaliação
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    Quando salvar uma nova avaliação
    Então a avaliação é salva com sucesso

  Cenário: Alterar Avaliação
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    E que avaliação já foi salva
    Quando alterar avaliação
    Então a avaliação é alterada com sucesso

  Cenário: Deletar Avaliação por id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    E que avaliação já foi salva
    Quando deletar a avaliação por id
    Então a avaliação é deletada com sucesso
