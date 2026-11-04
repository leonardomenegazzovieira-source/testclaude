# Saitama: One Punch (Forge 1.20.1)

## Como compilar
1. Instale o **JDK 17**.
2. Baixe o **Forge MDK 1.20.1 (47.3.0)** em files.minecraftforge.net e extraia numa pasta.
3. Copie `src/`, `build.gradle`, `settings.gradle` e `gradle.properties` deste projeto por cima da pasta do MDK (mantendo `gradlew`, `gradlew.bat` e a pasta `gradle/`).
4. Rode `./gradlew build` (Windows: `gradlew build`).
5. O mod fica em `build/libs/saitamamod-1.0.0.jar`. Coloque na pasta `mods` do Forge 1.20.1.

Para testar direto: `./gradlew runClient`.

## Como jogar
Treino (só conta antes de virar Saitama; o progresso é salvo e mantido ao morrer):
- **100 flexões**: socos no ar (sem bloco na frente)
- **100 abdominais**: pulos
- **100 agachamentos**: agachar e levantar
- **10 km de corrida**: correr (sprint) no chão

Ao completar tudo você vira o Saitama.

| Tecla | Poder |
|---|---|
| Soco normal (botão esquerdo) | Mata qualquer coisa de uma vez, arremessa o alvo e empurra quem está em volta |
| **Z** | Soco Sério: carrega 3 s e abre um corredor de destruição, limpa o clima |
| **X** | Socos Normais Consecutivos: rajada de corredores à frente |
| **V** | Soco Sério Planetário: onda que apaga tudo até a bedrock num raio enorme |

Passivos: invencível (exceto /kill e void), super velocidade e pulo, quebra qualquer bloco na hora, sem dano de queda. As teclas podem ser trocadas em Controles > Saitama.

## Comandos (op nível 2)
`/saitama on|off|reset|status [jogador]` — o `on` pula o treino para testar.

## Config
`saitamamod-common.toml` (na pasta `config` do mundo/jogo): metas do treino, tamanho dos socos, raio da onda e velocidade de destruição.
- `planetary.enabled = false` desliga o soco planetário.
- Se o jogo engasgar, diminua `columnsPerTick` e `slicesPerTick`.

## Avisos
- O Soco Planetário destrói o mundo de verdade (bedrock fica). **Faça backup do mundo.**
- A onda só apaga chunks carregados; o alcance real depende da render distance.
- Num servidor, `hurtOtherPlayers = false` impede que os socos matem outros jogadores.

## Compilar sem instalar nada (GitHub Actions)
1. Crie um repositório vazio no github.com.
2. Envie todos os arquivos desta pasta (incluindo a pasta `.github`) para ele: botão "Add file" > "Upload files".
3. Abra a aba **Actions**, clique no workflow "Build mod" e espere terminar (alguns minutos).
4. Dentro da execução, em **Artifacts**, baixe `saitamamod-jar`. Extraia o zip: o `.jar` está dentro.

## Efeitos visuais
- Carga: faíscas sendo sugadas, poeira e detritos do chão, tremor de tela crescendo.
- Soco normal: explosão, fumaça e estrondo no alvo.
- Soco Sério e Consecutivos: flash, onda de choque, fumaça, anéis de pressão e detritos pelo corredor.
- Soco Planetário: parede de poeira e fumaça na frente da onda, terreno voando e tremor longo.
- Se o jogo ficar pesado, reduza as partículas nas opções de vídeo (Particles: Decreased/Minimal).

## Partículas próprias
O mod traz 3 partículas com textura própria, em `assets/saitamamod/textures/particle`:
`big_smoke` (fumaça grossa), `big_dust` (poeira de terra) e `glow` (clarão). Dá para trocar os PNGs por outros
(mantendo os nomes `smoke_0` a `smoke_7` e `glow`) para mudar o visual.
