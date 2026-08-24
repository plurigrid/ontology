# Ontology history walk

This project adds live Clojure/Specter views of the Git history still exposed by GitHub for both [`plurigrid/ontology`](https://github.com/plurigrid/ontology) and every repository cataloged under the `plurigrid` organization.

It uses `gh api graphql` to collect:

- all 237 commits reachable from the default branch, with every parent edge;
- every current branch and tag, including annotated tags;
- all 57 pull requests and the commits retained by open, closed, and merged PRs;
- the transitive parent closure of those roots (253 unique commits at the 2026-08-24 refresh).

The result is cached as checksummed EDN, prepared as a bidirectional commit DAG, queried with real [`com.rpl/specter`](https://github.com/redplanetlabs/specter), and walked with deterministic SplitMix64 choices. A walk step includes the commit, topology degree, GF(3) trit, and color. Every completed walk conserves its trits: `Σ trits ≡ 0 (mod 3)`.

The organization catalog is independently paginated and normalized by `nameWithOwner`. It records repository metadata and default-branch heads, while an organization walk fetches only the current commit at each step. Commit identities are `[repository oid]`, so forks that share Git object IDs remain distinct.

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

# Catalog every repository in the organization, then inspect or walk it.
clojure -M:run org-refresh --org plurigrid
clojure -M:run org-summary
clojure -M:run org-select
clojure -M:run org-walk --repo plurigrid/ontology --seed 69 --steps 12
clojure -M:run org-walk --repo plurigrid/ontology --start-oid HEAD_OID --steps 12
clojure -M:run org-walk --seed 0x59b826e497513d1a --restart --steps 40

# Tests and static analysis.
bb test
bb lint
bb check
bb org-live-check
```

`--start head` (the default) starts a repository walk at the default branch head. `--start latest` starts at the newest commit among all retained branches and PRs. A branch/tag name or unique commit prefix also works. `past` follows parents, `future` follows children, and `both` explores either direction.

Both walk modes stop at a dead end unless `--restart` is supplied. A repository walk then teleports to an unvisited commit component; an organization walk teleports to an unvisited repository head. `--start-oid` starts an organization walk at a specific commit in the selected `--repo`. Empty repositories and repositories without a usable default-branch head remain in the catalog but cannot be selected for a walk.

Organization refreshes fail closed when the reported repository total or organization identity changes between pages, or when an overlapping repository row changes during pagination. Commit parent lists must fit in the requested GraphQL page. Supplying `--org` or `--repo` with an existing cache also verifies that the cached identity matches; use `--refresh` to replace a mismatched cache.

The repository cache is `.cache/ontology-history.edn`; the organization cache is `.cache/plurigrid-org.edn`. Both are ignored by Git and may be overridden with `--cache`.

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

(def org (h/fetch-organization {:organization "plurigrid"}))
(h/organization-summary org)
(h/organization-repositories org [sp/ALL #(not (:isFork %)) :nameWithOwner])
(h/organization-random-walk org {:repository "plurigrid/ontology"
                                 :start-oid "HEAD_OID"
                                 :seed 69
                                 :steps 12})
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
M-x ontology-history-organization-refresh
M-x ontology-history-organization-walk
M-x ontology-history-organization-specter-scratch
```

`ontology-history-jack-in` opens `src/plurigrid/ontology/history.clj` and starts CIDER with the project-local `:cider` alias. The scratch commands insert ready-to-edit repository or organization Specter queries into that REPL. Customize `ontology-history-organization` and `ontology-history-organization-cache` when targeting another organization.

Attribution: `monaduck1069`.
