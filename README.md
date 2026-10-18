# HeroVisuals 1.1 (Fabric, Minecraft 1.21.4)

Меню: Right Shift. ЛКМ по модулю - вкл/выкл, ПКМ - настройки, колесо - прокрутка.
HUD: открой чат (T) и перетаскивай элементы мышкой.
Сборка: gradle build -> build/libs/HeroVisuals-1.0.0.jar


## Дополнительные функции (обновлённая исходная версия)
- World: Warden Helper (контейнеры в радиусе и только при прямой видимости), Fullbright (gamma), Sky, Snow.
- Render: Target ESP / Target HUD, Jump Circle, Block Overlay, Hit Stars (критические удары создают больше звёзд), Trail, China Hat.
- HUD: Watermark, Module List, Info, Server Info (ping и сервер). Элементы HUD можно перетаскивать в чате.
- Client: Streamer Mode (скрытие адреса сервера и координат в собственном HUD), FPS Boost, интерфейс и шрифты.
- Бинды: средняя кнопка мыши на модуле в ClickGUI, затем нажать клавишу; Backspace/Delete/Esc снимает бинд.

Примечания: таймеры обновления сундуков не универсальны для Minecraft/Fabric — сервер должен предоставлять такой таймер, иначе мод не может достоверно определить его. Fullbright меняет gamma клиента, но не гарантирует снятие эффекта Darkness. Изменение положения руки/меча и полноценный заменяющий экран инвентаря не включены в эту итерацию, так как требуют отдельных render mixin/экранных обработчиков.
