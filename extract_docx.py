import zipfile
import xml.etree.ElementTree as ET
import os

def extract_docx_text(docx_path):
    # Namespace dictionary to find tags
    namespaces = {
        'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
    }
    
    try:
        with zipfile.ZipFile(docx_path) as docx:
            # The XML containing the document content
            xml_content = docx.read('word/document.xml')
            root = ET.fromstring(xml_content)
            
            paragraphs = []
            # Find all paragraph elements
            for para in root.iter('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}p'):
                # For each paragraph, find all text elements
                text_elems = para.iter('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}t')
                text = ''.join([elem.text for elem in text_elems if elem.text])
                if text.strip():
                    paragraphs.append(text)
            
            return '\n'.join(paragraphs)
    except Exception as e:
        return f"Error: {e}"

if __name__ == '__main__':
    docx_path = 'Mau bao caot.docx'
    if os.path.exists(docx_path):
        text = extract_docx_text(docx_path)
        with open('Mau_bao_caot.txt', 'w', encoding='utf-8') as f:
            f.write(text)
        print(f"Extracted successfully. Length: {len(text)} characters. Saved to Mau_bao_caot.txt.")
    else:
        print(f"File {docx_path} not found.")
