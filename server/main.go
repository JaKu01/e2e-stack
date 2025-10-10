package main

import (
	"encoding/json"
	"io"
	"log"
	"net/http"
)

type StatusCodeLogger struct {
	http.ResponseWriter
	status int
}

func (l *StatusCodeLogger) WriteHeader(status int) {
	l.status = status
	l.ResponseWriter.WriteHeader(status)
}

func logger(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

		statusCodeLogger := StatusCodeLogger{w, 200}
		next.ServeHTTP(&statusCodeLogger, r)
		log.Printf("%s %s finished with code %d %s", r.Method, r.URL, statusCodeLogger.status, http.StatusText(statusCodeLogger.status))
	})
}

func main() {

	keys := make(map[string]string)
	messages := make(map[string]string)

	mux := http.NewServeMux()

	mux.HandleFunc("GET /api/keys", func(w http.ResponseWriter, r *http.Request) {

		name := r.URL.Query().Get("name")
		if name == "" {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "name is empty"})
			return
		}

		keyForName, ok := keys[name]
		if !ok {
			w.WriteHeader(http.StatusNotFound)
			json.NewEncoder(w).Encode(map[string]string{"error": "name not found"})
			return
		}

		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(keyForName))
	})

	mux.HandleFunc("POST /api/keys", func(w http.ResponseWriter, r *http.Request) {
		name := r.URL.Query().Get("name")

		if name == "" {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "name is empty"})
			return
		}

		preKeyBundle, err := io.ReadAll(r.Body)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "body is empty"})
			return
		}

		keys[name] = string(preKeyBundle)
		w.WriteHeader(http.StatusCreated)
		log.Printf("Added pre key bundle %s with name %s", preKeyBundle, name)
	})

	mux.HandleFunc("GET /api/messages", func(w http.ResponseWriter, r *http.Request) {
		name := r.URL.Query().Get("name")
		if name == "" {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "name is empty"})
			return
		}

		encryptedMessage, ok := messages[name]
		if !ok {
			w.WriteHeader(http.StatusNotFound)
			json.NewEncoder(w).Encode(map[string]string{"error": "name not found"})
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.Write([]byte(encryptedMessage))
	})

	mux.HandleFunc("POST /api/messages", func(w http.ResponseWriter, r *http.Request) {
		name := r.URL.Query().Get("receiver")

		if name == "" {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "receiver is empty"})
			return
		}

		encryptedMessage, err := io.ReadAll(r.Body)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "body is empty"})
			return
		}

		messages[name] = string(encryptedMessage)
		w.WriteHeader(http.StatusCreated)
		log.Printf("Added encryptedMessage json %s for name %s", encryptedMessage, name)
	})

	log.Fatal(http.ListenAndServe(":8080", logger(mux)))
}
