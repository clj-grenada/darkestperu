(ns darkestperu.jar
  (:require [clojure.java.io :as io]
            [schema.core :as s]
            [darkestperu.manifest :as manifest])
  (:import [java.util.jar JarEntry JarOutputStream]))

;; Note: I know that java.nio.file.Path.relativize is more general than this,
;; but what I document here is the only functionality I need for make-jar.
(defn- relativize-path
  "Expects that DEEPER is a path leading below SHALLOWER. Returns the relative
  path to DEEPER starting from SHALLOWER.

  DEEPER and SHALLOWER can be Strings or Files."
  [shallower deeper]
  (-> shallower
      io/as-file
      .toPath
      (.relativize (.toPath (io/as-file deeper)))
      .toFile))

(s/defn make-jar
  "Creates a JAR in JAR-FILE with a manifest made from MANIFEST-MAP and
  containing the specified FILES and STRING-ENTRIES.

  JAR-FILE and FILES can be Strings or Files. Their paths in the JAR will be the
  given paths with TRIM-PREFIX removed. Example:

    path: /home/paddington/jam.class, TRIM-PREFIX: /home/paddington,
    path in JAR: jam.class

  TRIM-PREFIX has to be a prefix of the path of all FILES.

  JAR file contents can also be written directly from strings. STRING-ENTRIES is
  expected to be a map from path in JAR to String to be written."
  ([jar-file manifest-map :- manifest/ManifestMap files]
   (make-jar jar-file manifest-map files nil {}))
  ([jar-file manifest-map :- manifest/ManifestMap files trim-prefix]
   (make-jar jar-file manifest-map files trim-prefix {}))
  ([jar-file manifest-map :- manifest/ManifestMap files trim-prefix
    string-entries]
   (with-open [jar-os (JarOutputStream. (io/output-stream jar-file)
                                        (manifest/map->manifest manifest-map))]
     (doseq [[p s] string-entries]
       (doto jar-os
         (.putNextEntry (JarEntry. p))
         (.write (.getBytes s))))
     (doseq [fl files]
       (let [e (JarEntry. (str (if trim-prefix
                                 (relativize-path trim-prefix fl)
                                 fl)))]
         (doto jar-os
           (.putNextEntry e)
           (.write (.getBytes (slurp fl)))))))))

