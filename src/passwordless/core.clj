(ns passwordless.core
  (:require [twilio.core :as twilio]
            [taoensso.carmine :as car :refer (wcar)]))

;; test
(send-auth-token redis-opts 500 sms-deliverer "+12132201494")

(send-auth-token storage uid deliverer address) ;; uid is stored and sent to the address
(validate-auth-token redis-opts token) ;; returns uid or nil or error

(validate-auth-token redis-opts "SE7fWYE4nLEc")

(def token-storage (create-redis-storage {:pool {}
                                          :spec {:host "127.0.0.1" :port 6379}
                                          :prefix "TKN"}))

(def redis-opts {:pool {}
                 :spec {:host "127.0.0.1" :port 6379}
                 :prefix "TKN"})


(defn create-twilio-deliverer [{twilio-sid :twilio-sid
                                twilio-auth-token :twilio-auth-token
                                sender :sender
                                template :template}]
  (fn [auth-token address]
    (twilio/with-auth twilio-sid twilio-auth-token
      (twilio/send-sms
        {:From sender
         :To address
         :Body (format template auth-token)}))
    ))

(defn create-simple-smtp-deliverer [options]
  (fn [auth-token address]
    (let [body-template (:template options)
          body (:format body-template auth-token)]
    (postal.core.send-message
     options
     {:from (:from options)
      :to address
      :subject (:subject- options)
      :body body}))))

(defn send-auth-token [redis-opts uid deliverer address]
  (let [auth-token (create-auth-token)]
    (if (store-auth-token redis-opts auth-token uid)
      (deliverer auth-token address)
      (println "ggg")
    )))


(defn validate-auth-token [{prefix :prefix :as redis-opts} auth-token]
  (let [prefixed-token (append-prefix [prefix auth-token])]
    (car/wcar redis-opts (car/get prefixed-token))))

(defn store-auth-token [{prefix :prefix :as redis-opts} auth-token uid]
  (let [prefixed-token (append-prefix prefix auth-token)]
    (car/wcar redis-opts (car/set prefixed-token uid))))

(defn create-redis-storage [{prefix :prefix :as redis-opts}]
  (fn [auth-token uid]
    (let [prefixed-token (append-prefix [prefix auth-token])]
         (car/wcar conn-opts (car/set prefixed-token uid)))))

(defn append-prefix [args] (clojure.string/join "/" args))

(def url-safe-chars
  (let [chars-between #(map char (range (int %1) (int %2)))]
    (concat (chars-between \0 \9)
            (chars-between \a \z)
            (chars-between \A \Z))))

(defn fixed-length-password [n]
  (let [password (take n (repeatedly #(rand-nth url-safe-chars)))]
    (reduce str password)))

(defn create-auth-token [] (fixed-length-password 12))
