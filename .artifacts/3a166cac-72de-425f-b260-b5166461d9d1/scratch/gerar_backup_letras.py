import os
import json
import time
from datetime import datetime

input_dir = r"C:\Users\FERNANDO\Desktop\Projetos Pessoais\CIFRAS COMAV"
output_json = r"C:\Users\FERNANDO\Desktop\Projetos Pessoais\CIFRAS COMAV\letras_backup.json"

musicas_list = []
now_ms = int(time.time() * 1000)

print(f"Varrendo diretório em busca de arquivos de letra (* - letra.txt): {input_dir}")

id_counter = 1
for root, dirs, files in os.walk(input_dir):
    for file in files:
        if file.endswith("- letra.txt") or file.endswith("-Letra.txt") or file.endswith("- LETRA.txt") or "letra" in file.lower() and file.endswith(".txt"):
            file_path = os.path.join(root, file)

            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    conteudo = f.read().strip()
            except UnicodeDecodeError:
                try:
                    with open(file_path, 'r', encoding='latin-1') as f:
                        conteudo = f.read().strip()
                except Exception as e:
                    print(f"Erro ao ler TXT {file_path}: {e}")
                    continue
            except Exception as e:
                print(f"Erro ao ler TXT {file_path}: {e}")
                continue

            if not conteudo:
                continue

            # Limpa o título removendo sufixos de letra e extensão
            titulo = file
            for suffix in [" - letra.txt", " -Letra.txt", " - LETRA.txt", "- letra.txt", ".txt"]:
                if titulo.endswith(suffix):
                    titulo = titulo[:-len(suffix)]

            titulo = titulo.strip()

            musica = {
                "identificador": id_counter,
                "titulo": titulo,
                "conteudo": conteudo,
                "conteudoOriginal": conteudo,
                "tipoDeConteudo": "LETRA",
                "tipoDeOrigem": "MANUAL",
                "nomeDoArquivoDeOrigem": file,
                "criadaEm": now_ms,
                "atualizadaEm": now_ms
            }

            musicas_list.append(musica)
            id_counter += 1

backup_data = {
    "formato": "COMAV_BACKUP",
    "versao": 1,
    "exportadoEm": datetime.now().strftime('%Y-%m-%dT%H:%M:%S.000Z'),
    "musicas": musicas_list
}

with open(output_json, 'w', encoding='utf-8') as f:
    json.dump(backup_data, f, ensure_ascii=False, indent=2)

print(f"Backup de letras gerado com sucesso! Total de letras: {len(musicas_list)}")
print(f"Arquivo salvo em: {output_json}")
