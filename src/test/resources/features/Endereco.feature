# language: pt
Funcionalidade: Endereço

  Cenário: Buscar Endereço por id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    Quando buscar endereço por id
    Então o endereço é retornado com id

  Cenário: Salvar Endereço
    Dado que estado já foi salvo
    E que cidade já foi salva
    Quando salvar um novo endereço
    Então o endereço é salvo com sucesso

  Cenário: Alterar Endereço
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    Quando alterar endereço
    Então o endereço é alterado com sucesso

  Cenário: Deletar Endereço por id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    Quando deletar o endereço por id
    Então o endereço é deletado com sucesso

