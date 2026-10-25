# Aplicativo Android (Kotlin) — Espaço Massoterapia & Depilação

Este pacote contém o projeto Android completo em **Kotlin + Jetpack Compose** (com **Visão do Cliente** e **Painel do Profissional** no mesmo aplicativo, sincronização 100% online em tempo real e editor visual de serviços, preços, horários e Pix diretamente na tela do celular), já configurado com **GitHub Actions** para gerar o arquivo `.apk` automaticamente na nuvem sem precisar instalar o Android Studio.

---

## Como gerar e baixar o seu arquivo `.apk` gratuitamente pelo navegador

1. **Crie uma conta e um repositório no GitHub:**
   - Acesse [github.com](https://github.com) e faça login (ou crie uma conta gratuita).
   - Clique no botão **+** (canto superior direito) → **New repository**.
   - Dê um nome (ex.: `app-massoterapia`), marque **Public** (para usar o compilador gratuito ilimitado do GitHub) e clique em **Create repository**.

2. **Envie os arquivos deste `.zip`:**
   - Descompacte (extraia) este arquivo `Projeto_Android_Massoterapia_Depilacao.zip` em uma pasta no seu computador ou celular.
   - Na página do seu repositório recém-criado no GitHub, clique no link **"uploading an existing file"**.
   - Arraste **todas as pastas e arquivos extraídos** (incluindo a pasta `.github`, a pasta `app`, `build.gradle.kts`, `settings.gradle.kts` e `gradle.properties`) para dentro da página do GitHub.
   - *(Dica: se a pasta `.github` ficar oculta no seu computador, você também pode criar o arquivo clicando em **Actions** → **set up a workflow yourself** no GitHub e colando o conteúdo de `.github/workflows/gerar-apk.yml`).*
   - Clique no botão verde **Commit changes**.

3. **Baixe o seu `.apk` pronto:**
   - Clique na aba **Actions** no topo do seu repositório no GitHub.
   - Você verá o processo **"Compilar APK Android Automaticamente"** rodando (leva cerca de 2 a 3 minutos).
   - Quando ficar com um ícone verde de concluído (✅), clique nele e role até a seção **Artifacts** no final da página.
   - Clique em **`Aplicativo-Massoterapia-Depilacao-APK`** para baixar o seu `.apk` e instalar no celular Android!

---

## O que você pode configurar dentro do próprio aplicativo (sem mexer em código)

Ao abrir o aplicativo no celular e tocar em **Profissional** (senha padrão: `1234`), você tem 3 abas administrativas:
- **Agenda:** Veja todos os pedidos dos clientes, o que cada cliente quer (procedimentos + observações), a forma de pagamento escolhida, confirme ou cancele agendamentos, chame o cliente no WhatsApp com 1 toque e bloqueie/libere horários.
- **Serviços & Horários:** Adicione ou remova procedimentos de Massoterapia e Depilação, altere preços, durações e edite sua grade de horários padrão diretamente pelo celular.
- **Nuvem & Pix:** Altere o nome do seu estúdio, seu WhatsApp, sua chave Pix, sua senha de administrador e, para sincronizar entre vários aparelhos pela internet em tempo real, cole a URL gratuita do seu **Firebase Realtime Database** (ex.: `https://seu-projeto-default-rtdb.firebaseio.com`).
