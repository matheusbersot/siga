O Projeto SIGA é a versão de código aberto (GPL) do Sistema Integrado de Gestão Administrativa - SIGA, desenvolvido pela Seção Judiciária do Rio de Janeiro - SJRJ. 

Fonte: https://github.com/projeto-siga/siga e https://code.google.com/p/projeto-siga/

Utilizar o jdk 6 para compilar o projeto. Alguns erros foram encontrados ao tentar compilar o projeto com o jdk 7.

Usuários Windows: Executar lib/configure.bat para instalar dependências adicionais no repositório maven local.

Usuários Linux/Mac: Executar lib/configure.sh para instalar dependências adicionais no repositório maven local.

Compilação: Utilizar o comando "mvn clean instal". Os war necessários para rodar a aplicação vão ser criados do diretório target/.
