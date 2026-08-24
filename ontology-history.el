;;; ontology-history.el --- CIDER entry point for ontology history -*- lexical-binding: t; -*-

;; Attribution: monaduck1069

(require 'cl-lib)
(require 'subr-x)
(require 'cider)
(require 'cider-client)
(require 'cider-connection)
(require 'cider-jack-in)
(require 'nrepl-dict)

(defgroup ontology-history nil
  "Explore repository and organization Git history in CIDER."
  :group 'clojure)

(defcustom ontology-history-directory
  (file-name-directory (or load-file-name buffer-file-name))
  "Project directory containing deps.edn."
  :type 'directory)

(defcustom ontology-history-cache ".cache/ontology-history.edn"
  "Cache path relative to `ontology-history-directory`."
  :type 'string)

(defcustom ontology-history-organization "plurigrid"
  "GitHub organization queried by organization history commands."
  :type 'string)

(defcustom ontology-history-organization-cache ".cache/plurigrid-org.edn"
  "Organization cache path relative to `ontology-history-directory`."
  :type 'string)

(defun ontology-history--clojure-file ()
  (expand-file-name "src/plurigrid/ontology/history.clj"
                    ontology-history-directory))

(defun ontology-history-open ()
  "Open the Clojure history namespace and load this Emacs helper."
  (interactive)
  (find-file (ontology-history--clojure-file)))

(defun ontology-history-jack-in ()
  "Open the project and start its CIDER JVM REPL.

The project-local :cider alias provides nREPL and matching CIDER middleware."
  (interactive)
  (let ((default-directory (file-name-as-directory
                            (expand-file-name ontology-history-directory))))
    (find-file (ontology-history--clojure-file))
    (cider-jack-in-clj
     (list :project-dir default-directory
           :jack-in-cmd "clojure -M:cider"))))

(defun ontology-history--eval (form callback)
  (unless (cider-connected-p)
    (user-error "No CIDER connection; run M-x ontology-history-jack-in"))
  (cider-nrepl-send-eval-request
   form callback :ns "plurigrid.ontology.history"))

(defun ontology-history--display-output (buffer-name response)
  (when-let ((out (nrepl-dict-get response "out")))
    (with-current-buffer (get-buffer-create buffer-name)
      (let ((inhibit-read-only t))
        (erase-buffer)
        (insert out)
        (special-mode))
      (display-buffer (current-buffer)))))

(defun ontology-history-refresh ()
  "Refresh all default-branch history through gh GraphQL from CIDER."
  (interactive)
  (let ((cache (expand-file-name ontology-history-cache
                                 ontology-history-directory)))
    (ontology-history--eval
     (format "(summary (refresh! {:cache-file %S}))" cache)
     (lambda (response)
       (when-let ((value (nrepl-dict-get response "value")))
         (message "ontology history: %s" value))))))

(defun ontology-history-walk (seed steps direction)
  "Evaluate and display a deterministic history walk in the CIDER REPL."
  (interactive
   (list (read-string "SplitMix64 seed: " "21211")
         (read-number "Transitions: " 24)
         (intern (completing-read "Direction: " '("past" "future" "both")
                                  nil t nil nil "past"))))
  (let ((cache (expand-file-name ontology-history-cache
                                 ontology-history-directory)))
    (ontology-history--eval
     (format
      (concat "(let [h (read-cache %S)] "
              "(print (format-walk (random-walk h {:seed %S :steps %d :direction :%s}))))")
      cache seed steps (symbol-name direction))
     (lambda (response)
       (ontology-history--display-output "*ontology-history-walk*" response)))))

(defun ontology-history-organization-refresh ()
  "Refresh the organization repository catalog through CIDER."
  (interactive)
  (let ((cache (expand-file-name ontology-history-organization-cache
                                 ontology-history-directory)))
    (ontology-history--eval
     (format
      "(organization-summary (refresh-organization! {:organization %S :cache-file %S}))"
      ontology-history-organization cache)
     (lambda (response)
       (when-let ((value (nrepl-dict-get response "value")))
         (message "ontology organization: %s" value))))))

(defun ontology-history-organization-walk
    (repository start-oid seed steps restart)
  "Evaluate an organization walk from REPOSITORY and optional START-OID."
  (interactive
   (list (read-string "Repository (blank for seeded selection): ")
         (read-string "Start commit OID (blank for repository head): ")
         (read-string "SplitMix64 seed: " "21211")
         (read-number "Transitions: " 24)
         (y-or-n-p "Restart in another repository at roots? ")))
  (let ((cache (expand-file-name ontology-history-organization-cache
                                 ontology-history-directory)))
    (ontology-history--eval
     (format
      (concat "(let [catalog (read-cache %S)] "
              "(print (format-organization-walk "
              "(organization-random-walk catalog "
              "{:seed %S :steps %d :restart? %s%s%s}))))")
      cache seed steps (if restart "true" "false")
      (if (string-empty-p repository)
          ""
        (format " :repository %S" repository))
      (if (string-empty-p start-oid)
          ""
        (format " :start-oid %S" start-oid)))
     (lambda (response)
       (ontology-history--display-output "*ontology-organization-walk*" response)))))

(defun ontology-history-specter-scratch ()
  "Insert a prepared Specter query scratchpad into the current CIDER REPL."
  (interactive)
  (let ((repl (cider-current-repl)))
    (unless repl
      (user-error "No CIDER connection; run M-x ontology-history-jack-in"))
    (pop-to-buffer repl))
  (goto-char (point-max))
  (insert
   (format
    (concat "\n(require '[com.rpl.specter :as sp])\n"
            "(def h (prepare-history (read-cache %S)))\n"
            "(specter-select [:commits sp/ALL :messageHeadline] h)\n")
    (expand-file-name ontology-history-cache ontology-history-directory))))

(defun ontology-history-organization-specter-scratch ()
  "Insert an organization catalog Specter query into the current CIDER REPL."
  (interactive)
  (let ((repl (cider-current-repl)))
    (unless repl
      (user-error "No CIDER connection; run M-x ontology-history-jack-in"))
    (pop-to-buffer repl))
  (goto-char (point-max))
  (insert
   (format
    (concat "\n(require '[com.rpl.specter :as sp])\n"
            "(def org (read-cache %S))\n"
            "(organization-repositories org [sp/ALL #(not (:isFork %%)) :nameWithOwner])\n")
    (expand-file-name ontology-history-organization-cache
                      ontology-history-directory))))

(provide 'ontology-history)
;;; ontology-history.el ends here
