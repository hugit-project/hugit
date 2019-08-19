(defproject hugit "0.1.0"
  :min-lein-version "2.0.0"
  :dependencies [[thheller/shadow-cljs "2.8.50"]]
  :plugins [[nightlight/lein-nightlight "RELEASE"]]
  :source-paths ["src" "dev"]
  :aliases {"editor" ["nightlight" "--url" "\"http://localhost:$(cat .shadow-cljs/socket-repl.port)\""]})
