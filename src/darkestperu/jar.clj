(ns darkestperu.jar
  (:require [clojure.java.io :as io]
            [schema.core :as s]
            [darkestperu.manifest :as manifest])
  (:import [java.util.jar JarEntry JarOutputStream]))

;; Note: I know that java.nio.file.Path.relativize is more general than this,
;; but what I document here is the only functionality I need for make-jar.
(defn relativize-path
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
  containing the files from FILES-MAP.

  FILES-MAP has to map the path to the file on disk to the path it should have
  in the JAR. (The Javadoc for java.util.jar.JarEntry calls this the 'name' of
  the entry.)"
  [jar-file manifest-map :- manifest/ManifestMap files-map]
  (with-open [jar-os (JarOutputStream. (io/output-stream jar-file)
                                       (manifest/map->manifest manifest-map))]
    (doseq [[path-on-disk path-in-jar] files-map]
      (doto jar-os
        (.putNextEntry (JarEntry. (str path-in-jar)))
        (.write (.getBytes (slurp path-on-disk)))))))

