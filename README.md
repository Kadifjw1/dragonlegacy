Моменты для настройки и вызовы через команды: 

Показать плашку “получено”
  /dlquesttoast <ник> accepted

Показать плашку “завершено”
  /dlquesttoast <ник> completed

Настроить плашку
Синтаксис:

  /dlquesttoastconfig <ник> set <x> <y> <width> <height> <fadeIn> <stay> <fadeOut> <startOffsetX>

Пример:
/dlquesttoastconfig TheFrameTrip set 6 6 128 32 8 140 8 -140

Где:
x y — позиция
width height — размер
fadeIn — длительность появления в тиках
stay — сколько стоит на месте
fadeOut — длительность исчезновения
startOffsetX — откуда выезжает слева

Сбросить к дефолту
  /dlquesttoastconfig <ник> reset

Как вызывать из CustomNPC
Принятие квеста
  event.API.executeCommand(event.player.world, '/dlquesttoast ' + event.player.name + ' accepted')
  
Завершение квеста
  event.API.executeCommand(event.player.world, '/dlquesttoast ' + event.player.name + ' completed')

Настройка из NPC
  event.API.executeCommand(event.player.world, '/dlquesttoastconfig ' + event.player.name + ' set 6 6 128 32 8 140 8 -140')

---

Экран управления способностями (вкл/выкл)
  /dlabilityscreen open <ник>
