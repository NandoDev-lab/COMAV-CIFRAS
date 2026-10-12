import os
import zipfile
import xml.etree.ElementTree as ET
import json
import time
from datetime import datetime

input_dir = r"C:\Users\FERNANDO\Desktop\Projetos Pessoais\CIFRAS COMAV"
output_json = r"C:\Users\FERNANDO\Desktop\Projetos Pessoais\CIFRAS COMAV\cifras_backup.json"

def extract_docx(file_path):
    try:
        with zipfile.ZipFile(file_path, 'r') as z:
            if 'word/document.xml' not in z.namelist():
                return ""
            xml_content = z.read('word/document.xml')
            root = ET.fromstring(xml_content)

            # Namespace for wordprocessingml
            ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}

            paragraphs = []
            for p in root.iter('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}p'):
                texts = []
                for t in p.iter('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}t'):
                    if t.text:
                        texts.append(t.text)
                paragraphs.append("".join(texts))
            return "\n".join(paragraphs).strip()
    except Exception as e:
        print(f"Erro ao ler DOCX {file_path}: {e}")
        return ""

def extract_pdf(file_path):
    # Tenta usar pypdf se disponível, senão tenta extração básica ou retorna aviso
    try:
        import pypdf
        reader = pypdf.PdfReader(file_path)
        text = ""
        for page in reader.pages:
            t = page.extract_text()
            if t:
                text += t + "\n"
        return text.strip()
    except ImportError:
        try:
            import pypdf as pdf_lib
            # fallback antigo PyPDF2
            reader = pdf_lib.PdfFileReader(file_path)
            text = ""
            for i in range(reader.numPages):
                text += reader.getPage(i).extractText() + "\n"
            return text.strip()
        except Exception:
            return "[Conteúdo em PDF - Instale pypdf para extração automática ou converta para docx]"
    except Exception as e:
        print(f"Erro ao ler PDF {file_path}: {e}")
        return ""

musicas_list = []
now_ms = int(time.time() * 1000)

print(Var := f"Varrendo diretório: {input_dir}")

id_counter = 1
for root, dirs, files in os.walk(input_dir):
    for file in files:
        if file.startswith("~$") or file.endswith(".json") or file.endswith(".jpg") or file.endswith(".png"):
            continue

        file_path = os.path.join(root, file)
        ext = file.lower()

        conteudo = ""
        tipo_origem = "MANUAL"

        if ext.endswith(".docx"):
            conteudo = extract_docx(file_path)
            tipo_origem = "DOCX"
        elif ext.endswith(".pdf"):
            conteudo = extract_pdf(file_path)
            tipo_origem = "PDF"
        else:
            continue

        if not conteudo:
            continue

        # Título sugerido remove extensão
        titulo = os.path.splitext(file)[0]
        # Limpa sufixos comuns como "-capa" ou similar se houver

        musica = {
            "identificador": id_counter,
            "titulo": titulo,
            "conteudo": conteudo,
            "conteudoOriginal": conteudo,
            "tipoDeConteudo": "CIFRA",
            "tipoDeOrigem": tipo_origem,
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

print(f"Backup gerado com sucesso! Total de músicas: {len(musicas_list)}")
print(f"Arquivo salvo em: {output_json}")
