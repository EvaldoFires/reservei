#language: pt

Funcionalidade: Reserva

  Cenário: Buscar Reserva pelo Id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    E que reserva já foi salva
    Quando buscar reserva por id
    Então a reserva é retornada com sucesso

  Cenário: Salvar Reserva
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    Quando salvar uma nova reserva
    Então a reserva é salva com sucesso

  Cenário: Alterar Reserva
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    E que reserva já foi salva
    Quando alterar reserva
    Então a reserva é alterada com sucesso

  Cenário: Deletar Reserva por id
    Dado que estado já foi salvo
    E que cidade já foi salva
    E que endereço já foi salvo
    E que restaurante já foi salvo
    E que reserva já foi salva
    Quando deletar a reserva por id
    Então a reserva é deletada com sucesso
