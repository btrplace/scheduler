;;
;; Copyright (c) Fabien Hermenier
;;
;;        This file is part of Entropy.
;;
;;        Entropy is free software: you can redistribute it and/or modify
;;        it under the terms of the GNU Lesser General Public License as published by
;;        the Free Software Foundation, either version 3 of the License, or
;;        (at your option) any later version.
;;
;;        Entropy is distributed in the hope that it will be useful,
;;        but WITHOUT ANY WARRANTY; without even the implied warranty of
;;        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;        GNU Lesser General Public License for more details.
;;
;;       You should have received a copy of the GNU Lesser General Public License
;;       along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
;;

(defvar btrplace-mode-hook nil)

(defvar btrplace-mode-map
  (let ((btrplace-mode-map (make-keymap)))
    (define-key btrplace-mode-map "\C-j" 'newline-and-indent)
    btrplace-mode-map)
  "Keymap for Btrplace major mode")

(add-to-list 'auto-mode-alist '("\\.btrp\\'" . btrplace-mode))

(defconst btrplace-font-lock-keywords-1
  (list
   '("\\<\\(namespace\\|i\\(f\\|n\\|mport\\)\\|t\\(o\\|h\\en\\)\\|for\\|e\\(lse\\|xport\\)\\)\\>" . font-lock-keyword-face) ;; reserved words
   '("\\(\\$\\w+\\)" . font-lock-variable-name-face) ;; standard definition of variables
   '("\\($\\w+\\)\s+in" 1 font-lock-variable-name-face) ;; definition of an iterator   
   '(":\\s *\\(\\w+\\)" . font-lock-builtin-face) ;; Template
  '("\\(\\w+\\)\\s *(" 1 font-lock-function-name-face)) ;; constraint
  "Minimal highlighting expressions for Btrplace mode")


(defvar btrplace-font-lock-keywords btrplace-font-lock-keywords-1
  "Default highlighting expressions for Btrplace mode.")

(defvar btrplace-mode-syntax-table
  (let ((btrplace-mode-syntax-table (make-syntax-table)))
    
    ;; _ is a word
    (modify-syntax-entry ?_ "w" btrplace-mode-syntax-table)

    ;; Multiline comments
    (modify-syntax-entry ?/ ". 124b" btrplace-mode-syntax-table)
    (modify-syntax-entry ?* ". 23" btrplace-mode-syntax-table)
    (modify-syntax-entry ?\n "> b" btrplace-mode-syntax-table)

    ;; Operators (punctuation)
    (modify-syntax-entry ?+  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?-  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?%  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?&  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?|  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?^  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?!  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?=  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?<  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?>  "." btrplace-mode-syntax-table)
    (modify-syntax-entry ?,  "." btrplace-mode-syntax-table)

    btrplace-mode-syntax-table)
  "Syntax table for wpdl-mode")

(defun btrplace-mode ()
  "Major mode for editing Btrplace files"
  (interactive)
  (kill-all-local-variables)
  (set (make-local-variable 'font-lock-defaults) '(btrplace-font-lock-keywords))
  (set-syntax-table btrplace-mode-syntax-table)
  (setq major-mode 'btrplace-mode)
  (setq mode-name "Btrplace")
  (run-hooks 'btrplace-mode-hook)
  )

(provide 'btrplace-mode)