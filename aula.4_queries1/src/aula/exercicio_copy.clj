(ns aula.exercicio-copy
  (:require [datomic.client.api :as d]))

;; 1 - adicionar schema para termos artista como uma entidade separada com id e nome
;; 2 - criar query para listar albuns e seus artistas (uma query para cada)
;; 3 - criar query para listar albuns e seus artistas (album e o artista em uma só estrutura de mapa)
;; 4 - criar query para listar um album por nome
;; 5 - listar albuns por artista. (bônus)

;; referência -- https://docs.datomic.com/cloud/query/query-executing.html

(def album [{:db/ident :artista/id
             :db/cardinality :db.cardinality/one
             :db/unique :db.unique/identity
             :db/valueType :db.type/long}
            {:db/ident :artista/nome
             :db/cardinality :db.cardinality/one
             :db/valueType :db.type/string}

            {:db/ident :album/id
             :db/cardinality :db.cardinality/one
             :db/unique :db.unique/identity
             :db/valueType :db.type/long}
            {:db/ident :album/nome
             :db/cardinality :db.cardinality/one
             :db/valueType :db.type/string}
            {:db/ident :album/artista
             :db/cardinality :db.cardinality/one
             :db/valueType :db.type/ref}])

(def artista-samples
  (mapv (fn [i]
          {:artista/id i
           :artista/nome (str "artista " i)})
        (range 1 31)))

(comment
  (def client (d/client {:server-type :dev-local
                         :system "dev"}))

  (d/create-database client {:db-name "albuns"})

  (def conn (d/connect client {:db-name "albuns"}))

  (d/transact conn {:tx-data album})

  (d/transact conn {:tx-data artista-samples})

  (d/transact conn {:tx-data [{:album/id 1
                               :album/nome "American Idiot"
                               :album/artista [:artista/id 1]}
                              {:album/id 2
                               :album/nome "Amar Elo"
                               :album/artista [:artista/id 11]}
                              {:album/id 3
                               :album/nome "Multitude"
                               :album/artista [:artista/id 22]}]})

  ;; 2 - criar query para listar albuns e seus artistas (uma query para cada)

  ;; listando os albuns
  (d/q '[:find (pull ?album [*])
         :where [?album :album/nome ?nome]]
       (d/db conn))

  ;; listando os artistas
  (d/q '[:find (pull ?artista [*])
         :where [?artista :artista/nome ?nome]]
       (d/db conn))

  ;; 3 - criar query para listar albuns e seus artistas (album e o artista em uma só estrutura de mapa)
  (def album-pattern
    [:album/id
     :album/nome
     {:album/artista
      [:artista/id
       :artista/nome]}])
  (def american-idiot (ffirst
                        (d/q '[:find ?album
                               :where [?album :album/nome "Multitude"]]
                             (d/db conn))))
  (d/pull (d/db conn) album-pattern american-idiot)

  (d/q '[:find (pull ?album [*])
         :where [?album :album/nome ?nome]]
       (d/db conn) album-pattern)

  ;; 4 - criar query para listar um album por nome
  (d/q '[:find ?album
         :in $ ?nome
         :where [?album :album/nome ?nome]]
       (d/db conn) "American Idiot")

  ;; 5 - listar albuns por artista. (bônus)
  (d/q '[:find ?nome
         :in $ ?artista
         :where [?album :album/nome ?nome]
         [?album :album/artista ?artista]]
       (d/db conn) 92358976733272)

  (d/delete-database client {:db-name "albuns"})
  )