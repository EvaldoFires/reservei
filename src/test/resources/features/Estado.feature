# language: pt
Funcionalidade: Estado

  Cenário: Buscar Estado por id
    Dado que estado já foi salvo
    Quando buscar estado por id
    Então o estado é retornado com id

  Cenário: Salvar Estado
    Quando salvar um novo estado
    Então o estado é salvo com sucesso

  Cenário: Alterar Estado
    Dado que estado já foi salvo
    Quando alterar estado
    Então o estado é alterado com sucesso

  Cenário: Deletar Estado por id
    Dado que estado já foi salvo
    Quando deletar o estado por id
    Então o estado é deletado com sucesso
