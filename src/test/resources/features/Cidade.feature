# language: pt
Funcionalidade: Cidade

  Cenário: Buscar Cidade por id
    Dado que estado já foi salvo
    E que cidade já foi salva
    Quando buscar cidade por id
    Então a cidade é retornada com id

  Cenário: Salvar Cidade
    Dado que estado já foi salvo
    Quando salvar uma nova cidade
    Então a cidade é salva com sucesso

  Cenário: Alterar Cidade
    Dado que estado já foi salvo
    E que cidade já foi salva
    Quando alterar cidade
    Então a cidade é alterada com sucesso

  Cenário: Deletar Cidade por id
    Dado que estado já foi salvo
    E que cidade já foi salva
    Quando deletar a cidade por id
    Então a cidade é deletada com sucesso