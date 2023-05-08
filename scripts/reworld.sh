#! /usr/local/bin/scsh -e main -s  

(define (main args)
   (run (pkill "Rewind"))  
  
   (move-file "~/Documents/com.memoryvault.MemoryVault.plist"  
              "~/Library/Preferences/com.memoryvault.MemoryVault.plist")
   (move-directory "~/Documents/com.memoryvault.MemoryVault"   
                    "~/Library/Application Support/com.memoryvault.MemoryVault"))
