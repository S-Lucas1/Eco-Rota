# EcoRota

**EcoRota** é um aplicativo Android para gestão de rotas de coleta de lixo, voltado para uso por gestores municipais e garis. O sistema identifica a rota mais eficiente com base na localização das lixeiras e monitora o volume de lixo para otimizar a coleta.

## 🚀 Funcionalidades

- Cadastro de lixeiras e usuários
- Visualização de lixeiras em mapa com rota gerada via Google Maps
- Medição de volume de lixo por lixeira
- Geração automática de rota eficiente para coleta
- Interface segmentada para gestores e garis

## 🛠️ Tecnologias Utilizadas

- Android Studio
- Java
- Firebase (autenticação, banco de dados)
- Google Maps API (rota)
- Estrutura baseada em Activities e Services

## 🧱 Princípios SOLID Aplicados

O projeto EcoRota foi estruturado com base nos princípios **SOLID**, promovendo organização, manutenibilidade e escalabilidade do código:

### 🔹 S — Single Responsibility Principle (SRP)  
Cada classe tem uma única responsabilidade:
- `LixeiraService`, `RotaService`, `UsuarioService` lidam com as regras de negócio específicas de cada entidade.
- `Repository` como `LixeiraRepository` e `UsuarioRepository` são responsáveis apenas pelo acesso a dados.
- `Activity` como `CadastroActivity` e `LixeirasActivity` cuidam apenas da interface e interação com o usuário.

### 🔹 O — Open/Closed Principle (OCP)  
O código está aberto para extensão, mas fechado para modificação:
- A arquitetura baseada em `Services` e `Repositories` permite adicionar novos comportamentos (como filtros ou novas lógicas de rota) sem alterar diretamente o código já existente.

### 🔹 L — Liskov Substitution Principle (LSP)  
As classes e métodos foram desenhados de forma que possam ser reutilizados e estendidos sem alterar o comportamento esperado.
- As entidades `Usuario`, `Lixeira`, `Rota` seguem modelos simples e coesos que podem ser substituídos em abstrações mais genéricas, se necessário.

### 🔹 I — Interface Segregation Principle (ISP)  
Embora interfaces explícitas não estejam fortemente evidenciadas no projeto (algo comum em Java puro com Android), as responsabilidades estão bem separadas, evitando "interfaces grandes" em uma única classe.

### 🔹 D — Dependency Inversion Principle (DIP)  
As camadas de serviço dependem de abstrações e não diretamente da implementação:
- Os `Services` usam `Repositories` para acessar dados, possibilitando no futuro a injeção de dependências (via frameworks como Dagger ou manualmente) para desacoplar ainda mais.

## 🧩 Padrões de Projeto Utilizados

Além dos princípios SOLID, também foram aplicados padrões clássicos:

- **Repository Pattern**  
  Utilizado para isolar o acesso aos dados. Ex: `LixeiraRepository`, `RotaRepository`, `UsuarioRepository`.

- **Service Layer Pattern**  
  As classes `LixeiraService`, `UsuarioService`, `RotaService` atuam como uma camada intermediária entre as regras de negócio e a camada de dados.

- **Model-View-Controller (MVC)**  
  Separação clara entre:
  - `Model`: classes em `model/` (como `Lixeira`, `Rota`, `Usuario`)
  - `View`: Activities como `MainActivity`, `CadastroActivity`
  - `Controller`: Services e lógica intermediária de controle da aplicação

- **Adapter Pattern**  
  Exemplo: `LixeiraAdapter` é usado para adaptar os dados das lixeiras para visualização em listas no Android.

## 📦 Como Executar

1. Abra o projeto no Android Studio
2. Conecte um dispositivo Android ou utilize o emulador
3. Gere o APK pelo Android Studio ou execute diretamente via `Run`
