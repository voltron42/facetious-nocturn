(ns facetious-nocturn.session-manager)

(defprotocol ISessionManager
  (host [this host-ip session])
  (join [this session-key guest])
  (kick [this host-ip guest-key])
  (leave [this guest-ip])
  (close [this host-ip])
  (get-session [this host-ip])
  (post-session [this host-ip session])
  (get-guest [this guest-ip])
  (post-guest [this guest-ip guest])
  )

(defn- build-session-manager []
  (let [session-cache (atom {})
        session-key-map (atom {})]
    (reify ISessionManager
      (host [this host-ip session]
        ; todo
        )
      (join [this session-key guest]
        ; todo
        )
      (kick [this host-ip guest-key]
        ; todo
        )
      (leave [this guest-ip]
        ; todo
        )
      (close [this host-ip]
        ; todo
        )
      (get-session [this host-ip]
        ; todo
        )
      (post-session [this host-ip session]
        ; todo
        )
      (get-guest [this guest-ip]
        ; todo
        )
      (post-guest [this guest-ip guest]
        ; todo
        ))))

(defonce singleton (build-session-manager))