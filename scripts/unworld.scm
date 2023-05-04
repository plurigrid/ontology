#!/usr/local/bin/scsh \
-e main -s
!#

(define (main args)
  ; Kill the "Rewind" process
  (run (pkill Rewind))

  ; Copy the plist file
  (let ((home (getenv "HOME")))
    (run (cp ,(string-append home "/Library/Preferences/com.memoryvault.MemoryVault.plist")
             ,(string-append home "/Documents/com.memoryvault.MemoryVault.plist"))))

  ; Copy the "MemoryVault" directory
  (let ((home (getenv "HOME")))
    (run (cp -R ,(string-append home "/Library/Application Support/com.memoryvault.MemoryVault")
               ,(string-append home "/Documents/com.memoryvault.MemoryVault")))))
