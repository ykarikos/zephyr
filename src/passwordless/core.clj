(ns passwordless.core
  (:require [twilio.core :as twilio]
            [taoensso.carmine :as car :refer (wcar)]
            [postal.core :as postal :refer (send-message)]))

(defn- store-auth-token [{prefix :prefix :as redis-opts} auth-token uid]
  (let [prefixed-token (clojure.string/join "/" [prefix auth-token])]
    (car/wcar redis-opts (car/set prefixed-token uid))))

(defn send-auth-token [redis-opts uid deliverer address]
  (let [url-safe-chars (let [chars-between #(map char (range (int %1) (int %2)))]
                            (concat (chars-between \0 \9)
                                    (chars-between \a \z)
                                    (chars-between \A \Z)))
        token-length 12
        auth-token (reduce str (take token-length (repeatedly #(rand-nth url-safe-chars))))]
    (if (store-auth-token redis-opts auth-token uid)
      (deliverer auth-token address)
      (println "error") ;TODO handle error
    )))

(defn validate-auth-token [{prefix :prefix :as redis-opts} auth-token]
  (let [prefixed-token (clojure.string/join "/" [prefix auth-token])]
    (car/wcar redis-opts (car/get prefixed-token))))

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

(defn create-simple-smtp-deliverer [conn-opts msg-opts]
  (fn [auth-token address]
    (let [template (:template msg-opts)
          body (format template auth-token)]
    (postal/send-message
     conn-opts
     {:from (:from msg-opts)
      :to address
      :subject (:subject msg-opts)
      :body body}))))
