# QuPath Extension - Annotation Exchange

This extension for [QuPath](https://github.com/qupath/qupath) imports annotations made on `.svs` files using
[PaperJS](https://github.com/paperjs/paper.js) in combination with [OpenSeaDragon](https://github.com/openseadragon/openseadragon).

## Compatibility

This extension is currently only compatible with [QuPath 0.1.2](https://github.com/qupath/qupath/releases/tag/v0.1.2)

## JSON Structure

The coordinates, color, and other metadata of the annotations must be saved in a `.json` file, structured as follows:

```jsonc
{
  "SourceSlide": "name-of-file.svs",
  "dictionaries": [
    {
      "uid": "some-uid",
      "name": "some-name",
      // http://paperjs.org/reference/path/
      "path": {
          "applyMatrix": true,
          "data": {
            "id": "some-uid"
          },
          "segments": [
            // http://paperjs.org/reference/segment/#segment
            [
              [0.0, 0.0],
              [0.0, 0.0],
              [0.0, 0.0]
            ],
            // ...
          ],
          "closed": true,
          "fillColor": [0.0, 0.0, 0.0, 0.0],
          "strokeColor": [0.0, 0.0, 0.0],
          "strokeWidth": 50,
          "label": "Tumor" // (See `qupath.lib.objects.PathClass`)
      }
    }
  ]
}
```

A working example can be found below:

<details>
<summary>Full Example</summary>
<p>

```jsonc
{
  "SourceSlide": "24496.svs",
  "dictionaries": [
    [
      {
        "uid": "dc466dd0-15f7-11ea-94f7-3541d0425afc",
        "name": "dc466dd0-15f7-11ea-94f7-3541d0425afc",
        // http://paperjs.org/reference/path/
        "path": {
            "applyMatrix": true,
            "data": {
              "id": "dc466dd0-15f7-11ea-94f7-3541d0425afc"
            },
            "segments": [
              // http://paperjs.org/reference/segment/#segment
              [
                [4445.56952, 2904.39074],
                [0.9558, 6.05342],
                [-0.28748, -1.82071]
              ],
              [
                [4444.66043, 2909.84528],
                [1.02246, -1.53369],
                [-2.18812, 3.28217]
              ],
              [
                [4445.56952, 2921.66346],
                [-4.38693, -2.19347],
                [0.19696, 0.09848]
              ],
              [
                [4448.29679, 2922.57255],
                [0.00906, 0.09063],
                [-0.6098, -6.09799]
              ]
            ],
            "closed": true,
            "fillColor": [0.81569, 0.41569, 0.41569, 0.5],
            "strokeColor": [0.81569, 0.41569, 0.41569],
            "strokeWidth": 50
        }
      }
    ]
  ]
}
```

</p>
</details>

In the future, there will be efforts to remove redundant entries, and to improve the overall data structure (i.e.
transform arrays to hash-tables where it makes sense, and vice versa)

## Building the Extension

The following installations are required to build the
`AnnotationExchangeExtension.jar` artifact:

1. [Java Development Kit `8u151` (`JDK 1.8.0_151`)](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
(Requires registering for an Oracle account)

2. [Intellij IDEA 2017.3.7 (Community Edition)](https://www.jetbrains.com/idea/download/other.html)

    * Linux: https://download.jetbrains.com/idea/ideaIC-2017.3.7.tar.gz
    * Windows: https://download.jetbrains.com/idea/ideaIC-2017.3.7.exe
    * MacOS: https://download.jetbrains.com/idea/ideaIC-2017.3.7.dmg

### QuPath.jar

A build of [QuPath (v0.1.2)](https://github.com/qupath/qupath/tree/v0.1.2) is
required to ensure the Java compiler does not complain about the classes
`AnnotationExchangeExtension` extends.

Previous developers have built an artifact of `QuPath.jar`, and set that as a
dependency of `AnnotationExchangeExtension`. However, you may be able to set the
`QuPath` project as a dependency of `AnnotationExchangeExtension` without making
a build of it. If you are successful in doing the latter, you can skip this
step.

1. Clone QuPath

```bash
git clone git@github.com:qupath/qupath.git
# If you do not have an SSH key from your current terminal associated with your
# GitHub account, use the https link here:
git clone https://github.com/qupath/qupath.git
# The extension currently only works for the 0.1.2 release of QuPath
# It is crucial you do this before loading the project into IntelliJ, as later
# releases of QuPath use `Gradle` as the dependency manager, whereas earlier
# releases use `Maven`, which may confuse IntelliJ regarding which dependency
# manager to use
git checkout v0.1.2
```

2. Load the project into IntelliJ

* File -> New -> Project From Existing Sources
* Choose the `qupath/` directory you just cloned
* Click the Radio button for `Import project from external model`
* Choose `Maven`
* Leave all the defaults, and keep clicking `Next` until `Finish`

3. Change the compiler settings

* File -> Settings... -> Build, Execution, Deployment -> Compiler ->
Java Compiler
* Set `Project bytecode version` to `1.8`
* For all rows in the `Per-module bytecode version` table, ensure each one
has `Target bytecode version` set to `1.8`
* Then go to Build, Execution, Deployment -> Compiler -> Annotation Processors,
and ensure that `Enable annotation processing` is not checked off for the
modules under the `Default` dropdown, as well as the
`Maven default annotation processors profile`
![](docs/build-qupath/disable-annotation-processing-1.png)
![](docs/build-qupath/disable-annotation-processing-2.png)
* File -> Project Structure... -> Modules

  For each module listed, click the `Sources` tab, and ensure each
  `Language level` dropdown option is set to
  `8 - Lambdas, type annotations etc.`

![](docs/build-qupath/module-source-language-level.png)

4. Add module dependencies

* File -> Project Structure... -> Modules -> Click the `qupath` module

Click the `Dependencies` tab, and add Module Dependencies:

![](docs/build-qupath/add-module-dependencies.png)

Add all `qupath-*` modules, and check them off:

![](docs/build-qupath/module-dependencies.png)

5. Build QuPath `.class` files

* You should now be able to build the project by going to Build -> Build Project
(or `Ctrl+F9`), and if there are no issues, you should see the following on the
bottom left of the `IntelliJ` window:

![](docs/build-qupath/compilation-successful.png)

This means you should now be able to make `QuPath.jar`

6. Set up making `QuPath.jar`

* File -> Project Structure... -> Artifacts

![](docs/build-qupath/artifacts-from-modules-with-dependencies.png)

Leave `Module` as `<All Modules>`.

For `Main Class`, browse your file system with the `...` button, click the
`Project` tab, and select `src/main/java/qupath/QuPath.java`

![](docs/build-qupath/artifacts-select-main-class.png)

Your `Artifacts` screen should look like:

![](docs/build-qupath/artifacts-settings.png)

Note how all the `maven` dependencies (`.m2/repository`) are included in the
build, which `AnnotationExchangeExtension` will rely on.

Build the artifact by going to Build -> Build Artifacts... -> `qupath` -> Build

![](docs/build-qupath/build-artifact.png)

And the resulting file will be at:

`/path/to/qupath/out/artifacts/qupath/qupath.jar`

We now have the dependencies needed to build `AnnotationExchangeExtension`

### AnnotationExchangeExtension.jar

1. Clone `qupath-annotation`

```bash
git@github.com:bcgsc/qupath-annotation-exchange.git
# If you do not have an SSH key from your current terminal associated with your
# GitHub account, use the https link here:
https://github.com/bcgsc/qupath-annotation-exchange.git
```

2. Load project into IntelliJ

* File -> New -> Project From Existing Sources
* Choose the `qupath-annotation-exchange/` directory you just cloned
* Click the Radio button for `Import project from external model`
* Choose `Gradle`
* Leave all defaults and click `Next` until the `Finish` button
* Replace the `.idea/` directory if it prompts you to do so

3. Set `Qupath.jar` as a dependency

* File -> Project Structure... -> Libraries -> + -> Java

![](docs/build-annotation-extension/add-qupath-as-dependency-1.png)

Find the directory where `qupath.jar` was created:

![](docs/build-annotation-extension/add-qupath-as-dependency-2.png)

Add `qupath` to all `AnnotationExchangeExtension` modules:

![](docs/build-annotation-extension/add-qupath-as-dependency-3.png)

* File -> Project Structure... -> Modules

For each of the module options, highlighted with `<-`:

```
AnnotationExchangeExtension
  AnnotationExchangeExtension_main <-
  AnnotationExchangeExtension_test <-
AnnotationExchangeExtension <-
```

Ensure that the `qupath` library is checked:

![](docs/build-annotation-extension/add-qupath-as-dependency-4.png)

4. Make a build of `AnnotationExchangeExtension`

If the build was successful, you should see this at the bottom left of the
`IntelliJ` window:

![](docs/build-annotation-extension/build-complete.png)

5. Set up creating an `AnnotationExchangeExtension` artifact

* File -> Project Structure... -> Artifacts -> + -> JAR -> Empty

![](docs/build-annotation-extension/artifact-annotation-exchange-extension-1.png)

* Set the `Name:` to `AnnotationExchangeExtension`

* `+` -> Add Module Output
![](docs/build-annotation-extension/artifacts-add-module-output.png)

* Choose the module `AnnotationExchangeExtension_main`

![](docs/build-annotation-extension/artifacts-choose-module.png)

* `+` -> Directory Content

![](docs/build-annotation-extension/artifact-add-directory-content.png)

* Choose `/path/to/qupath-annotation-exchange/src/main/resources`

![](docs/build-annotation-extension/artifact-add-directory-content-path.png)

6. Build `AnnotationExchangeExtension.jar`

Build -> Build Artifacts... -> AnnotationExchangeExtension -> Build

![](docs/build-annotation-extension/build-artifact.png)

7. Import `AnnotationExchangeExtension.jar` in `QuPath`

Use your file system explorer to navigate to
`/path/to/qupath-annotation-exchange/out/artifacts/AnnotationExchangeExtension`

Open an [installed version of `QuPath`](https://github.com/qupath/qupath/releases/tag/v0.1.2):

![](docs/build-annotation-extension/qupath-window.png)

And drag the built `AnnotationExchangeExtension.jar` file into the `QuPath`
window. If QuPath prompts you to, accept a restart.

Click the Extensions button, and if you see "Annotations Exchange", with
options "Import JSON Annotation" and "Export JSON Annotation", congratulations,
you have successfully made a build of `AnnotationExchangeExtension` and added
it to `QuPath`.

If you have an `.svs` file, and annotations made using an application that
conforms to the JSON structure (i.e. the internal GSC React application), you
can now overlay those annotations over the `.svs` file:

![](docs/build-annotation-extension/qupath-imported-annotations.png)
