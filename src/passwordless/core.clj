(ns passwordless.core
  (:require [twilio.core :as twilio]
            [taoensso.carmine :as car :refer (wcar)]))

;; test
(send-auth-token token-storage 500 sms-deliverer "+12132201494")

(send-auth-token storage uid deliverer address) ;; uid is stored and sent to the address
(validate-auth-token storage token) ;; returns uid or nil or error

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

(defn send-auth-token [storage uid deliverer address]
  (let [auth-token (create-auth-token)]
    (print
  (if (storage auth-token uid)
    (deliverer auth-token address)
    (println "ggg")
  ))))

(defn create-redis-storage [{prefix :prefix :as conn-opts}]
  (fn [auth-token uid]
    (let [prefixed-token (clojure.string/join "/" [prefix auth-token])]
         (car/wcar conn-opts (car/set prefixed-token uid)))))

(def url-safe-chars
  (let [chars-between #(map char (range (int %1) (int %2)))]
    (concat (chars-between \0 \9)
            (chars-between \a \z)
            (chars-between \A \Z))))

(defn fixed-length-password [n]
  (let [password (take n (repeatedly #(rand-nth url-safe-chars)))]
    (reduce str password)))

(defn create-auth-token [] (fixed-length-password 12))
