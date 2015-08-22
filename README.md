# Zephyr

A Clojure library for passwordless authentication

## Usage

Currently, Zephyr exposes four functions: `send-auth-token`, `validate-auth-token`,
`create-twilio-deliverer`, `create-simple-smtp-deliverer.

Using Twilio to deliver sms:

```clojure
(in-ns 'zephyr.core)
(def sms-deliverer
     (create-twilio-deliverer {:twilio-sid ""
                               :twilio-auth-token ""
                               :sender "+18008008888"
                               :template "Welcome to Ladder Life! Click here to continue: http://ladderlife.com/auth?token=%s"}))
(def redis-opts {:pool {}
                 :spec {:host "127.0.0.1" :port 6379}
                 :prefix "TKN"})
(send-auth-token redis-opts uid sms-deliverer phone-number)
````

Using SMTP services:

```clojure
(def smtp-deliverer
     (create-simple-smtp-deliverer {:host "smtp.ladderlife.com"
                                    :user "ladder"
                                    :pass ""}
                                   {:from "hello@ladderlife.com"
                                    :subject "Welcome to Ladder Life"
                                    :template "Click here to continue: http://laderlife.com/auth?token=%s"}))

(send-auth-token redis-opts uid sms-deliverer phone-number)
````

To validate:

```clojure
(validate-auth-token auth-token) ; returns uid or nil if auth-token doesn't exist
```

## Todo

- Handling error
- Offer better smtp templates (support html)
- Documentation for rolling your own delivery system
- Documentation for rolling your own storage (will depend on error handling)
- Extract delivery and storage services from core
- Rename library

## License

Copyright © 2015 Ladder Life

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
