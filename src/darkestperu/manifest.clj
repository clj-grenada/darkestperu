(ns darkestperu.manifest
  "Tools for creating a Manifest object from a Clojure map."
  (:require [clojure.string :as string]
            [schema.core :as s])
  (:import [java.util.jar Attributes Manifest]))

;;;; Generally useful helper functions

(defn separate
  "Separates M into two maps mt and mf. mt contains the entries of M whose key
  fulfills p, mf contains the entries of M whose key doesn't fulfill p. "
  [m p]
  (let [{t-entries true f-entries false} (group-by (fn [[k v]] (p k)) m)]
    [(into {} t-entries) (into {} f-entries)]))

(defn- uppercase-hyph-words
  "Returns S with the initial letter and all letters following hyphens
  uppercased."
  [s]
  (as-> s x
        (string/split x #"-")
        (map #(string/capitalize %) x)
        (string/join "-" x)))

;;;; Helper functions specific to creating Manifests

(defn- kw->manifest-key [kw]
  "Turns a Clojure keyword into a manifest key.

  :manifest-version → Manifest-Version"
  (uppercase-hyph-words (name kw)))

(defn- into-attrs
  "Puts the entries of M into a java.util.jar.Attributes object."
  [attrs m]
  (doseq [[k v] m]
    (.putValue attrs (kw->manifest-key k) v)))

;;;; Some schemas

(def ^:private VersionNumber
  (s/both s/Str
          (s/pred #(re-matches #"(?xms) \d+ (?: \. \d)?" %)
                  "is-version-number")))

(def ManifestMap
  {:manifest-version VersionNumber

   ; main attributes (general attributes might be a better name)
   ; IMPROVE: Add optional keys for often-used attributes.
   (s/optional-key s/Keyword) s/Str

   ; attributes for individual entries, Str key holds entry name
   (s/optional-key s/Str) {s/Keyword s/Str}})

;;;; The function everybody wants

(s/defn map->manifest
  "Turns a Clojure map into a java.util.jar.Manifest.

  See ManifestMap for how the Clojure map has to look in order to be turned into
  a manifest."
  [m :- ManifestMap]
  (let [manifest (Manifest.)
        main-attrs (.getMainAttributes manifest)
        [main-attr-map attr-secs] (separate m keyword?)
        secs (.getEntries manifest)]
    (into-attrs main-attrs main-attr-map)
    (doseq [[sec-name sec-attrs-map] attr-secs]
      (let [sec-attrs (Attributes.)]
        (into-attrs sec-attrs sec-attrs-map)
        (.put secs sec-name sec-attrs)))
    manifest))
