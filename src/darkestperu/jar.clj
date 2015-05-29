(ns darkestperu.jar
  (:require [clojure.java.io :as io]
            [schema.core :as s]
            [darkestperu.manifest :as manifest])
  (:import [java.util.jar JarEntry JarOutputStream]))

(s/defn make-jar
  "Creates a JAR in JAR-FILE with a manifest made from MANIFEST-MAP and
  containing the specified FILES.

  JAR-FILE and FILES can be Strings or Files."
  [jar-file files manifest-map :- manifest/ManifestMap]
  (with-open [jar-os (JarOutputStream. (io/output-stream jar-file)
                                       (manifest/map->manifest manifest-map))]
    (doseq [fl files]
      (let [e (JarEntry. (str fl))]
        (doto jar-os
          (.putNextEntry e)
          (.write (.getBytes (slurp fl))))))))

