# CraftMMO — especificação integral do plugin

## Estado do documento

Este documento define a visão integral, os contratos técnicos e o roadmap de implementação do CraftMMO.

O CraftMMO reutiliza a direção arquitetural do CraftRPG — framework modular para Paper, progressão persistente, conteúdo, criaturas, bosses, quests, parties, loot, zonas, menus, comandos, API e ferramentas administrativas — mas substitui por completo o modelo de classes, atributos distribuíveis, recursos de classe e skill bars por uma progressão baseada em atividades e skills no estilo mcMMO.

O CraftMMO não terá:

- classes;
- subclasses;
- escolha de classe;
- pontos de atributo distribuíveis;
- pontos de skill;
- árvores de talentos de classe;
- mana, energia ou fúria de classe;
- skill bar de combate;
- aprendizagem manual de skills.

As skills evoluem através da utilização das mecânicas correspondentes.

## Referência funcional da versão 1.0.0

A versão 1.0.0 do CraftMMO utilizará como referência funcional inicial:

- mcMMO `2.2.053`;
- data de congelamento da referência: `2026-06-16`;
- comportamento observado nessa versão;
- configuração padrão dessa versão;
- documentação oficial e, conforme a estratégia legal escolhida, análise permitida do código oficial da versão fixada;
- uma versão exata de Minecraft e Paper, a definir e congelar antes da implementação das skills.

A referência foi selecionada para definir o comportamento inicial do CraftMMO 1.0.0. Não cria uma obrigação de compatibilidade, interoperabilidade, substituição direta ou acompanhamento das versões futuras do mcMMO.

“Replicar o mcMMO na versão 1.0.0” significa reproduzir de forma independente o comportamento funcional relevante, e não apenas reutilizar os mesmos nomes.

A especificação funcional da 1.0.0 deverá cobrir:

- skills existentes na referência;
- fontes de XP;
- fórmulas de XP;
- curvas e modos de progressão;
- níveis e ranks;
- unlock levels;
- super abilities;
- ativação e estado ready;
- duração;
- cooldowns;
- passivas;
- probabilidades;
- dano;
- drops;
- loot e treasure tables;
- interações com encantamentos;
- diferenças PvE/PvP;
- child skills;
- permissões relevantes;
- edge cases;
- comportamento após morte, logout, mudança de mundo e restart;
- anti-exploit;
- ordem relevante dos eventos Paper;
- valores padrão da versão de referência.

A referência da 1.0.0 fica congelada durante o desenvolvimento. Uma publicação posterior do mcMMO não altera automaticamente o objetivo da release.

Depois da versão 1.0.0, o CraftMMO evoluirá como produto próprio e poderá:

- alterar fórmulas e balanceamento;
- adicionar, remover ou redesenhar skills e sub-skills;
- alterar cooldowns, loot, XP e progressão;
- integrar profundamente equipamento, quests, zonas, mobs e bosses;
- introduzir comportamentos incompatíveis com a referência inicial.

Qualquer alteração pós-1.0 será decidida e versionada como evolução do CraftMMO, não como atualização de compatibilidade com o mcMMO.

## Independência e afiliação

CraftMMO é um produto separado e autónomo.

Não deverá afirmar que é:

- o mcMMO oficial;
- uma versão oficial do mcMMO;
- um fork oficial;
- aprovado pelos autores do mcMMO;
- afiliado ao projeto mcMMO;
- um substituto drop-in garantido para instalações mcMMO.

A documentação pública deverá declarar que o mcMMO 2.2.053 foi utilizado apenas como referência funcional inicial para definir a experiência da versão 1.0.0 do CraftMMO.

O CraftMMO não dependerá do mcMMO em runtime e não utilizará o mcMMO como provider de skills.

## Natureza deste documento

Este documento separa:

1. visão integral do produto;
2. arquitetura e contratos técnicos;
3. referência funcional inicial da versão 1.0.0;
4. escopo de cada release.

Uma funcionalidade estar presente na visão integral não significa que tenha de entrar na primeira build jogável.

O desenvolvimento será feito através de fatias verticais demonstráveis. Cada milestone deverá produzir um ciclo utilizável, persistente e testado.

---

# 1. Definição do produto

O CraftMMO será um framework MMO modular para servidores Paper, orientado a progressão por atividades.

O jogador progride ao:

- minerar;
- cortar madeira;
- escavar;
- cultivar;
- pescar;
- preparar poções;
- reparar e recuperar materiais;
- combater com diferentes armas;
- combater sem armas;
- usar arcos, bestas, tridentes, lanças e maças;
- domesticar e utilizar companheiros;
- sobreviver a quedas;
- completar quests;
- explorar zonas;
- enfrentar criaturas, elites e bosses;
- participar em parties e eventos.

O plugin será responsável por:

- perfil persistente;
- progressão individual por skill;
- Power Level;
- skills passivas;
- super abilities;
- cooldowns;
- combate relevante para as skills;
- gathering e recompensas;
- alquimia;
- pesca;
- repair, salvage e smelting;
- custom items e equipamentos;
- criaturas, elites e bosses;
- quests;
- loot;
- parties;
- zonas e checkpoints;
- menus e comandos;
- API;
- PostgreSQL;
- Redis opcional;
- ferramentas administrativas;
- migração de dados;
- observabilidade;
- segurança e anti-exploit.

O CraftMMO não deverá substituir:

- LuckPerms;
- WorldGuard;
- Citizens;
- Vault;
- plugins de economia completos;
- plugins de proteção;
- proxies;
- plugins de NPCs;
- sistemas de autenticação.

Integrações serão usadas onde já existem soluções maduras.

---

# 2. Diferenças fundamentais em relação ao CraftRPG

## 2.1 Sem classes

Não existe Guerreiro, Arqueiro, Mago ou qualquer outra classe.

Um jogador pode desenvolver simultaneamente:

- Mining;
- Swords;
- Fishing;
- Alchemy;
- Taming;
- qualquer outra skill disponível.

Não existem restrições artificiais de classe sobre armas ou atividades.

## 2.2 Sem atributos distribuíveis

Não existem pontos de Força, Vitalidade, Destreza, Inteligência ou Espírito distribuídos pelo jogador.

Isto evita introduzir modificadores que alterariam as fórmulas definidas para o ruleset oficial do CraftMMO 1.0.0.

Podem existir estatísticas de equipamento para os sistemas RPG próprios, mas o perfil oficial da versão 1.0.0 deverá impedir que essas estatísticas alterem os resultados das skills definidos pelo ruleset inicial.

## 2.3 Sem pontos de skill

As sub-skills são desbloqueadas automaticamente quando o nível correspondente é atingido.

O jogador não compra ranks.

O jogador não escolhe talentos.

O nível da skill determina:

- ranks;
- probabilidades;
- bónus;
- duração;
- desbloqueios.

## 2.4 Sem recursos de classe

Não existem mana, energia ou fúria para ativar as skills do núcleo inicial do CraftMMO.

Super abilities usam:

- condições de ativação;
- duração;
- cooldown;
- ferramenta válida;
- estado ready;
- permissões.

## 2.5 Progressão por uso

A progressão principal é:

ação válida
→ validação anti-exploit
→ cálculo de XP
→ aplicação de multiplicadores permitidos
→ ganho de XP na skill correta
→ level up
→ novo rank ou efeito desbloqueado
→ persistência
→ feedback

---

# 3. Experiência principal do jogador

O ciclo de jogo será:

Explorar
→ realizar atividades
→ ganhar XP nas skills usadas
→ aumentar níveis e Power Level
→ desbloquear passivas e super abilities
→ obter loot, moeda e equipamentos
→ enfrentar conteúdo mais difícil
→ completar quests, elites e bosses
→ especializar-se naturalmente através do estilo de jogo

O jogador poderá interagir através de:

- ações vanilla;
- comandos;
- menus;
- NPCs;
- blocos;
- entidades;
- ferramentas;
- armas;
- mensagens clicáveis;
- action bar;
- boss bars;
- sons e partículas.

Os comandos permanecem descobríveis através de:

`/craftmmo help`

Aliases curtos podem ser configuráveis, mas o namespace principal deverá evitar colisões.

---

# 4. Motor de skills do CraftMMO

## 4.1 Motor próprio

O CraftMMO implementa internamente todas as skills, progressão, passivas, super abilities, cooldowns e recompensas.

Objetivos da versão 1.0.0:

- reproduzir o comportamento funcional selecionado do mcMMO 2.2.053;
- não depender do plugin mcMMO em runtime;
- possuir modelos, serviços, persistência e eventos próprios;
- validar a implementação através de cenários de referência e testes diferenciais durante o desenvolvimento;
- congelar os resultados esperados num ruleset oficial `craftmmo-1.0.0`.

O motor do CraftMMO será a única autoridade sobre as suas skills.

## 4.2 Sem provider mcMMO

Não existirá modo delegado nem integração que entregue ao mcMMO oficial o cálculo de XP, níveis, abilities ou rewards.

O CraftMMO não é um addon do mcMMO.

A instalação simultânea dos dois plugins no mesmo servidor não será suportada, porque ambos podem reagir às mesmas ações e produzir progressão, dano, drops ou cooldowns duplicados.

O bootstrap deverá detetar o mcMMO instalado e falhar com uma mensagem clara, salvo se no futuro existir uma estratégia explícita e isolada de migração ou coexistência que não duplique autoridade.

## 4.3 Evolução após a versão 1.0.0

O ruleset `craftmmo-1.0.0` representa o comportamento oficial da primeira release.

Versões posteriores poderão substituir esse comportamento por regras próprias do CraftMMO. A referência mcMMO permanece apenas como registo histórico e base de verificação da 1.0.0.

---

# 5. Perfis de jogador

Cada UUID terá um perfil principal.

O perfil guardará:

- UUID;
- nome conhecido mais recente;
- nível e XP de cada skill;
- Power Level;
- ranks derivados;
- cooldowns persistentes relevantes;
- estado de abilities quando aplicável;
- estatísticas;
- criaturas derrotadas;
- bosses derrotados;
- quests;
- achievements;
- zonas descobertas;
- checkpoints;
- party atual;
- moedas;
- pity;
- preferências;
- data de criação;
- último login;
- tempo jogado;
- versão do perfil;
- versão do ruleset CraftMMO aplicado ao perfil.

Não existirão múltiplos personagens por conta na versão 1.0.

---

# 6. Níveis, XP e Power Level

## 6.1 Níveis por skill

Cada skill terá:

- XP atual;
- XP total;
- nível;
- máximo configurável;
- curva definida pelo ruleset ativo;
- ranks derivados;
- notificações;
- eventos públicos.

A skill não sobe por pontos administrativos escondidos em listeners. Toda a progressão passa por `SkillProgressionService`.

## 6.2 Power Level

O Power Level será calculado segundo o ruleset oficial da versão ativa do CraftMMO.

Por padrão:

`Power Level = soma dos níveis das skills consideradas no cálculo`

A lista de skills incluídas, child skills e comportamento de skills desativadas deverão seguir o ruleset ativo.

O valor será derivado, não uma segunda fonte autoritativa de progressão.

## 6.3 Modos de progressão

Se forem suportados modos equivalentes a Standard e Retro, cada modo deverá ter:

- curva própria;
- caps próprios;
- escalamento de unlocks;
- conversão explícita;
- migrations testadas;
- impossibilidade de alternar sem dry-run e backup.

Não se deve alterar o modo num servidor em produção sem pré-visualizar o impacto.

## 6.4 XP administrativo

Comandos administrativos poderão:

- dar XP;
- retirar XP;
- definir nível;
- definir XP;
- recalcular Power Level;
- importar dados;
- corrigir perfis.

Todas estas operações deverão:

- ser auditadas;
- ser transacionais;
- validar limites;
- produzir eventos;
- ser idempotentes quando possuírem operation ID.

---

# 7. Catálogo oficial de skills

O ruleset inicial do CraftMMO 1.0.0 contém 19 skills:

## Combate e suporte

1. Archery
2. Axes
3. Crossbows
4. Maces
5. Spears
6. Swords
7. Taming
8. Tridents
9. Unarmed

## Gathering

10. Excavation
11. Fishing
12. Herbalism
13. Mining
14. Woodcutting

## Utilidade e crafting

15. Acrobatics
16. Alchemy
17. Repair
18. Salvage
19. Smelting

Os IDs internos deverão ser estáveis:

- `acrobatics`
- `alchemy`
- `archery`
- `axes`
- `crossbows`
- `excavation`
- `fishing`
- `herbalism`
- `maces`
- `mining`
- `repair`
- `salvage`
- `smelting`
- `spears`
- `swords`
- `taming`
- `tridents`
- `unarmed`
- `woodcutting`

Os nomes visíveis podem ser traduzidos. Os IDs nunca dependem do idioma.

---

# 8. Acrobatics

## Identidade

Skill passiva de mobilidade e mitigação de quedas.

## XP

A fonte de XP, condições, limites e cálculo deverão corresponder à especificação funcional da 1.0.0.

Não se pode conceder XP por:

- dano cancelado;
- quedas artificiais inválidas;
- loops de teleporte;
- eventos marcados como exploit;
- dano não elegível.

## Sub-skills

### Roll

Possibilidade de reduzir ou cancelar dano de queda segundo:

- nível;
- chance;
- condição de queda;
- limite do ruleset da 1.0.0;
- feedback configurado.

### Graceful Roll

Variante melhorada quando o jogador executa a interação exigida durante a queda.

A janela temporal, multiplicador e comportamento devem corresponder exatamente à especificação funcional da 1.0.0.

### Dodge

Possibilidade de reduzir dano recebido.

Deverá respeitar:

- tipos de dano válidos;
- probabilidades;
- cap;
- eventos cancelados;
- diferenças configuradas.

## Critérios de conformidade da referência

- dano final;
- XP;
- probabilidade;
- mensagens;
- ausência de proc em fontes inválidas;
- comportamento em água, teleporte, elytra e veículos;
- ordem de aplicação com outros plugins.

---

# 9. Alchemy

## Identidade

Skill de brewing e expansão de poções.

## XP

XP é obtida pelas ações de brewing elegíveis do ruleset da 1.0.0.

O sistema deverá identificar corretamente:

- proprietário ou participante elegível;
- ingrediente;
- potion type;
- quantidade;
- conclusão real da operação;
- impossibilidade de duplicar XP por chunk unload ou restart.

## Sub-skills

### Catalysis

Melhora a velocidade de brewing de acordo com:

- nível;
- rank;
- multiplicador;
- limites;
- configuração.

### Concoctions

Desbloqueia receitas e combinações adicionais nos níveis corretos.

O motor deverá suportar:

- ingredientes;
- resultados;
- duração;
- amplificador;
- splash;
- lingering;
- compatibilidade com versões de Minecraft;
- validação de conflitos.

## Segurança

- não duplicar poções;
- não conceder XP duas vezes;
- preservar inventário do brewing stand;
- lidar com hopper e automação;
- atribuir ownership de forma determinística;
- recuperar após restart.

---

# 10. Archery

## Identidade

Progressão de combate com bow.

## XP

O cálculo deverá reproduzir:

- dano elegível;
- draw force;
- distância;
- caps;
- PvP XP;
- entidade válida;
- projétil e shooter originais.

## Sub-skills

### Skill Shot

Bónus passivo de dano com ranks e cap correspondentes à especificação funcional da 1.0.0.

### Daze

Proc contra alvos elegíveis, incluindo:

- chance;
- alteração de orientação;
- efeito;
- dano adicional;
- restrição PvP;
- interação com cancelamentos.

### Arrow Retrieval

Recuperação de arrows em mortes elegíveis.

Deverá respeitar:

- Infinity;
- tipo de projétil;
- ownership;
- proteção de regiões;
- quantidade;
- morte válida.

### Archery Limit Break

Bónus contra armadura segundo:

- rank;
- armor quality;
- PvP/PvE;
- configuração;
- cap;
- ordem de dano.

### Explosive Shot

Super ability listada pelo ruleset da 1.0.0.

A ativação, área, dano, proteção de blocos, friendly fire e cooldown deverão ser reproduzidos conforme a versão fixada.

---

# 11. Axes

## Identidade

Combate com axes.

## Sub-skills

### Axe Mastery

Bónus passivo de dano.

### Critical Strikes

Chance e multiplicador de critical hit.

### Greater Impact

Dano e knockback adicionais nas condições corretas.

### Armor Impact

Dano ou desgaste aplicado à armadura segundo o ruleset da 1.0.0.

### Skull Splitter

Super ability de dano em área.

Deverá respeitar:

- ready state;
- alvo principal;
- alcance;
- número de alvos;
- dano;
- PvP;
- party;
- proteção;
- cooldown.

### Axes Limit Break

Bónus contra targets armados conforme armor quality e configuração.

---

# 12. Crossbows

## Identidade

Combate ranged com crossbows.

## XP

Deverá reproduzir:

- distância;
- dano;
- shooter;
- multishot;
- piercing;
- fireworks;
- projéteis recuperáveis;
- regiões protegidas.

## Sub-skills

### Powered Shot

Bónus passivo percentual com cap.

### Trick Shot

Ricochets em blocos.

Deverá preservar:

- direção refletida;
- máximo de ricochets;
- damage;
- critical state;
- pierce;
- knockback;
- potion effects;
- pickup status;
- proteção contra loops;
- attribution de XP.

### Crossbows Limit Break

Bónus de dano contra armadura segundo o ruleset da 1.0.0.

---

# 13. Excavation

## Identidade

Gathering com shovel.

## XP

XP por blocos elegíveis, com tabela versionada.

O sistema deverá distinguir:

- blocos naturais;
- blocos colocados;
- blocos regenerados;
- blocos movidos;
- Silk Touch;
- ferramentas válidas;
- regiões desativadas.

## Sub-skills

### Archaeology

Treasure drops por bloco e nível.

Cada entrada deverá definir:

- item;
- quantidade;
- chance;
- nível mínimo;
- blocos de origem;
- XP adicional;
- versão;
- regras de encantamentos;
- proteção de inventário e região.

### Giga Drill Breaker

Super ability de digging.

Deverá reproduzir:

- ready state;
- ferramenta válida;
- enchant buff temporário;
- duração;
- rolls de treasure;
- XP;
- cooldown;
- restauração segura do item.

---

# 14. Fishing

## Identidade

Gathering e treasure hunting através de fishing.

## Sub-skills

### Fisherman's Diet

Melhorias ao consumo de alimentos elegíveis.

### Ice Fishing

Capacidade de pescar em gelo nas condições do ruleset da 1.0.0.

### Magic Hunter

Possibilidade de treasures encantados.

Deverá respeitar:

- encantamentos possíveis;
- conflitos;
- unsafe enchantments;
- níveis;
- material;
- chance;
- configuração do ruleset da 1.0.0.

### Master Angler

Modificadores de chance ou velocidade segundo ambiente, biome, boat e condições relevantes.

### Treasure Hunter

Loot tables de pesca, raridades, níveis, chances e junk.

### Shake

Drops obtidos de entidades elegíveis através da mecânica prevista.

## Requisitos especiais

Fishing é uma das skills mais complexas e deverá possuir:

- RNG injetável;
- loot manifest versionado;
- testes estatísticos;
- proteção contra AFK farms;
- ownership do hook;
- tracking de cast;
- cancelamento correto;
- prevenção de duplicação;
- compatibilidade com off-hand.

---

# 15. Herbalism

## Identidade

Gathering através de crops, plantas e blocos vegetais.

## Sub-skills

### Double Drops

Chance de drops adicionais.

### Verdant Bounty

Roll adicional de recompensa nos níveis e condições corretos.

### Farmer's Diet

Melhoria de alimentos elegíveis.

### Green Thumb

Replanting e transformação de blocos/crops conforme o ruleset da 1.0.0.

Deverá validar o material real consumido e impedir seeds renomeadas ou falsificadas.

### Green Terra

Super ability.

Deverá reproduzir:

- ready state;
- duração;
- crops afetadas;
- drops;
- replanting;
- transformação;
- cooldown.

### Hylian Luck

Drops especiais de blocos elegíveis.

### Shroom Thumb

Transformações com mushrooms e blocos compatíveis.

## Anti-exploit

- sem XP de crops colocadas e quebradas em loop quando proibido;
- consumo real de seeds;
- proteção de região;
- compatibilidade com auto farms;
- autoria de blocos;
- eventos de drop respeitados.

---

# 16. Maces

## Identidade

Combate melee com mace.

## Sub-skills

### Crush

Bónus passivo de dano com ranks.

### Cripple

Chance de aplicar Slowness com:

- ranks;
- duração PvP;
- duração PvE;
- attack strength;
- verificação de efeito existente.

### Maces Limit Break

Bónus contra armadura segundo armor quality.

A versão 1.0.0 reproduzirá o comportamento selecionado desta skill. Alterações posteriores serão tratadas como evolução própria do CraftMMO e documentadas nas release notes.

---

# 17. Mining

## Identidade

Gathering com pickaxe e TNT.

## XP

Tabela de XP por bloco versionada.

Deverá suportar:

- ores;
- deepslate ores;
- terrain;
- decorative blocks;
- amethyst;
- sculk;
- coral;
- novos materiais da versão alvo.

## Sub-skills

### Double Drops

Chance passiva de drop adicional.

### Mother Lode

Roll adicional depois de Double Drops nas condições corretas.

### Super Breaker

Super ability com:

- ready state;
- Efficiency temporária;
- duração;
- triple drops;
- ferramenta;
- cooldown;
- restauração de item.

### Blast Mining

Mecânica com TNT.

Deverá reproduzir:

- remote detonation;
- ownership da TNT;
- ranks;
- ore bonus;
- debris reduction;
- self-damage reduction;
- drop multiplier;
- radius bonus;
- proteção de regiões;
- ausência de XP indevida;
- attribution após logout ou chunk unload.

---

# 18. Repair

## Identidade

Repair de ferramentas e armadura através do anvil definido pelo ruleset da 1.0.0.

## Sub-skills

### Repair Mastery

Aumento da quantidade reparada.

### Super Repair

Chance de reparar quantidade adicional.

### Arcane Forging

Preservação, perda ou downgrade de enchantments.

## Requisitos

- confirmação quando aplicável;
- material correto;
- consumo atómico;
- cálculo por durabilidade;
- custom model data;
- itens unbreakable;
- enchantments;
- impossibilidade de dupe;
- inventário cheio;
- cancelamento;
- rollback em falha.

---

# 19. Salvage

## Identidade

Child skill de Repair e Fishing.

O nível deverá ser derivado exatamente:

`Salvage Level = floor((Repair Level + Fishing Level) / 2)`

Não se ganha Salvage XP diretamente.

## Sub-skills

### Scrap Collector

Recuperação de materiais segundo:

- rank;
- durabilidade;
- receita;
- cap;
- material;
- item elegível.

### Arcane Salvage

Extração de enchantments com:

- full extraction;
- partial extraction;
- loss;
- max enchant level;
- unsafe enchantments;
- permissões especiais.

## Requisitos

- gold block ou estação definida pelo ruleset da 1.0.0;
- confirmação temporal;
- operação transacional;
- item consumido apenas uma vez;
- output calculado antes de mutar inventário;
- rollback seguro.

---

# 20. Smelting

## Identidade

Child skill de Mining e Repair.

O nível deverá ser derivado exatamente:

`Smelting Level = floor((Mining Level + Repair Level) / 2)`

Não se ganha Smelting XP diretamente.

A XP de operações elegíveis deverá ser distribuída pelas parent skills conforme o ruleset da 1.0.0.

## Sub-skills

### Fuel Efficiency

Multiplicador de burn time com cap seguro.

### Second Smelt

Chance de resultado adicional, respeitando stack limits e materiais desativados.

### Understanding the Art

Multiplicador de XP vanilla da furnace.

## Requisitos

- ownership ou attribution determinística;
- hopper support;
- chunk unload;
- restart;
- múltiplos jogadores;
- inventory capacity;
- overflow;
- nenhuma duplicação.

---

# 21. Spears

## Identidade

Combate melee com spear conforme o material introduzido na versão alvo de Minecraft.

## Sub-skills

### Spear Mastery

Bónus passivo de dano com ranks.

### Momentum

Chance de Speed ao acertar:

- rank;
- duration;
- amplifier;
- attack strength;
- efeito existente;
- target eligibility.

### Spears Limit Break

Bónus contra armadura segundo o ruleset da 1.0.0.

---

# 22. Swords

## Identidade

Combate melee com swords.

## Sub-skills

### Stab

Bónus passivo de dano.

### Rupture

Bleed periódico.

Deverá reproduzir:

- chance;
- rank;
- duração;
- refresh;
- tick interval;
- dano PvP;
- dano PvE;
- pure damage;
- blocking;
- ownership;
- morte do source;
- logout.

### Serrated Strikes

Super ability em área.

Deverá respeitar:

- ready state;
- alvo principal;
- raio;
- material da weapon;
- máximo de alvos;
- percentagem de dano;
- aplicação de Rupture;
- party;
- friendly fire;
- cooldown.

### Counter Attack

Proc de reflexão de dano.

### Swords Limit Break

Bónus contra armor quality.

---

# 23. Taming

## Identidade

Progressão de companions e animais domesticados.

## XP

XP através de:

- domesticação elegível;
- dano causado por companion elegível;
- ownership;
- species values;
- combat multipliers.

## Sub-skills

### Beast Lore

Inspeção de animais.

### Call of the Wild

Invocação temporária de companions através dos itens e quantidades definidos.

### Environmentally Aware

Proteção contra morte ambiental elegível.

### Fast Food Service

Chance de cura por dano causado.

### Gore

Bónus de dano e aplicação de bleed.

### Holy Hound

Conversão de certos tipos de dano em cura.

### Pummel

Chance de knockback.

### Sharpened Claws

Bónus de dano.

### Shock Proof

Redução de explosion e lightning damage.

### Thick Fur

Redução física e proteção de fire damage.

## Requisitos

- ownership robusto;
- limpeza no logout;
- mundo correto;
- chunk unload;
- death;
- teleport;
- limites de summon;
- persistência apenas quando prevista;
- prevenção de farming entre contas;
- compatibilidade com variantes modernas de animais.

---

# 24. Tridents

## Identidade

Combate melee e ranged com tridents.

## Sub-skills

### Impale

Bónus passivo de dano.

Deverá distinguir:

- melee hit;
- thrown hit;
- attack strength;
- projectile ownership;
- Loyalty;
- Riptide;
- Channeling;
- retorno após logout.

### Tridents Limit Break

Bónus contra armor quality.

---

# 25. Unarmed

## Identidade

Combate sem weapon.

## Sub-skills

### Steel Arm Style

Bónus passivo de dano.

### Berserk

Super ability.

Deverá reproduzir:

- ativação;
- multiplicador;
- attack strength;
- duração;
- block interactions;
- cooldown.

### Arrow Deflect

Chance de cancelar projectile damage.

### Disarm

Chance de retirar a weapon de outro player.

Deverá tratar:

- PvP only;
- inventory;
- drop;
- AntiTheft;
- regiões;
- item binding;
- cancelamento.

### Iron Grip

Counter-proc contra Disarm.

### Block Cracker

Conversão de blocos elegíveis.

### Unarmed Limit Break

Bónus contra armor quality.

---

# 26. Woodcutting

## Identidade

Gathering com axe em árvores e materiais elegíveis.

## Sub-skills

### Harvest Lumber

Drop adicional.

### Clean Cuts

Roll adicional depois do bónus anterior quando aplicável.

### Leaf Blower

Remoção eficiente de leaves segundo nível e condições.

### Tree Feller

Super ability que derruba estruturas arbóreas elegíveis.

Deverá possuir:

- deteção limitada;
- orçamento por tick;
- limite máximo de blocos;
- proteção contra estruturas artificiais;
- tool durability;
- drops;
- XP;
- regiões;
- cooldown;
- cancelamento seguro;
- continuação controlada em árvores grandes.

### Knock on Wood

Recompensas adicionais e XP vanilla nas condições do ruleset da 1.0.0.

## Performance

Tree Feller nunca poderá fazer scans ilimitados ou processar milhares de blocos num único tick.

---

# 27. Manifesto da referência funcional da 1.0.0

A referência utilizada para construir a versão 1.0.0 terá um diretório imutável:

```text
reference/
└── mcmmo/
    └── 2.2.053/
        ├── manifest.yml
        ├── skills/
        ├── experience/
        ├── abilities/
        ├── treasures/
        ├── potions/
        ├── combat/
        ├── permissions/
        └── known-edge-cases.yml
```

O manifest deverá guardar:

- produto e versão de referência;
- commit, build ou artefacto de referência quando disponível;
- data em que a referência foi congelada;
- versão de Minecraft;
- versão de Paper;
- lista de skills;
- defaults observados;
- fórmulas;
- unlocks;
- rank tables;
- cooldowns;
- loot tables;
- event ordering assumptions;
- bugs observados reproduzidos ou corrigidos;
- diferenças conhecidas;
- estado de verificação de cada comportamento.

O diretório existe para tornar a construção da 1.0.0 reproduzível. Não representa suporte contínuo a versões mcMMO nem um sistema de compatibilidade selecionável em runtime.

## Política para comportamentos defeituosos da referência

Para cada bug observado deverá existir uma decisão explícita:

- `REPRODUCE_FOR_1_0`: reproduzir na 1.0.0 para manter o comportamento funcional escolhido;
- `FIX_IN_1_0`: corrigir antes do lançamento e documentar a diferença;
- `NOT_APPLICABLE`: não se aplica à arquitetura do CraftMMO;
- `NEEDS_REVIEW`: comportamento ainda não confirmado.

Nunca se deve corrigir silenciosamente uma diferença e continuar a declarar que o cenário foi reproduzido.

---

# 28. Ruleset oficial e personalização

## Perfil oficial da versão

Configuração padrão:

`rules.profile: CRAFTMMO_1_0`

Neste perfil:

- as fórmulas seguem o ruleset oficial do CraftMMO 1.0.0;
- equipment próprio não altera chances, ranks, XP ou cooldowns das skills, salvo quando a especificação da versão o definir;
- quests não alteram cooldowns das skills;
- events não duplicam drops;
- custom mobs são tratados através das regras de elegibilidade definidas;
- plugins externos só alteram resultados através de contratos públicos permitidos.

Este perfil define o comportamento oficial da release. A sua estabilidade é um contrato do CraftMMO, não um contrato de compatibilidade com o mcMMO.

## Perfil customizado

Opcional:

`rules.profile: CUSTOM`

Neste perfil, administradores podem ativar:

- equipamento que modifica skills;
- boosters;
- eventos;
- zonas;
- buffs;
- affixes;
- perks;
- overrides de fórmulas e loot permitidos pela configuração.

Ao ativar este perfil, o servidor deixa de executar o ruleset oficial sem alterações. O sistema deverá mostrar quais valores foram modificados e de onde veio cada override.

## Evolução de rulesets

Versões posteriores do CraftMMO poderão introduzir novos rulesets oficiais, substituir regras antigas ou executar migrations.

O projeto não assume a obrigação de preservar todos os rulesets históricos indefinidamente. O suporte e o caminho de upgrade serão definidos pelas release notes do CraftMMO.

---

# 29. Pipeline central de XP

Todos os ganhos passam por:

```text
Paper event
→ event adapter
→ eligibility validation
→ provenance validation
→ anti-exploit
→ ruleset XP lookup
→ ruleset multipliers
→ allowed server modifiers
→ diminished returns, se ativo
→ transaction identity
→ SkillProgressionService
→ level transitions
→ events
→ feedback
→ async persistence
```

O pipeline deverá receber um `ProgressionOperationId` estável quando existir risco de repetição.

A mesma operação não pode conceder XP duas vezes.

---

# 30. Super abilities

Uma super ability terá:

- skill;
- activation method;
- ready state;
- ready timeout;
- valid tool;
- minimum level;
- duration;
- cooldown;
- shared cooldown group;
- start timestamp;
- end timestamp;
- source item snapshot;
- cancellation conditions;
- persistence policy;
- effects;
- feedback.

## Cancelamentos possíveis

- morte;
- logout;
- mudança de mundo;
- item inválido;
- perda de permissão;
- região proibida;
- reload incompatível;
- shutdown;
- evento cancelado.

## Relógio

Cooldowns persistentes deverão utilizar tempo monotónico durante a sessão e timestamps absolutos apenas para persistência e recuperação.

Não se deve usar ticks restantes como única fonte persistente.

---

# 31. Combate

O CraftMMO não precisa substituir todo o combate vanilla para implementar as skills do seu ruleset.

A regra será:

- preservar o pipeline vanilla/Paper sempre que possível;
- aplicar apenas modificadores necessários à especificação funcional da 1.0.0;
- respeitar cancelamentos;
- evitar dano recursivo;
- marcar damage sources internos;
- impedir XP a partir de dano gerado pelo próprio sistema quando o ruleset ativo não o permite.

Cada alteração deverá declarar:

- fase do evento;
- valor de entrada;
- valor alterado;
- tipo de dano;
- source;
- target;
- skill;
- sub-skill;
- attack strength;
- PvP/PvE;
- operação de origem.

---

# 32. Equipamentos e itens personalizados

O CraftMMO mantém o sistema de custom items do CraftRPG.

Slots suportados:

- helmet;
- chestplate;
- leggings;
- boots;
- main hand;
- off-hand;
- necklace;
- ring 1;
- ring 2;
- amulet;
- trinket;
- cape opcional.

Itens podem ter:

- ID interno;
- material;
- nome;
- lore;
- rarity;
- requisitos;
- stats;
- passives;
- set;
- sockets futuros;
- durability;
- bind;
- owner;
- custom model data;
- flags;
- creation metadata.

## Regra do perfil oficial

No perfil `CRAFTMMO_1_0`, custom item stats não alteram:

- chances das sub-skills;
- ranks;
- XP;
- cooldown;
- duração;
- drops;
- fórmulas do ruleset oficial.

Um custom item pode continuar a possuir atributos vanilla ou efeitos próprios, mas esses efeitos deverão estar isolados e ser identificáveis no pipeline.

Nunca se confia apenas em nome ou lore. Itens usam Persistent Data Container e IDs internos.

---

# 33. Criaturas, elites e bosses

O sistema de criaturas do CraftRPG permanece.

Templates podem definir:

- ID;
- entity type;
- nome;
- nível recomendado;
- health;
- damage;
- armor;
- resistances;
- speed;
- aggro;
- leash;
- skills próprias;
- loot;
- XP de quests;
- moeda;
- tags;
- sounds;
- particles.

## Relação com as skills do CraftMMO

Cada mob deverá definir:

- elegibilidade para XP de combat skill;
- XP value ou modifier;
- elegibilidade para Taming XP;
- elegibilidade para Shake;
- imunidades;
- anti-farm identity;
- summoned status;
- boss status;
- player-created status.

Bosses não deverão conceder XP repetido por adds infinitos ou fases recicláveis.

---

# 34. Quests

O sistema de quests permanece, mas não depende de classes.

Objetivos:

- matar criatura;
- matar boss;
- recolher item;
- entregar item;
- falar com NPC;
- visitar local;
- descobrir zona;
- usar skill;
- ativar super ability;
- atingir nível numa skill;
- atingir Power Level;
- pescar item;
- reparar item;
- preparar potion;
- sobreviver;
- proteger NPC;
- completar sequência.

Recompensas:

- XP numa skill específica;
- XP distribuída por conjunto definido;
- moeda;
- item;
- título;
- acesso;
- checkpoint;
- loot table;
- achievement.

XP de quest deverá ser uma origem explícita e configurável. Não deverá fingir que resultou de uma ação vanilla.

---

# 35. Parties

Parties permanecem parte do núcleo de conteúdo.

Funcionalidades:

- criação;
- líder;
- invites;
- expiração;
- chat;
- kick;
- promote;
- disband;
- loot mode;
- quest credit;
- interface;
- localização opcional.

## XP de skills e parties

Por padrão, XP de atividade individual não é partilhada.

Exemplos:

- Mining pertence a quem partiu o bloco;
- Fishing pertence a quem controlava o hook;
- Repair pertence a quem executou a operação;
- combat XP pertence ao participante elegível segundo o ruleset da 1.0.0.

Quest XP, boss rewards e conteúdo próprio podem ser partilhados segundo regras explícitas.

Não se deve aplicar genericamente a partilha de party a todas as skills, pois isso alteraria o comportamento oficial da versão 1.0.0.

---

# 36. Loot

O motor geral de loot permanece separado das loot tables de skills.

Existem duas autoridades:

## Skill loot

Exemplos:

- Archaeology;
- Treasure Hunter;
- Hylian Luck;
- Shake;
- Double Drops;
- Blast Mining;
- Harvest Lumber.

Estas tabelas seguem o ruleset oficial da versão 1.0.0.

## Content loot

Exemplos:

- mobs;
- elites;
- bosses;
- quests;
- chests;
- events.

Estas tabelas pertencem ao CraftMMO.

Uma recompensa não pode ser processada pelos dois motores sem uma regra explícita.

Loot de bosses deverá preferir loot pessoal.

---

# 37. Zonas e checkpoints

Zonas podem definir:

- ID;
- nome;
- bounds;
- nível de Power Level recomendado;
- níveis de skills recomendados;
- mobs;
- quests;
- sounds;
- PvP;
- checkpoints;
- discovery rewards;
- skills desativadas;
- modifiers de skills apenas no perfil `CUSTOM`.

Fast travel poderá existir entre checkpoints, com:

- custo;
- channel;
- bloqueio em combat;
- permissões;
- mundos permitidos.

---

# 38. Achievements, codex e leaderboards

O codex poderá registar:

- skills;
- milestones;
- mobs;
- bosses;
- items;
- zones;
- quests;
- lore;
- achievements.

Leaderboards:

- cada skill;
- Power Level;
- bosses;
- achievements;
- fishing treasures;
- damage;
- quest completion;
- playtime.

Queries pesadas deverão ser assíncronas e usar cache.

---

# 39. Comandos

## Namespace principal

```text
/craftmmo
/craftmmo help
/craftmmo profile [player]
/craftmmo stats [player]
/craftmmo skills
/craftmmo skill <skill>
/craftmmo power
/craftmmo top [skill|power]
/craftmmo rank [player]
/craftmmo quests
/craftmmo party
/craftmmo equipment
/craftmmo codex
/craftmmo achievements
/craftmmo settings
/craftmmo version
```

## Aliases familiares opcionais

Opcionalmente:

```text
/mcstats
/mctop
/mcrank
/mmopower
/acrobatics
/alchemy
/archery
/axes
/crossbows
/excavation
/fishing
/herbalism
/maces
/mining
/repair
/salvage
/smelting
/spears
/swords
/taming
/tridents
/unarmed
/woodcutting
```

Os aliases deverão ser configuráveis.

A instalação simultânea do mcMMO e do CraftMMO não é suportada.

Se o mcMMO for detetado:

- o bootstrap interrompe a ativação do motor de skills;
- apresenta uma mensagem clara sobre dupla progressão e dupla autoridade;
- não tenta roubar comandos ou processar eventos parcialmente;
- permite apenas ferramentas explicitamente offline de importação, quando aplicável.

---

# 40. Help e tab completion

`/craftmmo help` deverá:

- mostrar comandos disponíveis;
- respeitar permissões;
- suportar páginas;
- procurar por comando ou skill;
- mostrar aliases;
- apresentar argumentos;
- fornecer exemplos;
- usar MiniMessage;
- oferecer click e hover;
- respeitar idioma.

Tab completion deverá mostrar apenas:

- comandos autorizados;
- skills ativas;
- players elegíveis;
- IDs válidos;
- opções contextuais.

IDs administrativos não devem ser expostos sem permissão.

---

# 41. Menus

Menus principais:

- perfil;
- resumo de skills;
- detalhe de skill;
- ranks e unlocks;
- Power Level;
- cooldowns;
- equipamento;
- quests;
- party;
- codex;
- achievements;
- leaderboards;
- settings;
- help.

Os menus deverão:

- usar holders ou IDs internos;
- não confiar no título;
- validar clicks;
- impedir item movement;
- suportar paginação;
- atualizar com frequência limitada;
- respeitar permissões;
- funcionar sem serem obrigatórios.

---

# 42. Configurações do jogador

Opções:

- idioma;
- sons;
- partículas;
- intensidade de partículas;
- action bar;
- notifications de XP;
- notifications de level up;
- notifications de sub-skill;
- ready messages;
- cooldown messages;
- compact numbers;
- quest tracking;
- party requests;
- interface reduzida;
- confirmação de ações destrutivas.

---

# 43. Administração

Comandos principais:

```text
/craftmmo admin help
/craftmmo admin validate
/craftmmo admin reload <scope>
/craftmmo admin profile <player>
/craftmmo admin skill set <player> <skill> <level>
/craftmmo admin skill xp give <player> <skill> <amount>
/craftmmo admin skill xp take <player> <skill> <amount>
/craftmmo admin cooldown reset <player> [skill]
/craftmmo admin power recalculate <player>
/craftmmo admin quest ...
/craftmmo admin item give ...
/craftmmo admin mob spawn ...
/craftmmo admin currency ...
/craftmmo admin debug <player>
/craftmmo admin migrate ...
/craftmmo admin export ...
/craftmmo admin reference conformance report
```

Todas as ações administrativas relevantes produzem audit logs.

---

# 44. Configuração

Estrutura proposta:

```text
plugins/CraftMMO/
├── config.yml
├── database.yml
├── redis.yml
├── messages.yml
├── progression.yml
├── abilities.yml
├── combat.yml
├── anti-exploit.yml
├── compatibility.yml
├── skills/
├── experience/
├── treasures/
├── potions/
├── items/
├── sets/
├── mobs/
├── bosses/
├── quests/
├── loot-tables/
├── zones/
├── shops/
├── achievements/
├── languages/
└── state/
    └── block-provenance/
```

Os valores observados na referência deverão pertencer ao manifest da referência. Os valores oficiais do CraftMMO e os overrides do servidor ficam separados.

Isto permite distinguir:

- valor oficial do ruleset CraftMMO;
- override local;
- valor efetivo;
- origem do valor.

---

# 45. Validação e reload

No arranque e reload, validar:

- skill IDs;
- ruleset ativo;
- formulas;
- ranks;
- unlock levels;
- chances;
- materials;
- entities;
- enchantments;
- potion types;
- treasure tables;
- recipes;
- parent skills;
- child skill formulas;
- cooldown groups;
- command aliases;
- permissions;
- referências de quests;
- loot tables;
- zonas;
- mensagens;
- migrations;
- compatibilidade de Minecraft.

## Reload seguro

Fluxo:

1. ler;
2. parse;
3. validar schema;
4. validar semântica;
5. resolver referências;
6. construir snapshot imutável;
7. comparar ruleset e referência aplicável;
8. produzir diff;
9. aplicar apenas se válido;
10. manter snapshot anterior para operações em curso.

Cada snapshot terá `ContentVersion`.

Operações ativas deverão guardar a versão com que começaram.

---

# 46. PostgreSQL

PostgreSQL é a fonte autoritativa dos dados persistentes de jogadores e conteúdo operacional.

Pode ser:

- Supabase;
- PostgreSQL local;
- PostgreSQL remoto;
- serviço PostgreSQL gerido.

O core não deverá depender de APIs específicas do Supabase.

## Requisitos

- connection pool;
- prepared statements;
- migrations versionadas;
- transactions;
- optimistic locking;
- operation IDs;
- idempotency;
- retries limitados;
- timeouts;
- health checks;
- backups;
- recovery documentada.

Nenhuma query bloqueia a main thread.

## Operações transacionais críticas

- XP administrativo;
- rewards de quests;
- boss rewards;
- currency;
- item grants persistentes;
- imports;
- profile migration;
- pity;
- party mutations;
- ownership de sessão.

---

# 47. Redis

Redis é opcional e complementar.

Usos permitidos:

- cache;
- pub/sub;
- invalidação;
- session ownership;
- distributed locks;
- presence;
- rate limits;
- coordination;
- leaderboard cache.

Redis nunca será:

- autoridade única de XP;
- autoridade única de nível;
- única cópia de inventory state;
- substituto do PostgreSQL.

O CraftMMO deverá funcionar num servidor único sem Redis.

Em falha de Redis:

- single-server continua;
- funcionalidades distribuídas degradam explicitamente;
- locks não são assumidos válidos;
- nenhum dado autoritativo é perdido.

---

# 48. Proveniência de blocos

A prevenção de XP e drops por blocos colocados exige tracking de proveniência.

Guardar cada bloco individual no PostgreSQL seria tecnicamente fraco.

A solução deverá usar armazenamento por região ou chunk, próximo dos mundos:

- formato binário versionado;
- escrita atómica;
- checksums;
- backups;
- compactação;
- lazy loading;
- cache limitada;
- flush no shutdown;
- recuperação após crash;
- migrations de layout de mundos.

O sistema deverá acompanhar:

- placed blocks;
- moved blocks quando relevante;
- generated blocks;
- regenerated blocks;
- world;
- coordinates;
- version;
- timestamps apenas quando necessários.

Mudanças de layout de Paper deverão ter:

- detector;
- dry-run;
- backup;
- migration idempotente;
- relatório;
- rollback documentado.

---

# 49. Cache e sessões

Login:

1. validar UUID;
2. adquirir ownership;
3. carregar profile;
4. aplicar migrations;
5. validar versão do ruleset;
6. construir sessão;
7. restaurar cooldowns;
8. calcular níveis derivados;
9. disponibilizar ações.

Enquanto carrega, ações CraftMMO ficam bloqueadas de forma controlada.

Logout:

1. bloquear novas operações;
2. concluir ou cancelar operações;
3. persistir dirty state;
4. libertar ownership;
5. limpar tasks;
6. remover references;
7. limpar summons temporários;
8. invalidar cache.

Dois servidores não podem ser autoridade simultânea do mesmo jogador.

---

# 50. Modelo de dados

Tabelas aproximadas:

```text
players
player_skill_progress
player_skill_statistics
player_cooldowns
player_settings
player_quests
player_achievements
player_discoveries
player_currencies
player_pity
player_equipment
parties
party_members
transactions
reward_operations
session_ownership
content_versions
migration_history
admin_audit_log
```

`player_skill_progress` deverá ter, no mínimo:

- player UUID;
- skill ID;
- level;
- current XP;
- total XP;
- version;
- updated_at.

Child skills podem ser calculadas, não duplicadas como autoridade.

Não se guardará o perfil inteiro num único JSON gigante.

---

# 51. Arquitetura do projeto

Módulos:

```text
craftmmo-api
craftmmo-core
craftmmo-paper
craftmmo-storage
craftmmo-content
craftmmo-skills
craftmmo-reference
craftmmo-integrations
craftmmo-migration
craftmmo-testkit
```

## craftmmo-api

- contratos públicos;
- modelos;
- eventos;
- registries;
- extension points.

## craftmmo-core

- domínio;
- progressão;
- quests;
- parties;
- loot;
- zonas;
- regras independentes de Paper.

## craftmmo-paper

- listeners;
- commands;
- menus;
- schedulers;
- entities;
- inventories;
- PDC;
- presentation.

## craftmmo-storage

- PostgreSQL;
- repositories;
- migrations;
- transactions;
- Redis;
- caches;
- session ownership.

## craftmmo-content

- loaders;
- schemas;
- validation;
- snapshots;
- reference resolution.

## craftmmo-skills

- implementações das 19 skills;
- sub-skills;
- abilities;
- XP adapters;
- cooldowns;
- rewards.

## craftmmo-reference

- manifests;
- reference scenarios;
- differential assertions;
- deviation registry;
- reference conformance reports.

## craftmmo-integrations

- WorldGuard;
- PlaceholderAPI;
- Vault;
- Citizens;
- LuckPerms;
- ProtocolLib ou PacketEvents;
- MythicMobs;

## craftmmo-migration

- import mcMMO;
- export;
- dry-run;
- mapping;
- backups;
- verification.

## craftmmo-testkit

- fake clock;
- seeded RNG;
- fake repositories;
- event fixtures;
- skill scenarios;
- reference verification harness.

---

# 52. Serviços principais

```text
PlayerProfileService
SessionService
SkillRegistry
SkillProgressionService
SkillXpService
PowerLevelService
AbilityService
CooldownService
CombatSkillService
GatheringSkillService
CraftingSkillService
ChildSkillService
BlockProvenanceService
AntiExploitService
RandomService
ClockService
ItemService
EquipmentService
LootService
MobService
BossService
QuestService
PartyService
ZoneService
CurrencyService
AchievementService
LeaderboardService
StorageService
MigrationService
ReferenceVerificationService
LocalizationService
MenuService
AuditService
```

Listeners encaminham eventos para serviços.

Listeners não deverão conter fórmulas de negócio extensas.

---

# 53. API pública

A API permitirá:

- consultar perfil;
- consultar nível e XP;
- consultar Power Level;
- conceder XP através de origem validada;
- ouvir level ups;
- consultar cooldowns;
- consultar abilities;
- iniciar quests;
- consultar equipment;
- modificar currency;
- registar objetivos;
- consultar parties;
- consultar zonas;
- adicionar providers controlados.

Eventos propostos:

```text
CraftMmoProfileLoadEvent
CraftMmoSkillXpPreGainEvent
CraftMmoSkillXpGainEvent
CraftMmoSkillLevelUpEvent
CraftMmoPowerLevelChangeEvent
CraftMmoAbilityReadyEvent
CraftMmoAbilityActivateEvent
CraftMmoAbilityEndEvent
CraftMmoAbilityFailEvent
CraftMmoSubSkillProcEvent
CraftMmoDamageEvent
CraftMmoGatheringRewardEvent
CraftMmoQuestStartEvent
CraftMmoQuestProgressEvent
CraftMmoQuestCompleteEvent
CraftMmoBossDefeatEvent
CraftMmoZoneDiscoverEvent
```

Durante `0.x`, a API será experimental.

Só fica estável depois de uso interno e testes de contrato e referência.

---

# 54. Integrações

Integrações opcionais:

- PlaceholderAPI;
- Vault;
- LuckPerms;
- Citizens;
- WorldGuard;
- ProtocolLib ou PacketEvents;
- MythicMobs;
- holograms;
- scoreboard;


Cada integração terá:

- detector;
- adapter;
- capability report;
- config;
- fallback;
- mensagens de estado;
- testes de ausência.

O plugin inicia sem integrações opcionais.

---

# 55. Placeholders

Exemplos:

```text
%craftmmo_power_level%
%craftmmo_skill_mining_level%
%craftmmo_skill_mining_xp%
%craftmmo_skill_mining_xp_required%
%craftmmo_skill_swords_level%
%craftmmo_ability_mining_cooldown%
%craftmmo_highest_skill%
%craftmmo_party_size%
%craftmmo_active_quest%
```

Placeholders dinâmicos deverão ser cacheados por request curto e nunca executar queries pesadas.

---

# 56. Idiomas e mensagens

Nenhuma mensagem relevante fica hardcoded.

Suporte:

- `pt_PT`;
- `en_US`;
- idiomas adicionais;
- MiniMessage;
- placeholders;
- hover;
- click;
- pluralização simples;
- compact numbers.

IDs e behavior permanecem independentes do idioma.

Não é necessário copiar texto oficial do mcMMO para reproduzir o comportamento mecânico selecionado.

---

# 57. Segurança e anti-exploit

O plugin deverá proteger contra:

- placed-block XP farming;
- crop loops;
- tree farms exploradas fora das regras;
- TNT ownership spoofing;
- fishing AFK abuse;
- projectile duplication;
- combat alt farming;
- repair duplication;
- salvage duplication;
- smelting duplication;
- brewing duplication;
- repeated quest rewards;
- repeated boss rewards;
- reconnect para resetar estado;
- logout para resetar cooldown;
- inventory manipulation;
- fake custom items;
- command spam;
- packet spam;
- race conditions;
- double session;
- stale Redis locks;
- duplicate async callbacks.

Nunca se confia em:

- lore;
- display name;
- slot enviado pelo cliente;
- inventory title;
- ordem acidental de eventos;
- cache como autoridade;
- entidade sem ownership validado.

---

# 58. Performance

Regras:

- nenhuma query pesada na main thread;
- nenhuma chamada Redis bloqueante na main thread;
- nenhum scan global de entities por tick;
- nenhum scan ilimitado de árvore;
- nenhum scheduler por efeito individual se puder ser agregado;
- caches limitadas;
- particle budgets;
- menu refresh controlado;
- leaderboards cacheadas;
- provenance cache por região;
- batch writes;
- profiling opcional;
- métricas por skill.

Orçamentos deverão ser publicados para:

- event processing;
- Tree Feller;
- Blast Mining;
- fishing loot;
- block provenance;
- login;
- save;
- reload.

---

# 59. Logging e debug

Logs:

- bootstrap;
- ruleset ativo;
- content;
- migrations;
- PostgreSQL;
- Redis;
- region storage;
- warnings;
- invalid configs;
- failed rewards;
- duplicate operation attempts;
- session conflicts;
- admin actions;
- exploit signals.

Debug poderá mostrar:

- skill;
- level;
- rank;
- XP source;
- XP base;
- multipliers;
- final XP;
- RNG roll;
- chance;
- cooldown;
- provenance;
- damage pipeline;
- operation ID;
- snapshot version;
- ruleset version.

Comando:

`/craftmmo admin debug <player>`

---

# 60. Testes

## Unit tests

- XP curves;
- levels;
- ranks;
- Power Level;
- child skill formulas;
- cooldowns;
- duration;
- chances;
- damage;
- drops;
- loot;
- repair;
- salvage;
- smelting;
- brewing;
- session ownership;
- idempotency;
- validation;
- migrations.

## Integration tests

- PostgreSQL;
- Redis opcional;
- login/logout;
- restart;
- async saves;
- rollback;
- region storage;
- Paper events;
- inventories;
- commands;
- permissions.

## Testes diferenciais de referência

Executar o mesmo cenário controlado em:

1. mcMMO 2.2.053 num ambiente de referência controlado;
2. CraftMMO 1.0.0 com o ruleset oficial.

Comparar:

- XP;
- level;
- rank;
- damage;
- effects;
- drops;
- cooldown;
- duration;
- inventory mutation;
- messages relevantes;
- event cancellation;
- persisted state.

Cada skill terá uma matriz de cenários:

- normal;
- boundary;
- max level;
- invalid action;
- protected region;
- PvP;
- PvE;
- death;
- logout;
- restart;
- reload;
- conflicting plugin event;
- RNG seeded/statistical.

## Relatório de conformidade da referência

A build deverá produzir:

```text
Skill: Mining
Initial reference: mcMMO 2.2.053
Scenarios: 184
Pass: 184
Fail: 0
Accepted deviations: 0
Status: REFERENCE_CONFORMANT
```

A release 1.0 não pode declarar conformidade com a referência nos cenários que tenham falhas desconhecidas. A declaração pública deverá limitar-se aos comportamentos e cenários efetivamente verificados.

---

# 61. RNG e determinismo

Todas as probabilidades passam por `RandomService`.

Em produção:

- gerador adequado;
- seed não previsível quando necessário.

Em testes:

- seeded RNG;
- sequência controlável;
- reproducibilidade.

Nenhuma skill deve instanciar `Random` diretamente em listeners.

Testes estatísticos deverão validar distribuições com tolerância definida, sem flaky tests.

---

# 62. Licenciamento e estratégia legal

O repositório oficial mcMMO é distribuído sob GPL-3.0.

Antes de escrever o motor de skills, deverá ser escolhida uma estratégia legal e técnica:

## Estratégia A — derivada GPL

- reutilizar ou adaptar código GPL;
- distribuir CraftMMO sob termos compatíveis;
- preservar notices;
- disponibilizar source conforme exigido;
- documentar alterações.

## Estratégia B — implementação comportamental independente

- produzir a especificação a partir de comportamento observável, documentação pública e materiais legalmente permitidos;
- escrever a implementação independentemente;
- não copiar textos, assets ou código sem cumprir a licença aplicável;
- utilizar testes black-box e cenários documentados;
- manter registos sobre a origem da especificação;
- obter revisão legal antes de distribuição comercial ou fechada.

Não existe estratégia de provider oficial: o CraftMMO não dependerá do mcMMO em runtime.

Misturar código GPL num projeto que se pretende manter fechado sem cumprir a licença seria uma decisão juridicamente fraca.

A estratégia escolhida deverá ficar registada em `LEGAL_STRATEGY.md` antes da primeira implementação de skill.

---

# 63. Migração de mcMMO

O CraftMMO poderá importar dados existentes do mcMMO através de uma migração pontual. Esta funcionalidade não implica compatibilidade contínua entre os dois plugins.

Fluxo:

1. detetar source;
2. read-only scan;
3. validar versão;
4. mapear UUIDs;
5. mapear skills;
6. calcular child skills;
7. gerar relatório;
8. criar backup;
9. executar em transaction batches;
10. verificar counts e checksums;
11. marcar migration;
12. permitir reexecução idempotente.

Comando:

```text
/craftmmo admin migrate mcmmo --dry-run
/craftmmo admin migrate mcmmo --execute
/craftmmo admin migrate verify
```

Nunca se altera a base original durante import.

---

# 64. Versionamento

O CraftMMO terá:

- versão do plugin;
- versão do ruleset de produto;
- metadados históricos da referência utilizada para construir a 1.0.0.

Exemplo:

```text
CraftMMO: 1.0.0
Ruleset: craftmmo-1.0.0
Initial functional reference: mcMMO 2.2.053
Reference manifest revision: 1
```

A versão mcMMO é apenas metadado histórico da primeira especificação. Não é uma segunda versão de runtime que o CraftMMO tenha de acompanhar.

Uma atualização do CraftMMO poderá:

- manter o mesmo ruleset;
- alterar o ruleset com compatibilidade de dados;
- exigir migration;
- introduzir mudanças de comportamento próprias;
- abandonar comportamentos herdados da referência inicial.

Todas as mudanças funcionais deverão constar das release notes do CraftMMO.

---

# 65. Estratégia de desenvolvimento

O desenvolvimento será feito por fatias verticais.

Cada fatia inclui, conforme necessário:

- domain;
- config;
- Paper adapter;
- persistence;
- commands;
- feedback;
- anti-exploit;
- tests;
- reference scenarios;
- logging;
- failure recovery.

Não se implementam primeiro 19 listeners incompletos e só depois o motor. Cada skill entregue deverá funcionar de ponta a ponta.

---

# 66. Roadmap vertical

## 0.0 — Referência e decisões irreversíveis

- fixar mcMMO 2.2.053 como referência funcional histórica da 1.0.0;
- registar a data de congelamento da referência;
- escolher estratégia legal;
- escolher versão de Java;
- escolher versão exata de Paper/Minecraft;
- definir suporte ou ausência de suporte a Folia;
- criar reference manifest;
- criar o ruleset `craftmmo-1.0.0`;
- definir test harness;
- definir clock e RNG;
- definir package names;
- definir schemas;
- definir performance budgets.

Critério:

não começa implementação massiva sem referência reproduzível, ruleset próprio e estratégia legal aprovada.

## 0.1 — Primeiro ciclo jogável com Mining

- bootstrap;
- módulos;
- PostgreSQL;
- perfil;
- sessão;
- Mining XP;
- Mining level;
- Power Level;
- Double Drops;
- Super Breaker;
- block provenance;
- `/craftmmo`;
- admin XP;
- persistence;
- restart recovery;
- testes diferenciais de referência.

Ciclo:

entrar
→ minerar bloco natural
→ ganhar XP
→ subir Mining
→ ativar ability
→ receber drop correto
→ sair
→ reiniciar
→ manter estado.

## 0.2 — Gathering base

- Excavation;
- Herbalism;
- Woodcutting;
- provenance partilhada;
- Giga Drill Breaker;
- Green Terra;
- Tree Feller;
- treasures;
- performance budgets.

## 0.3 — Ranged e melee

- Archery;
- Axes;
- Crossbows;
- Maces;
- Spears;
- Swords;
- Tridents;
- Unarmed;
- damage source tracking;
- Limit Break;
- PvP/PvE;
- cooldown families.

## 0.4 — Crafting e child skills

- Alchemy;
- Repair;
- Salvage;
- Smelting;
- brewing ownership;
- furnace ownership;
- atomic inventory mutations;
- enchantment edge cases;
- child skill derivation.

## 0.5 — Fishing, Taming e Acrobatics

- Fishing;
- Taming;
- Acrobatics;
- complex RNG;
- loot tables;
- companions;
- fall tracking;
- AFK protections;
- statistical reference tests.

## 0.6 — Conteúdo RPG vertical

- uma criatura;
- uma elite;
- um boss;
- uma zona;
- checkpoints;
- uma quest chain;
- um custom item;
- loot pessoal;
- party básica;
- integração sem alterar o perfil oficial `CRAFTMMO_1_0`.

## 0.7 — Operação e extensibilidade

- API experimental;
- PlaceholderAPI;
- WorldGuard;
- Vault;
- Citizens;
- Redis opcional;
- multi-instance ownership;
- leaderboards;
- admin tools;
- migration mcMMO;
- reference conformance reports.

## 0.9 — Release candidate

- 19 skills completas;
- reference conformance matrix completa;
- migrations testadas;
- failure recovery;
- performance medida;
- security audit;
- operation guide;
- no critical partial features;
- upgrade path;
- documentation.

---

# 67. Escopo da versão 1.0

A versão 1.0 deverá incluir:

- um perfil por UUID;
- PostgreSQL obrigatório;
- Redis opcional;
- motor próprio de skills;
- referência funcional inicial mcMMO 2.2.053 congelada para a 1.0.0;
- as 19 skills;
- todas as sub-skills definidas para o ruleset `craftmmo-1.0.0`;
- super abilities;
- XP e levels;
- Power Level;
- child skills;
- cooldowns;
- combat interactions;
- gathering rewards;
- treasure tables;
- brewing;
- repair;
- salvage;
- smelting;
- block provenance;
- anti-exploit;
- commands essenciais;
- aliases configuráveis;
- menus essenciais;
- permissions;
- idiomas;
- admin tools;
- safe reload;
- manifest da referência e rulesets versionados;
- suite diferencial de referência;
- PostgreSQL migrations;
- import mcMMO em dry-run e execution;
- uma fatia RPG de referência:
  - criaturas;
  - uma elite;
  - um boss;
  - quests essenciais;
  - uma zona;
  - checkpoints;
  - parties básicas;
  - custom items e loot;
- API pública pequena;
- integrações principais comprovadas;
- performance report;
- upgrade guide;
- diferenças conhecidas explicitamente publicadas e justificadas.

Não são requisitos da 1.0:

- classes;
- atributos distribuíveis;
- talentos;
- mana;
- energia;
- fúria;
- subclasses;
- múltiplos personagens;
- guilds;
- auction house;
- housing;
- mounts;
- battle pass;
- seasonal progression;
- dezenas de bosses;
- dezenas de zonas;
- dungeons instanciadas;
- raids;
- cross-server parties;
- editor visual;
- compatibilidade ou coexistência em runtime com o mcMMO.

---

# 68. Pós-1.0

Depois da versão 1.0.0, o CraftMMO evolui de forma independente.

Possíveis versões futuras:

- balanceamento próprio de XP, dano, drops e cooldowns;
- redesign de skills e sub-skills existentes;
- novas skills exclusivas do CraftMMO;
- novas super abilities;
- progressão integrada com equipamento, quests, zonas, elites e bosses;
- conteúdo RPG adicional;
- guilds;
- professions adicionais;
- dungeons;
- raids;
- cross-server;
- seasonal content;
- editor visual;
- dashboards;
- analytics;
- resource pack opcional.

O projeto não assumirá acompanhamento automático das versões futuras do mcMMO.

Uma futura decisão de observar uma nova versão do mcMMO será apenas investigação de produto. Não cria obrigação de compatibilidade nem substitui a evolução própria do CraftMMO.

Nenhuma expansão entra antes de o ruleset da 1.0.0, a persistência e a recuperação de falhas estarem provados.

---

# 69. Critérios globais de conclusão

Uma milestone só está concluída quando:

- funcionalidade está jogável;
- testes unitários passam;
- testes de integração passam;
- cenários de referência aplicáveis passam;
- não bloqueia a main thread;
- restart preserva estado;
- duplicate operations são idempotentes;
- invalid config não destrói estado funcional;
- logs permitem diagnosticar falhas;
- performance fica dentro do budget;
- migrations foram testadas;
- documentação foi atualizada.

---

# 70. Definição final do CraftMMO

O CraftMMO será um framework MMO configurável e extensível para Paper, sem classes, no qual a progressão é obtida através das atividades executadas pelo jogador.

A versão 1.0.0 utilizará o mcMMO 2.2.053 como referência funcional inicial para definir, de forma reproduzível e testável, as seguintes 19 skills:

- Acrobatics;
- Alchemy;
- Archery;
- Axes;
- Crossbows;
- Excavation;
- Fishing;
- Herbalism;
- Maces;
- Mining;
- Repair;
- Salvage;
- Smelting;
- Spears;
- Swords;
- Taming;
- Tridents;
- Unarmed;
- Woodcutting.

O CraftMMO implementará estas mecânicas através de motor, dados, serviços e persistência próprios. Não dependerá do mcMMO, não utilizará um provider mcMMO e não prometerá compatibilidade ou coexistência em runtime.

O plugin manterá os restantes pilares do CraftRPG:

- custom items;
- equipamento;
- criaturas;
- elites;
- bosses;
- quests;
- parties;
- loot;
- zonas;
- checkpoints;
- menus;
- comandos;
- API;
- integrações;
- PostgreSQL;
- Redis opcional;
- ferramentas administrativas.

A filosofia será:

- progressão por uso;
- ausência de classes;
- uma referência funcional congelada para a 1.0.0;
- rulesets próprios do CraftMMO;
- evolução independente após a 1.0.0;
- PostgreSQL autoritativo;
- Redis complementar;
- operações idempotentes;
- anti-exploit desde a fundação;
- armazenamento de proveniência seguro;
- arquitetura modular;
- testes diferenciais de referência durante a construção da 1.0.0;
- fatias verticais;
- performance medida;
- reload versionado;
- falhas explícitas;
- nenhuma autoridade duplicada.

O maior risco técnico não é implementar uma skill isolada. É afirmar que a versão 1.0.0 reproduz 19 skills sem:

- fixar uma versão de referência;
- testar fórmulas e edge cases;
- reproduzir ou documentar anti-exploit;
- controlar event ordering;
- versionar loot tables;
- tratar migrations;
- definir estratégia legal.

O plano da 1.0.0 só é considerado cumprido quando os comportamentos selecionados forem demonstrados por cenários reproduzíveis e as diferenças conhecidas estiverem documentadas.

Depois da 1.0.0, a referência inicial deixa de limitar o design. O CraftMMO passa a evoluir segundo as necessidades e decisões do próprio projeto.
