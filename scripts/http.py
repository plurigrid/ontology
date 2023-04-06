import http.server
import socketserver
from pathlib import Path
import re

def extract_sections(arena_md):
    sections = re.findall(r'- \[(?:x| )\] (.*)', arena_md)
    sections_done = sections[:len(sections)//2]
    sections_next = sections[len(sections)//2:]

    return {
        'done': sections_done,
        'next': sections_next
    }

def arena_content():
    arena_path = Path("arenas/007.md")
    with open("arena.html", "r") as f:
        html_template = f.read()

    with open(arena_path, "r") as f:
        arena_md = f.read()

    sections = extract_sections(arena_md)

    done = ''.join([f'<li>{task.strip()}</li>' for task in sections['done']])
    next_tasks = ''.join([f'<li>{task.strip()}</li>' for task in sections['next']])

    return html_template.replace("<!-- INSERT_DONE -->", done)\
                        .replace("<!-- INSERT_NEXT -->", next_tasks)

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/" or self.path == "/arena.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html")
            self.end_headers()
            self.wfile.write(arena_content().encode())
        else:
            super().do_GET()

PORT = 8000

Handler = CustomHTTPRequestHandler
httpd = socketserver.TCPServer(("", PORT), Handler)

print(f"Serving on port {PORT}")
httpd.serve_forever()
