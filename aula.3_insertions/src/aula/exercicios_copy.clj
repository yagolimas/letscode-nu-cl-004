(ns aula.exercicios-copy
  (:require [datomic.client.api :as d]))

(def genre [:pop :rock :mpb :bossa :reaggue :rap :sertanejo])

;; 1 - criar schema de album músical que contenha id (deve ser único), nome, artista
;; 2 - adicionar um (ou mais de um) album(s) ao nosso banco de dados
;; 3 - modificar nosso schema, adicionando o campo gênero (deve ser um "enum")
;; 4 - adicione gênero nos albuns já existentes
;; 5 - faça um retract, removendo um album ou um atributo
;; 6 - faça com que nome do álbum e artista sejam atributos obrigatórios (bônus)
;; 7 - faça uma query que liste todos os albuns (bônus 2)

(def album-schema [{:db/ident       :album/id
                    :db/unique      :db.unique/identity
                    :db/valueType   :db.type/long
                    :db/cardinality :db.cardinality/one}
                   {:db/ident       :album/name
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/one}
                   {:db/ident       :album/artist
                    :db/valueType   :db.type/string
                    :db/cardinality :db.cardinality/many}])

(comment
  (def client (d/client {:server-type :dev-local
                         :system      "dev"}))
  (d/create-database client {:db-name "albums-db"})
  (def conn (d/connect client {:db-name "albums-db"}))

  (d/transact conn {:tx-data album-schema})

  (d/transact conn {:tx-data [{:album/id     1
                               :album/name   "Thriller"
                               :album/artist "Michael Jackson"}
                              {:album/id     2
                               :album/name   "The Beatles"
                               :album/artist "Paul Maccartney"}]})

  (d/db conn)

  (d/transact conn {:tx-data [{:album/id     3
                               :album/name   "Elvis"
                               :album/artist "Elvis Presley"}]})

  (d/q '[:find (pull ?album [*])
         :where [?album :album/name]] (d/db conn))

  (d/q '[:find ?id ?name ?artist
         :where
         [?album :album/id ?id]
         [?album :album/name ?name]
         [?album :album/artist ?artist]] (d/db conn))

  (d/delete-database client {:db-name "albums-db"})
  )
