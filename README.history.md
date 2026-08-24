# Ontology history walk

This project adds a live Clojure/Specter view of the Git history still exposed by GitHub for [`plurigrid/ontology`](https://github.com/plurigrid/ontology).

It uses `gh api graphql` to collect:

- all 237 commits reachable from the default branch, with every parent edge;
- every current branch and tag, including annotated tags;
- all 57 pull requests and the commits retained by open, closed, and merged PRs;
- the transitive parent closure of those roots (253 unique commits at the 2026-08-24 refresh).

The result is cached as checksummed EDN, prepared as a bidirectional commit DAG, queried with real [`com.rpl/specter`](https://github.com/redplanetlabs/specter), and walked with deterministic SplitMix64 choices. A walk step includes the commit, topology degree, GF(3) trit, and color.

## CLI

```bash
# Authenticate first; the implementation never reads or stores the token.
gh auth status

# Fetch every GraphQL page and retained history root.
clojure -M:run refresh

# Reuse the checksummed cache.
clojure -M:run summary
clojure -M:run walk --seed 0x59b826e497513d1a --steps 24 --direction past
clojure -M:run walk --start latest --direction both --restart --steps 40

# Print every headline selected through Specter.
clojure -M:run select

# Tests and static analysis.
clojure -M:test
joker --lint src/plurigrid/ontology/history.clj
```

`--start head` (the default) starts at the default branch head. `--start latest` starts at the newest commit among all retained branches and PRs. A branch/tag name or unique commit prefix also works. `past` follows parents, `future` follows children, and `both` explores either direction. A walk stops at a dead end unless `--restart` is supplied, in which case it teleports to an unvisited component.

The cache is `.cache/ontology-history.edn` by default and is ignored by Git.

## Clojure REPL and Specter

```clojure
(require '[plurigrid.ontology.history :as h]
         '[com.rpl.specter :as sp])

(def history (h/prepare-history (h/read-cache ".cache/ontology-history.edn")))

(h/summary history)
(h/random-walk history {:seed 21211 :steps 12 :direction :both})
(h/specter-select [:commits sp/ALL :messageHeadline] history)
(h/merge-commits history)
(h/commits-touching history "arena")
```

The public data functions take an optional `:gh-runner`, making GraphQL pagination and failures testable without the network.

## Emacs/CIDER

The checked-in [`ontology-history.el`](ontology-history.el) is a small project adapter, not a global Emacs configuration.

```elisp
M-x load-file RET /path/to/ontology/ontology-history.el
M-x ontology-history-jack-in
M-x ontology-history-refresh
M-x ontology-history-walk
M-x ontology-history-specter-scratch
```

`ontology-history-jack-in` opens `src/plurigrid/ontology/history.clj` and starts CIDER with the project-local `:cider` alias. The scratch command inserts a ready-to-edit Specter query into that REPL.

Attribution: `monaduck1069`.
