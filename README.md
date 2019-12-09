# QuPath Extension - Annotation Exchange

This extension for [QuPath](https://github.com/qupath/qupath) imports annotations made on `.svs` files using
[PaperJS](https://github.com/paperjs/paper.js) in combination with [OpenSeaDragon](https://github.com/openseadragon/openseadragon).

## Compatibility

This extension is currently only compatible with [QuPath 0.1.2](https://github.com/qupath/qupath/releases/tag/v0.1.2)

## JSON Structure

The coordinates, color, and other metadata of the annotations must be saved in a `.json` file, structured as follows:

```jsonc
[
  {
    "SourceSlide": "name-of-file.svs"
  },
  {
    "dictionaries": [
      [
        {
          "uid": "some-uid",
          "name": "some-name",
          // http://paperjs.org/reference/path/
          "path": [
            "Path",
            {
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
              "strokeWidth": 50
            }
          ],
          "zoom": 0,
          "context": [],
          "dictionary": "default"
        }
      ]
    ]
  }
]
```

A working example can be found below:

<details>
<summary>Full Example</summary>
<p>

```jsonc
[
  {
    "SourceSlide": "24496.svs"
  },
  {
    "dictionaries": [
      [
        {
          "uid": "dc466dd0-15f7-11ea-94f7-3541d0425afc",
          "name": "dc466dd0-15f7-11ea-94f7-3541d0425afc",
          // http://paperjs.org/reference/path/
          "path": [
            "Path",
            {
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
          ],
          "zoom": 0,
          "context": [],
          "dictionary": "default"
        }
      ]
    ]
  }
]
```

</p>
</details>

In the future, there will be efforts to remove redundant entries, and to improve the overall data structure (i.e.
transform arrays to hash-tables where it makes sense, and vice versa)
