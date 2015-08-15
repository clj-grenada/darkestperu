(ns darkestperu.jar
  "Procedures for making JAR files and reading from them."
  (:require [clojure.java.io :as io]
            [guten-tag.core :as gt]
            [schema.core :as s]
            [darkestperu.manifest :as manifest])
  (:import [java.util.jar JarEntry JarFile JarOutputStream]))

;;;; Utility functions

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


;;;; Support for different types of things to put in a JAR

(gt/deftag file-entry [contents]
           {:pre [(some? contents) (extends? io/Coercions (type contents))]})

(gt/deftag string-entry [contents]
           {:pre [(string? contents)]})

(defmulti entry-to-bytes gt/tag)

(defmethod entry-to-bytes ::file-entry [[_ {:keys [contents]}]]
  (.getBytes (slurp contents)))

(defmethod entry-to-bytes ::string-entry [[_ {:keys [contents]}]]
  (.getBytes contents))


;;;; Actually dealing with JARs

(s/defn make-jar
  "Creates a JAR in JAR-FILE with a manifest made from MANIFEST-MAP and
  containing the files from FILES-MAP.

  FILES-MAP has to map the path to the file on disk to the path it should have
  in the JAR. (The Javadoc for java.util.jar.JarEntry calls this the 'name' of
  the entry.)"
  [jar-file manifest-map :- manifest/ManifestMap files-map]
  (with-open [jar-os (JarOutputStream. (io/output-stream jar-file)
                                       (manifest/map->manifest manifest-map))]
    (doseq [[path-in-jar data] files-map]
      (doto jar-os
        (.putNextEntry (JarEntry. (str path-in-jar)))
        (.write (entry-to-bytes data))))))

(defn as-jar-file
  "Makes a JarFile from JAR-FILEABLE, which has to be a valid input for
  clj::clojure.java.io/as-file."
  [jar-fileable]
  (JarFile. (io/as-file jar-fileable)))

(defn jar-seq
  "Returns a sequence of the paths of the entries in JAR denoted by
  JAR-FILEABLE."
  [jar-fileable]
  (->> jar-fileable
       as-jar-file
       .entries
       enumeration-seq
       (map #(.getName %))))

(defn slurp-from-jar
  "Slurps the contents of the entry with PATH (as returned by jar-seq) from JAR
  denoted by JAR-FILEABLE."
  [jar-fileable path]
  (-> jar-fileable
      as-jar-file
      (as-> x
        (.getInputStream x
                         (.getJarEntry x path)))
      slurp))
