(ns <<project-ns>>.core
  (:require
    [<<project-ns>>.ajax :refer [load-interceptors!]]
    [ajax.core :refer [GET]]
    [cljsjs.jquery]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [hoplon.core
     :as h
     :include-macros true]
    [hoplon.jquery]
    [javelin.core
     :refer [cell]
     :refer-macros [cell= dosync]]<% if expanded %>
    [markdown.core :refer [md->html]]<% endif %>
    [secretary.core :as secretary])
  (:import goog.History))

(defonce selected-page (cell :home))

(defonce docs (cell nil))

(defn nav-link [uri title page]
  (h/a :class (cell= {:is-active   (= page selected-page)
                      :navbar-item true})
       :href uri
       :click #(secretary/dispatch! uri)
       title))

(defn navbar []
  (let [expanded? (cell false)]
    (h/nav :class "navbar is-info"
      (h/div :class "navbar-brand"
        (h/a :class "navbar-item"
             :href "/"
             "myapp")
        (h/span :class (cell= {:navbar-burger true
                               :burger        true
                               :is-active     expanded?})
                :data-target "nav-menu"
                :click #(swap! expanded? not)
                (h/span)
                (h/span)
                (h/span)))
      (h/div :id "nav-menu " :class (cell= {:navbar-menu true
                                            :burger      true
                                            :is-active   expanded?})
             (h/div :class "navbar-start"
                    (nav-link "#/" "Home" :home)<% if expanded %>
                    (nav-link "#/about" "About" :about)<% endif %>)))))
<% if expanded %>
(defn about []
  (h/section :class "section"
    (h/div :class "container"
      (h/div :class "content"
        (h/img :src <% if servlet %>(str js/context "/img/warning_clojure.png")<% else %>"/img/warning_clojure.png"<% endif %>)))))
<% endif %>
(defn home []
  (h/section :class "section"
    (h/div :class "container"
      (h/div :class "content"
        <%if expanded %>(h/div :html (cell= (md->html docs)))<% else %>"Hello world"<% endif %>))))

(h/defelem page []
  (h/div :id "app"
    (navbar)
    (cell=
     (case selected-page
       :home (home)<% if expanded %>
       :about (about)<% endif %>))))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
 (reset! selected-page :home))
<% if expanded %>
(secretary/defroute "/about" []
 (reset! selected-page :about))
<% endif %>
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
 (doto (History.)
   (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
   (.setEnabled true)))

;; -------------------------
;; Initialize app<% if expanded %>
(defn fetch-docs! []
  (GET "/docs" {:handler #(reset! docs %)}))<% endif %>

(defn<% if shadow-cljs %> ^:dev/after-load<% endif %> mount-components []
  (js/jQuery #(.replaceWith (js/jQuery "#app") (page))))

(defn init! []
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components)<% if expanded %>
  (fetch-docs!)<% endif %>)
