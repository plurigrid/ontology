play:
    @echo gm! ☀️
obsidian:
    @echo playback v0.1 alpha
    cp -R .playback/.obsidian-template .obsidian
    @echo "Opening Obsidian..."
    open -a Obsidian
arena:
    nix-shell --run "poetry shell" --run "python scripts/http.py" & sleep 2 && open "http://localhost:8000/"
