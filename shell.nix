{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  buildInputs = with pkgs; [
    # texlive.combined.scheme-full
    # idris2
    # agda
    # ghc
    # cargo
    just
    python3
    poetry
    tree
    sl
  ];
}
